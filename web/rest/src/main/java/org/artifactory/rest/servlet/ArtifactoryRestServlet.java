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

package org.artifactory.rest.servlet;


import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.BeanUtilsBean2;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.storage.binstore.service.InternalBinaryService;
import org.artifactory.webapp.servlet.DelayedInit;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jfrog.storage.binstore.ifc.BinaryProviderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.servlet.Filter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.util.concurrent.BlockingQueue;

/**
 * We use our own rest servlet for the initialization using ArtifactoryContext.
 *
 * @author Yossi Shaul
 */
public class ArtifactoryRestServlet extends ServletContainer implements DelayedInit {


    private static final Logger log = LoggerFactory.getLogger(ArtifactoryRestServlet.class);

    private ServletConfig config;

    @SuppressWarnings({"unchecked"})
    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        BlockingQueue<Filter> waiters = (BlockingQueue<Filter>) config.getServletContext()
                .getAttribute(APPLICATION_CONTEXT_LOCK_KEY);
        if (waiters != null) {
            waiters.add(this);
        } else {
            //Servlet 2.5 lazy filter initing
            delayedInit();
        }
        // register the bean utils converter to fail if encounters a type mismatch between the destination
        // and the original, see RTFACT-3610
        BeanUtilsBean instance = BeanUtilsBean2.getInstance();
        instance.getConvertUtils().register(true, false, 0);
    }

    @Override
    public void delayedInit() throws ServletException {
        super.init(config);
        ApplicationContext appContext = (ApplicationContext) config.getServletContext().getAttribute(ArtifactoryContext.APPLICATION_CONTEXT_KEY);
        if (appContext == null) {
            throw new IllegalStateException("Got null Application Context in delayed init!");
        }
        // Add the BinaryProvider Manager to the servlet context
        config.getServletContext().setAttribute(BinaryProviderManager.MANAGER_CONTEXT_KEY, appContext.getBean(InternalBinaryService.class).getBinaryProviderManager());
    }

}
