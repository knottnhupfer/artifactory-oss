
<#if addHa && addOnce>
## add HA entries when ha is configured
<Proxy balancer://${upstreamName}>
    <#list hsservers as haserver>
    ${haserver}
    </#list>
ProxySet lbmethod=byrequests
ProxySet failonstatus=503
</Proxy>
</#if>
<#if !generalOnly>

Listen ${repoPort}
</#if>
<VirtualHost *:<#if httpOnly && generalOnly>${httpPort}</#if><#if !generalOnly>${repoPort}</#if><#if httpsOnly && generalOnly>${sslPort}</#if>>

    ProxyPreserveHost On

    ServerName ${serverName}
    ServerAlias *.${serverName}
    ServerAdmin server@admin

<#if useHttps && !httpOnly>
    SSLEngine on
    SSLCertificateFile ${sslCrtPath}
    SSLCertificateKeyFile ${sslKeyPath}
    SSLProxyEngine on
</#if>

    ## Application specific logs
    ## ErrorLog ${APACHE_LOG_DIR}/${serverName}-error.log
    ## CustomLog ${APACHE_LOG_DIR}/${serverName}-access.log combined

    AllowEncodedSlashes On
    RewriteEngine on

    RewriteCond %{SERVER_PORT} (.*)
    RewriteRule (.*) - [E=my_server_port:%1]
    ##  NOTE: The 'REQUEST_SCHEME' Header is supported only from apache version 2.4 and above
    RewriteCond %{REQUEST_SCHEME} (.*)
    RewriteRule (.*) - [E=my_scheme:%1]

    RewriteCond %{HTTP_HOST} (.*)
    RewriteRule (.*) - [E=my_custom_host:%1]

<#if subdomain>
    RewriteCond "%{REQUEST_URI}" "^/(v1|v2)/"
    RewriteCond "%{HTTP_HOST}" ${quotes}^(.*)\.${serverName}$${quotes}
    RewriteRule "^/(v1|v2)/(.*)$" ${quotes}${webPublicContext}api/docker/%1/$1/$2${quotes} [PT]
</#if>
<#if isRepoPath>
    RewriteRule "^/(v2)/(.*)$" ${quotes}${webPublicContext}$1/$2${quotes} [P]
</#if>

<#if (!subdomain && !generalOnly && !isRepoPath) || isSamePort>
    RewriteRule "^/(v1|v2)/(.*)$" ${quotes}${webPublicContext}api/docker/${repoKey}/$1/$2${quotes} [P]
</#if>

    RewriteRule ^/$                ${webPublicContext}webapp/ [R,L]
    RewriteRule ^/${publicContext}(/)?$      ${webPublicContext}webapp/ [R,L]
    RewriteRule ^/${publicContext}/webapp$   ${webPublicContext}webapp/ [R,L]

    RequestHeader set Host %{my_custom_host}e
    RequestHeader set X-Forwarded-Port %{my_server_port}e
    ## NOTE: {my_scheme} requires a module which is supported only from apache version 2.4 and above
    RequestHeader set X-Forwarded-Proto %{my_scheme}e
    RequestHeader set X-Artifactory-Override-Base-Url %{my_scheme}e://${serverName}:%{my_server_port}e${publicContextWithSlash}
    ProxyPassReverseCookiePath /${absoluteAppContext} /${publicContext}

<#if addGeneral>
    ProxyRequests off
    ProxyPreserveHost on
</#if>
<#if !addHa>
    ProxyPass ${webPublicContext} http://${localNameAndPort}/${appContext}
    ProxyPassReverse ${webPublicContext} http://${localNameAndPort}/${appContext}
</#if>
<#if addHa>
    ProxyPass ${webPublicContext} balancer://${upstreamName}/${appContext}
    ProxyPassReverse ${webPublicContext} balancer://${upstreamName}/${appContext}
</#if>
</VirtualHost>
