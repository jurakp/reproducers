package org.jboss.sts.picketlink.ws.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import org.jboss.sts.picketlink.ws.EchoServiceRemote;
import org.jboss.ws.api.configuration.ClientConfigUtil;
import org.picketlink.identity.federation.api.wstrust.WSTrustClient.SecurityInfo;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.fed.WSTrustException;
import org.picketlink.identity.federation.api.wstrust.WSTrustClient;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.trust.jbossws.SAML2Constants;

/**
 * @author petr
 *
 */
public class WsClient {

    private static String username = "UserA";
    private static String password = "PassA";

    /**
     * @param args
     * @throws ParsingException
     * @throws MalformedURLException
     */
    public static void main(String[] args) throws Exception {
        WSTrustClient client = new WSTrustClient("PicketLinkSTS", "PicketLinkSTSPort",
                "http://localhost:8080/picketlink-sts/PicketLinkSTS", new SecurityInfo(username, password));
        Element assertion = null;
        try {
            System.out.println("Invoking token service to get SAML assertion for " + username);
            assertion = client.issueToken(SAMLUtil.SAML2_TOKEN_TYPE);
            System.out.println("SAML assertion for " + username + " successfully obtained!");
        } catch (WSTrustException wse) {
            System.out.println("Unable to issue assertion: " + wse.getMessage());
            wse.printStackTrace();
            System.exit(1);
        }

        URL wsdl = new URL("http://127.0.0.1:8080/picketlink-sts-ws/EchoService?wsdl");
        QName serviceName = new QName("http://ws.picketlink.sts.jboss.org/", "EchoServiceService");
        Service service = Service.create(wsdl, serviceName);
        EchoServiceRemote port = service.getPort(new QName("http://ws.picketlink.sts.jboss.org/", "EchoServicePort"),
                EchoServiceRemote.class);

        BindingProvider bp = (BindingProvider) port;
        //ClientConfigUtil.setConfigHandlers(bp, "META-INF/standard-jaxws-client-config.xml", "SAML WSSecurity Client");
        bp.getRequestContext().put(SAML2Constants.SAML2_ASSERTION_PROPERTY, assertion);
        List<Handler> handlers = bp.getBinding().getHandlerChain();
        handlers.add(new org.picketlink.trust.jbossws.handler.SAML2Handler());
        bp.getBinding().setHandlerChain(handlers);
        port.echo("Test");
        System.out.println("Check the server output log.");

    }

}
