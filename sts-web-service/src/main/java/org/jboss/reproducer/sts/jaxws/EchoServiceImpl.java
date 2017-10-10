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

package org.jboss.reproducer.sts.jaxws;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebService;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import org.apache.cxf.annotations.EndpointProperties;
import org.apache.cxf.annotations.EndpointProperty;
import org.apache.cxf.interceptor.InInterceptors;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.interceptor.OutInterceptors;
import org.apache.cxf.interceptor.security.SimpleAuthorizingInterceptor;
import org.jboss.reproducer.sts.jaxws.generated.EchoService;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.wsf.stack.cxf.security.authentication.SubjectCreatingInterceptor;

/**
 * @author pjurak
 *
 */
@WebService(targetNamespace = "http://www.example.org/sts/", portName = "EchoServicePort", serviceName = "EchoService", wsdlLocation = "WEB-INF/echo.wsdl", endpointInterface = "org.jboss.reproducer.sts.jaxws.generated.EchoService")
@EndpointProperties(value = {
        // @EndpointProperty(key = "ws-security.signature.username", value = "myservicekey"),
        @EndpointProperty(key = "ws-security.signature.properties", value = "serviceKeystore.properties"),
        // @EndpointProperty(key = "ws-security.encryption.properties", value = "serviceKeystore.properties"),
        @EndpointProperty(key = "ws-security.callback-handler", value = "org.jboss.reproducer.sts.jaxws.ServerPasswordCallbackHandler") })
@InInterceptors(classes = { LoggingInInterceptor.class, SubjectCreatingInterceptor.class,
        SamlSecureAnnotationsInterceptor.class })
@OutInterceptors(classes = { LoggingOutInterceptor.class })
public class EchoServiceImpl implements EchoService {

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.reproducer.sts.jaxws.generated.Sts#echo(java.lang.String)
     */
    @Override
    @RolesAllowed("friend")
    public String echo(String in) {
        System.out.println("In trusted method: " + in);
        try {
            Subject subject = (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
            System.out.println("Found subject in web service call: " + subject);
            SecurityContext current = SecurityContextAssociation.getSecurityContext();
            System.out.println("Found security context in web service call: " + current);
            System.out.println("Security domain in web service call: " + current.getSecurityDomain());
            System.out.println("Subject info in web service call: " + current.getSubjectInfo());
        } catch (PolicyContextException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        return "Hello! " + in;
    }

}
