/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.sts.provider;

import org.apache.cxf.ws.security.sts.provider.SecurityTokenServiceProvider;
import org.apache.ws.security.saml.ext.bean.AttributeStatementBean;
import org.jboss.sts.callback.STSBearerCallbackHandler;
import org.apache.cxf.annotations.EndpointProperties;
import org.apache.cxf.annotations.EndpointProperty;
import org.apache.cxf.interceptor.InInterceptors;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.interceptor.OutInterceptors;
import org.apache.cxf.interceptor.security.JAASLoginInterceptor;
import org.apache.cxf.sts.StaticSTSProperties;
import org.apache.cxf.sts.claims.ClaimsHandler;
import org.apache.cxf.sts.claims.ClaimsManager;
import org.apache.cxf.sts.claims.ClaimsParameters;
import org.apache.cxf.sts.claims.ClaimsParser;
import org.apache.cxf.sts.claims.IdentityClaimsParser;
import org.apache.cxf.sts.operation.TokenIssueOperation;
import org.apache.cxf.sts.service.ServiceMBean;
import org.apache.cxf.sts.service.StaticService;
import org.apache.cxf.sts.token.provider.AttributeStatementProvider;
import org.apache.cxf.sts.token.provider.DefaultAttributeStatementProvider;
import org.apache.cxf.sts.token.provider.SAMLTokenProvider;
import org.apache.cxf.sts.token.provider.TokenProviderParameters;

import javax.xml.ws.WebServiceProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author pjurak
 *
 */
@WebServiceProvider(serviceName = "SecurityTokenService", portName = "UT_Port", targetNamespace = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/", wsdlLocation = "WEB-INF/wsdl/sts-provider.wsdl")
// be sure to have dependency on org.apache.cxf module when on AS7, otherwise Apache CXF annotations are ignored
@EndpointProperties(value = { @EndpointProperty(key = "ws-security.signature.username", value = "mystskey"),
        @EndpointProperty(key = "ws-security.signature.properties", value = "stsKeystore.properties"),
        @EndpointProperty(key = "ws-security.callback-handler", value = "org.jboss.sts.callback.STSBearerCallbackHandler") })
@InInterceptors(classes = { LoggingInInterceptor.class })
@OutInterceptors(classes = { LoggingOutInterceptor.class })
public class STSBearerProvider extends SecurityTokenServiceProvider {

    public STSBearerProvider() throws Exception {
        super();

        StaticSTSProperties props = new StaticSTSProperties();
        props.setSignatureCryptoProperties("stsKeystore.properties");
        props.setSignatureUsername("mystskey");
        props.setCallbackHandlerClass(STSBearerCallbackHandler.class.getName());
        props.setEncryptionCryptoProperties("stsKeystore.properties");
        props.setEncryptionUsername("myservicekey");
        props.setIssuer("DoubleItSTSIssuer");

        List<ServiceMBean> services = new LinkedList<ServiceMBean>();
        StaticService service = new StaticService();
        service.setEndpoints(Arrays.asList("http://localhost:(\\d)*/sts-web-service/EchoService",
                "http://\\[::1\\]:(\\d)*/sts-web-service/EchoService",
                "http://\\[0:0:0:0:0:0:0:1\\]:(\\d)*/sts-web-service/EchoService"));
        services.add(service);

        TokenIssueOperation issueOperation = new TokenIssueOperation();
        SAMLTokenProvider tokenProvider = new SAMLTokenProvider();
        List<AttributeStatementProvider> customProviderList = new ArrayList<AttributeStatementProvider>();
        customProviderList.add(new DefaultAttributeStatementProvider());
        customProviderList.add(new CustomAttributeProvider());
        tokenProvider.setAttributeStatementProviders(customProviderList);
        issueOperation.getTokenProviders().add(tokenProvider);
        ClaimsManager claimsManager = new ClaimsManager();
        claimsManager.setClaimParsers(Arrays.asList(new ClaimsParser[] { new IdentityClaimsParser() }));
        claimsManager.setClaimHandlers(Arrays.asList(new ClaimsHandler[] { new RoleClaimHandler() }));
        issueOperation.setClaimsManager(claimsManager);
        issueOperation.setServices(services);
        issueOperation.setStsProperties(props);
        this.setIssueOperation(issueOperation);
    }

}
