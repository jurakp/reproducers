package org.picketlink.identity.federation.core.wstrust.app;

import org.jboss.logging.Logger;
import org.picketlink.identity.federation.core.wstrust.PicketLinkSTS;

import javax.annotation.Resource;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;

@WebServiceProvider(serviceName = "PicketLinkSTS", portName = "PicketLinkSTSPort", targetNamespace = "urn:picketlink:identity-federation:sts", wsdlLocation = "WEB-INF/wsdl/PicketLinkSTS.wsdl")
@ServiceMode(value = Service.Mode.MESSAGE)
public class PicketLinkSTSService extends PicketLinkSTS implements Provider<SOAPMessage> {

    private static Logger log = Logger.getLogger(PicketLinkSTSService.class.getName());

    @Resource
    public void setWSC(WebServiceContext wctx) {
        log.debug("Setting WebServiceContext = " + wctx);
        this.context = wctx;
    }
}