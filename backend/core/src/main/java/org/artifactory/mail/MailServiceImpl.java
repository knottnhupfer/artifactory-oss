/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.mail;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.mail.MailServerConfiguration;
import org.artifactory.api.mail.MailService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.mail.MailServerDescriptor;
import org.artifactory.util.EmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;



/**
 * The mail service's main implementation
 *
 * @author Noam Tenne
 */
@Service
public class MailServiceImpl implements MailService {

    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    @Autowired
    private CentralConfigService centralConfig;

    /**
     * Send an e-mail message based on the parameters in the mail server configuration descriptor
     *
     * @param recipients Recipients of the message that will be sent
     * @param subject    The subject of the message
     * @param body       The body of the message
     * @throws EmailException
     */
    @Override
    public void sendMail(String[] recipients, String subject, String body) throws EmailException {
        MailServerConfiguration mailServerConfiguration = getMailServerConfig();
        if (mailServerConfiguration == null) {
            log.warn("Unable to send E-mail: No mail server configuration found.");
            return;
        }
        sendMail(recipients, subject, body, mailServerConfiguration);
    }


    /**
     * Send an e-mail message
     *
     * @param recipients    Recipients of the message that will be sent
     * @param subject       The subject of the message
     * @param body          The body of the message
     * @param config        A mail server configuration to use
     * @param recipientType {@link Message.RecipientType}
     * @throws EmailException
     */
    @Override
    public void sendMail(String[] recipients, String subject, String body, MailServerConfiguration config,
            Message.RecipientType recipientType) throws EmailException {
        verifyParameters(recipients, config);

        if (!config.isEnabled()) {
            log.debug("Ignoring requested mail delivery. The given configuration is disabled.");
            return;
        }

        if (recipients.length == 0) {
            log.debug("No recipients specified.");
            return;
        }

        boolean debugEnabled = log.isDebugEnabled();

        Properties properties = new Properties();

        properties.put("mail.smtp.host", config.getHost());
        properties.put("mail.smtp.port", Integer.toString(config.getPort()));

        properties.put("mail.smtp.quitwait", "false");

        //Default protocol
        String protocol = "smtp";

        //Enable TLS if set - all versions need to be explicitly declared in Java
        if (config.isUseTls()) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("mail.smtp.starttls.enable", "true");
            map.put("mail.smtp.ssl.trust", config.getHost());
            String protocols = "mail.smtp.ssl.protocols";
            map.put(protocols, "TLSv1");
            map.put(protocols, "TLSv1.1");
            map.put(protocols, "TLSv1.2");
            properties.putAll(map);
        }

        //Enable SSL if set
        boolean useSsl = config.isUseSsl();
        if (useSsl) {
            properties.put("mail.smtp.socketFactory.port", Integer.toString(config.getPort()));
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtp.socketFactory.fallback", "false");
            //Requires special protocol
            protocol = "smtps";
        }

        //Set debug property if enabled by the logger
        properties.put("mail.debug", debugEnabled);

        Authenticator authenticator = null;
        if (!StringUtils.isEmpty(config.getUsername())) {
            properties.put("mail.smtp.auth", "true");
            authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getUsername(), config.getPassword());
                }
            };
        }

        Session session = Session.getInstance(properties, authenticator);
        Message message = new MimeMessage(session);

        String subjectPrefix = config.getSubjectPrefix();
        String fullSubject = (!StringUtils.isEmpty(subjectPrefix)) ? (subjectPrefix + " " + subject) : subject;
        try {
            message.setSubject(fullSubject);

            if (!StringUtils.isEmpty(config.getFrom())) {
                InternetAddress addressFrom = new InternetAddress(config.getFrom());
                message.setFrom(addressFrom);
            }

            InternetAddress[] addressTo = new InternetAddress[recipients.length];
            for (int i = 0; i < recipients.length; i++) {
                addressTo[i] = new InternetAddress(recipients[i]);
            }
            message.addRecipients(recipientType, addressTo);

            //Create multi-part message in case we want to add html support
            Multipart multipart = new MimeMultipart("related");

            BodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(body, "text/html");
            multipart.addBodyPart(htmlPart);
            message.setContent(multipart);

            //Set debug property if enabled by the logger
            session.setDebug(debugEnabled);

            //Connect and send
            Transport transport = session.getTransport(protocol);
            if (useSsl) {
                transport.connect(config.getHost(), config.getPort(), config.getUsername(), config.getPassword());
            } else {
                transport.connect();
            }
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (MessagingException e) {
            throw new EmailException("Failed to send e-mail to " + recipients[0] + " due to "+e.getMessage(), e);
        }
    }


    /**
     * Send an e-mail message to users using Message.RecipientType.TO RecipientType
     *
     * @param recipients Recipients of the message that will be sent
     * @param subject    The subject of the message
     * @param body       The body of the message
     * @param config     A mail server configuration to use
     * @throws EmailException
     */
    @Override
    public void sendMail(String[] recipients, String subject, String body, final MailServerConfiguration config)
            throws EmailException {
        sendMail(recipients, subject, body, config, Message.RecipientType.TO);
    }

    /**
     * Return the mail configuration object based on the params from the descriptor
     *
     * @return MailServerConfiguration - Configuration object. Null if no descriptor exists
     */
    private MailServerConfiguration getMailServerConfig() {
        CentralConfigDescriptor descriptor = centralConfig.getDescriptor();
        MailServerDescriptor m = descriptor.getMailServer();
        if (m == null) {
            return null;
        }

        return new MailServerConfiguration(m.isEnabled(), m.getHost(), m.getPort(), m.getUsername(),
                CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), m.getPassword()), m.getFrom(), m.getSubjectPrefix(),
                m.isTls(), m.isSsl(), m.getArtifactoryUrl());
    }

    private void verifyParameters(String[] recipients, MailServerConfiguration config) {
        if (recipients == null) {
            throw new EmailException("Recipient list cannot be null.");
        }

        if (config == null) {
            throw new EmailException("Mail server configuration cannot be null.");
        }
    }
}
