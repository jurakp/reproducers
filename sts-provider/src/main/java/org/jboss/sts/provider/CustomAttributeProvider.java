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

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.ws.WebServiceContext;

import org.apache.cxf.sts.claims.Claim;
import org.apache.cxf.sts.claims.ClaimCollection;
import org.apache.cxf.sts.claims.ClaimsManager;
import org.apache.cxf.sts.claims.ClaimsParameters;
import org.apache.cxf.sts.request.ReceivedToken;
import org.apache.cxf.sts.request.TokenRequirements;
import org.apache.cxf.sts.token.provider.AttributeStatementProvider;
import org.apache.cxf.sts.token.provider.TokenProviderParameters;
import org.apache.cxf.ws.security.sts.provider.STSException;
import org.apache.cxf.ws.security.sts.provider.model.secext.UsernameTokenType;
import org.apache.ws.security.SAMLTokenPrincipal;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.saml.ext.AssertionWrapper;
import org.apache.ws.security.saml.ext.bean.AttributeBean;
import org.apache.ws.security.saml.ext.bean.AttributeStatementBean;
import org.w3c.dom.Element;

/**
 * @author pjurak
 *
 */
public class CustomAttributeProvider implements AttributeStatementProvider {

    @Override
    public AttributeStatementBean getStatement(TokenProviderParameters providerParameters) {
        List<AttributeBean> attributeList = new ArrayList<AttributeBean>();

        TokenRequirements tokenRequirements = providerParameters.getTokenRequirements();
        String tokenType = tokenRequirements.getTokenType();
        Principal principal = providerParameters.getPrincipal();
        WebServiceContext wsctx = providerParameters.getWebServiceContext();

        // Handle Claims
        ClaimsManager claimsManager = providerParameters.getClaimsManager();
        ClaimCollection retrievedClaims = new ClaimCollection();

        if (claimsManager != null) {
            ClaimsParameters params = new ClaimsParameters();
            params.setAdditionalProperties(providerParameters.getAdditionalProperties());
            params.setAppliesToAddress(providerParameters.getAppliesToAddress());
            params.setEncryptionProperties(providerParameters.getEncryptionProperties());
            params.setKeyRequirements(providerParameters.getKeyRequirements());
            params.setPrincipal(providerParameters.getPrincipal());
            params.setRealm(providerParameters.getRealm());
            params.setStsProperties(providerParameters.getStsProperties());
            params.setTokenRequirements(providerParameters.getTokenRequirements());
            params.setTokenStore(providerParameters.getTokenStore());
            params.setWebServiceContext(providerParameters.getWebServiceContext());
            retrievedClaims = claimsManager.retrieveClaimValues(providerParameters.getRequestedPrimaryClaims(),
                    providerParameters.getRequestedSecondaryClaims(), params);
        }

        System.out.println("Attributes: principal " + principal.getName());
        System.out.println("Received claims: " + retrievedClaims);
        AttributeStatementBean attrBean = new AttributeStatementBean();
        Iterator<Claim> claimIterator = retrievedClaims.iterator();
        if (!claimIterator.hasNext()) {
            // If no Claims have been processed then create a default attribute
            AttributeBean attributeBean = createDefaultAttribute(tokenType);
            attributeList.add(attributeBean);
        }

        while (claimIterator.hasNext()) {
            Claim claim = claimIterator.next();
            AttributeBean attributeBean = createAttributeFromClaim(claim, tokenType);
            attributeList.add(attributeBean);
        }

        ReceivedToken onBehalfOf = tokenRequirements.getOnBehalfOf();
        ReceivedToken actAs = tokenRequirements.getActAs();
        try {
            if (onBehalfOf != null) {
                AttributeBean parameterBean = handleAdditionalParameters(false, onBehalfOf.getToken(), tokenType);
                if (!parameterBean.getAttributeValues().isEmpty()) {
                    attributeList.add(parameterBean);
                }
            }
            if (actAs != null) {
                AttributeBean parameterBean = handleAdditionalParameters(true, actAs.getToken(), tokenType);
                if (!parameterBean.getAttributeValues().isEmpty()) {
                    attributeList.add(parameterBean);
                }
            }
        } catch (WSSecurityException ex) {
            throw new STSException(ex.getMessage(), ex);
        }

        attrBean.setSamlAttributes(attributeList);

        return attrBean;
    }

    /**
     * Create a default attribute
     */
    private AttributeBean createDefaultAttribute(String tokenType) {
        AttributeBean attributeBean = new AttributeBean();

        if (WSConstants.WSS_SAML2_TOKEN_TYPE.equals(tokenType) || WSConstants.SAML2_NS.equals(tokenType)) {
            attributeBean.setQualifiedName("token-requestor");
            attributeBean.setNameFormat("http://cxf.apache.org/sts/custom");
        } else {
            attributeBean.setSimpleName("token-requestor");
            attributeBean.setQualifiedName("http://cxf.apache.org/sts/custom");
        }

        attributeBean.setAttributeValues(Collections.singletonList("authenticated"));

        return attributeBean;
    }

    /**
     * Handle ActAs or OnBehalfOf elements.
     */
    private AttributeBean handleAdditionalParameters(boolean actAs, Object parameter, String tokenType)
            throws WSSecurityException {
        AttributeBean parameterBean = new AttributeBean();

        String claimType = actAs ? "CustomActAs" : "CustomOnBehalfOf";
        if (WSConstants.WSS_SAML2_TOKEN_TYPE.equals(tokenType) || WSConstants.SAML2_NS.equals(tokenType)) {
            parameterBean.setQualifiedName(claimType);
            parameterBean.setNameFormat("http://cxf.apache.org/sts/custom/" + claimType);
        } else {
            parameterBean.setSimpleName(claimType);
            parameterBean.setQualifiedName("http://cxf.apache.org/sts/custom/" + claimType);
        }
        if (parameter instanceof UsernameTokenType) {
            parameterBean
                    .setAttributeValues(Collections.singletonList(((UsernameTokenType) parameter).getUsername().getValue()));
        } else if (parameter instanceof Element) {
            AssertionWrapper wrapper = new AssertionWrapper((Element) parameter);
            SAMLTokenPrincipal principal = new SAMLTokenPrincipal(wrapper);
            parameterBean.setAttributeValues(Collections.singletonList(principal.getName()));
        }

        return parameterBean;
    }

    /**
     * Create an Attribute from a claim.
     */
    private AttributeBean createAttributeFromClaim(Claim claim, String tokenType) {
        AttributeBean attributeBean = new AttributeBean();
        if (WSConstants.WSS_SAML2_TOKEN_TYPE.equals(tokenType) || WSConstants.SAML2_NS.equals(tokenType)) {
            attributeBean.setQualifiedName(claim.getClaimType().toString());
        } else {
            attributeBean.setSimpleName(claim.getClaimType().toString());
        }
        attributeBean.setAttributeValues(claim.getValues());

        return attributeBean;
    }
}
