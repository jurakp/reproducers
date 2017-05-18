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

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.security.SecurityContext;
import org.apache.ws.security.SAMLTokenPrincipal;
import org.apache.ws.security.saml.ext.AssertionWrapper;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.apache.cxf.interceptor.security.AccessDeniedException;
import org.apache.cxf.interceptor.security.SecureAnnotationsInterceptor;

import org.w3c.dom.Element;

/**
 * @author pjurak
 *
 */
public class SamlSecureAnnotationsInterceptor extends SecureAnnotationsInterceptor {

    public SamlSecureAnnotationsInterceptor() {
        System.out.println("Creating SamlSecureAnnotationInterceptor instance.");
        Map<String, String> rolesMap = new HashMap<String, String>();
        findRoles(EchoServiceImpl.class, rolesMap);
        for (String method : rolesMap.keySet()) {
            System.out.printf("Added roles %s for method %s", rolesMap.get(method), method);
        }
        setMethodRolesMap(rolesMap);
    }

    @Override
    protected boolean isUserInRole(SecurityContext sc, List<String> roles, boolean deny) {
        System.out.println("Expected roles: " + roles);
        Principal principal = sc.getUserPrincipal();
        if (principal instanceof SAMLTokenPrincipal) {
            SAMLTokenPrincipal samlPrincipal = (SAMLTokenPrincipal) principal;
            AssertionWrapper assertion = samlPrincipal.getToken();
            String role = getRoleFromAssertion(assertion);
            System.out.println("Got role from assertion: " + role);
            if (roles.contains(role)) {
                System.out.printf("User %s is in role %s\n", samlPrincipal.getName(), role);
                return true;
            }
            System.out.printf("User %s not authorized \n", samlPrincipal.getName());
        }
        return false;
    }

    private String getRoleFromAssertion(AssertionWrapper assertion) {
        Assertion saml2Assertion = assertion.getSaml2();
        if (saml2Assertion == null) {
            return null;
        }

        List<AttributeStatement> attributeStatements = saml2Assertion.getAttributeStatements();
        if (attributeStatements == null || attributeStatements.isEmpty()) {
            return null;
        }

        String name = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role";
        String nameFormat = "urn:oasis:names:tc:SAML:2.0:attrname-format:uri";
        for (AttributeStatement statement : attributeStatements) {
            List<Attribute> attributes = statement.getAttributes();
            for (Attribute attribute : attributes) {
                System.out
                        .println("in getRoleFromAssertion " + attribute.getName() + " nameformat " + attribute.getNameFormat());
                if (name.equals(attribute.getName()) && nameFormat.equals(attribute.getNameFormat())) {
                    Element attributeValueElement = attribute.getAttributeValues().get(0).getDOM();
                    return attributeValueElement.getTextContent();
                }
            }
        }
        return null;
    }

}
