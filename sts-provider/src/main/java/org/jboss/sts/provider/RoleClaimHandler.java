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

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.xml.ws.WebServiceContext;

import org.apache.cxf.sts.claims.Claim;
import org.apache.cxf.sts.claims.ClaimCollection;
import org.apache.cxf.sts.claims.ClaimsHandler;
import org.apache.cxf.sts.claims.ClaimsParameters;
import org.apache.cxf.sts.claims.RequestClaim;
import org.apache.cxf.sts.claims.RequestClaimCollection;
import org.apache.ws.security.saml.ext.bean.AttributeBean;

/**
 * @author pjurak
 *
 */
public class RoleClaimHandler implements ClaimsHandler {

    /*
     * (non-Javadoc)
     *
     * @see org.apache.cxf.sts.claims.ClaimsHandler#getSupportedClaimTypes()
     */
    @Override
    public List<URI> getSupportedClaimTypes() {
        try {
            return Arrays.asList(new URI[] { new URI("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role") });
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.cxf.sts.claims.ClaimsHandler#retrieveClaimValues(org.apache.cxf.sts.claims.RequestClaimCollection,
     * org.apache.cxf.sts.claims.ClaimsParameters)
     */
    @Override
    public ClaimCollection retrieveClaimValues(RequestClaimCollection claims, ClaimsParameters parameters) {
        ClaimCollection claimsColl = new ClaimCollection();

        for (RequestClaim claim : claims) {
            if ("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role".equals(claim.getClaimType().toString())) {
                Claim c = new Claim();
                c.setClaimType(claim.getClaimType());
                c.setPrincipal(parameters.getPrincipal());
                // add roles (hardcoded)
                if ("alice".equalsIgnoreCase(parameters.getPrincipal().getName())) {
                    c.addValue("friend");
                } else {
                    c.addValue("");
                }
                claimsColl.add(c);
            } else {
                System.out.println("Unsupported claim " + claim.getClaimType() + " " + claim.getClaimValue());
            }
        }
        return claimsColl;
    }

}
