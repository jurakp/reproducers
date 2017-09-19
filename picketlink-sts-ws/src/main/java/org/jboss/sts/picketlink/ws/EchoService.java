package org.jboss.sts.picketlink.ws;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.xml.ws.WebServiceContext;

import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ws.api.annotation.EndpointConfig;

/**
 * Session Bean implementation class EchoService
 */
@Stateless
@WebService
@EndpointConfig(configName = "SAML-WSSecurity-Endpoint")
@SecurityDomain("sp")
@RolesAllowed("testRole")
public class EchoService implements EchoServiceRemote {

    @Resource
    private WebServiceContext wsCtx;
    @Resource
    private SessionContext ejbCtx;

    @Override
    @WebMethod
    public void echo(final String echo) {
        System.out.println("EchoService: " + echo);
        System.out.println("Principal: " + wsCtx.getUserPrincipal());
        System.out.println("Principal.getName(): " + wsCtx.getUserPrincipal().getName());
        System.out.println("wctx isUserInRole('testRole'): " + wsCtx.isUserInRole("testRole"));
        System.out.println("ejbctx isUserInRole('testRole'): " + ejbCtx.isCallerInRole("testRole"));
        
        try {
            Subject subject = (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
            System.out.println("Found subject in web service call: " + subject);
            SecurityContext current = SecurityContextAssociation.getSecurityContext();
            System.out.println("Found security context in web service call: " + current);
            System.out.println("Security domain in web service call: " + current.getSecurityDomain());
            System.out.println("Subject info in web service call: " + current.getSubjectInfo());
        } catch (PolicyContextException e) {
            throw new IllegalStateException(e);
        }
    }

}
