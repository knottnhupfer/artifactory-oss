
<#if addHa>
## add HA entries when ha is configure
upstream ${upstreamName} {
    <#list hsservers as haserver>
    server ${haserver};
    </#list>
}
</#if>
<#if addSsl>
## add ssl entries when https has been set in config
ssl_protocols TLSv1 TLSv1.1 TLSv1.2 TLSv1.3;
ssl_certificate      ${sslCrtPath};
ssl_certificate_key  ${sslKeyPath};
ssl_session_cache shared:SSL:1m;
ssl_prefer_server_ciphers   on;
</#if>
## server configuration
server {
    <#-- set the server port to lisen , depend if ssl or docker repository is configure  -->
    <#if !generalOnly>listen ${repoPort}<#if useHttps> ssl</#if>;</#if><#if generalOnly && useHttps>listen ${sslPort} ssl;</#if>
    <#if useHttp && generalOnly>listen ${httpPort} ;</#if>
    <#-- set subdomain regex if doocker subdomain method has been define-->
    <#if subdomain>server_name ~(?<repo>.+)\.${serverName} ${serverName};</#if>
    <#if !subdomain>server_name ${serverName};</#if>
    if ($http_x_forwarded_proto = '') {
        set $http_x_forwarded_proto  $scheme;
    }
    ## Application specific logs
    ## access_log /var/log/nginx/${serverName}-access.log timing;
    ## error_log /var/log/nginx/${serverName}-error.log;
<#-- set webapp rewrite incase of general or subdomain setting-->
<#if generalOnly || subdomain || isRepoPath >
    rewrite ^/$ ${webPublicContext}webapp/ redirect;
    rewrite ^/${publicContext}/?(/webapp)?$ ${webPublicContext}webapp/ redirect;
<#-- set docker v1/v2 rewrite incase of subdomain setting-->
    <#if subdomain>
    rewrite ^/(v1|v2)/(.*) ${webPublicContext}api/docker/$repo/$1/$2;
    </#if>
    <#if isRepoPath>
    rewrite ^/(v2)/(.*) ${webPublicContext}$1/$2;
    </#if>
</#if>
<#-- set docker v1/v2 rewrite incase of port method setting or
 in case docker ssl and general ports are the same-->
<#if (!generalOnly && !subdomain && !isRepoPath) || isSamePort>
    rewrite ^/(v1|v2)/(.*) ${webPublicContext}api/docker/${repoKey}/$1/$2;
</#if>
    chunked_transfer_encoding on;
    client_max_body_size 0;
<#-- set location and required headers to artifactory server-->
    location ${webPublicContext} {
    proxy_read_timeout  2400s;
    proxy_pass_header   Server;
    proxy_cookie_path   ~*^/.* /;
    <#if generalOnly>
    if ( $request_uri ~ ^${webPublicContext}(.*)$ ) {
        proxy_pass          http://${localNameAndPort}/${appContext}$1;
    }
    </#if>
    proxy_pass          http://${localNameAndPort}/${appContext};
    <#if addHa>
    proxy_next_upstream http_503 non_idempotent;
    </#if>
    proxy_set_header    X-Artifactory-Override-Base-Url $http_x_forwarded_proto://$host:$server_port${publicContextWithSlash};
    proxy_set_header    X-Forwarded-Port  $server_port;
    proxy_set_header    X-Forwarded-Proto $http_x_forwarded_proto;
    proxy_set_header    Host              $http_host;
    proxy_set_header    X-Forwarded-For   $proxy_add_x_forwarded_for;
    }
}
