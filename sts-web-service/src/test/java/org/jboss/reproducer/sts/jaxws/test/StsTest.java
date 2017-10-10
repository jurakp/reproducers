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

package org.jboss.reproducer.sts.jaxws.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.xml.ws.BindingProvider;

import org.apache.cxf.BusException;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.ws.security.SecurityConstants;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.reproducer.sts.jaxws.EchoServiceImpl;
import org.jboss.reproducer.sts.jaxws.generated.EchoService;
import org.jboss.reproducer.sts.jaxws.generated.EchoService_Service;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author pjurak
 *
 */
@RunWith(Arquillian.class)
public class StsTest {

    private static final String APP_NAME = "sts-web-service";
    private static final String SEI = "EchoService";
    private static final String WSDL_PATH = SEI + "?wsdl";

    @ArquillianResource
    private URL deploymentUrl;
    private static final String WEBAPP_SRC = "src/main/webapp";

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, APP_NAME + ".war").addPackage(EchoServiceImpl.class.getPackage())
                .addPackage(EchoService.class.getPackage()).addAsWebInfResource(new File(WEBAPP_SRC, "WEB-INF/echo.wsdl"))
                .addAsResource(new File("src/main/resources/serviceKeystore.properties"))
                .addAsResource(new File("src/main/resources/servicestore.jks"))
                .addAsResource(new File("src/main/resources/log4j.xml")).addAsWebResource(new File(WEBAPP_SRC, "index.html"))
                .addAsWebInfResource(new File(WEBAPP_SRC, "WEB-INF/beans.xml"))
                .addAsWebInfResource(new File(WEBAPP_SRC, "WEB-INF/jboss-web.xml"))
                .setWebXML(new File(WEBAPP_SRC, "WEB-INF/web.xml"))
                .setManifest(new StringAsset("Dependencies: org.jboss.ws.cxf.jbossws-cxf-client, org.jboss.ws.cxf.jbossws-cxf-server, org.apache.cxf.impl, org.opensaml"));
        war.as(ZipExporter.class).exportTo(new File("target/shrink.war"), true);
        return war;
    }

    @Test
    public void runApi() throws BusException, EndpointException {
        try {
            EchoService service = new EchoService_Service(new URL(deploymentUrl.toString() + WSDL_PATH)).getEchoServicePort();
            // EchoService service = new EchoService_Service(new URL("http://localhost:8080/sts-web-service/" +
            // WSDL_PATH)).getEchoServicePort();
            Map<String, Object> ctx = ((BindingProvider) service).getRequestContext();
            // set the security related configuration information for the service "request"
            ctx.put(SecurityConstants.USERNAME, "alice");
            ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallBack());
            ctx.put(SecurityConstants.SIGNATURE_PROPERTIES,
                    Thread.currentThread().getContextClassLoader().getResource("clientKeystore.properties"));
            ctx.put(SecurityConstants.ENCRYPT_PROPERTIES,
                    Thread.currentThread().getContextClassLoader().getResource("clientKeystore.properties"));
            ctx.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");
            ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myservicekey");

            // -- Configuration settings that will be transfered to the STSClient
            // "alice" is the name provided for the WSS Username. Her password will
            // be retreived from the ClientCallbackHander by the STSClient.
            ctx.put(SecurityConstants.USERNAME + ".it", "alice");
            ctx.put(SecurityConstants.CALLBACK_HANDLER + ".it", new ClientCallBack());
            ctx.put(SecurityConstants.ENCRYPT_PROPERTIES + ".it",
                    Thread.currentThread().getContextClassLoader().getResource("clientKeystore.properties"));
            ctx.put(SecurityConstants.ENCRYPT_USERNAME + ".it", "mystskey");
            ctx.put(SecurityConstants.STS_TOKEN_USERNAME + ".it", "myclientkey");
            ctx.put(SecurityConstants.STS_TOKEN_PROPERTIES + ".it",
                    Thread.currentThread().getContextClassLoader().getResource("clientKeystore.properties"));
            ctx.put(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO + ".it", "true");

            String response = service.echo("test");
            assertEquals("Hello! test", response);

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
