/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.quickstarts.ws.jaxws.samples.jsr181pojo;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

/**
*
* @author rsearls@redhat.com
*/

/**
 *
 * @author aollebla@redhat.com
 * 
 * Based on jaxws-pojo: An POJO JAX-WS Web Service quickstart 
 * https://github.com/wildfly/quickstart/tree/master/jaxws-pojo
 * 
 * adapted to reproduce the JBEAP-16517 issue.
 * 
 * Steps:
 *
 * Invoke the client via:
 *
 * 
 * Open a terminal and navigate into the client directory of this quickstart.
 * 
 * $ cd client/
 * 
 * Type this command to run the client.
 * 
 * $ mvn exec:java
 * 
 * 
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class JSEBean {
	@WebMethod
	public String echo(String input) throws SOAPFaultException {
//		try {
			SOAPFaultException ex;
			try {
				ex = wrapToSoapFault(new Exception("hello"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				return e.toString();
			}
			throw ex;
//		} catch (Exception e) {
//			return e.toString();
//		}
//		return "<>JSEBean pojo: " + input;

	}

	private SOAPFaultException wrapToSoapFault(Exception ex) throws Exception {
//		String methodName = "wrapToSoapFault";
		SOAPFactory fac = null;
		try {
			fac = SOAPFactory.newInstance();
			String message = ex.getMessage();
			SOAPFault sf = fac.createFault(message, new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Client"));
			// logger.severe(methodName + ": SOAPFaultException is created message: [" +
			// message + "]");
			sf.setFaultString(message);


//			if(sf.hasDetail())
//				sf.getDetail();
			
			// hangs EAP
			sf.addDetail().setAttribute("message", message);

			//////////////////////////////////

			return new SOAPFaultException(sf);
		} catch (Exception e2) {
			// logger.severe(methodName + ": caught unexpected exception: " +
			// e2.getMessage());
			throw e2;
		}
	}

}
