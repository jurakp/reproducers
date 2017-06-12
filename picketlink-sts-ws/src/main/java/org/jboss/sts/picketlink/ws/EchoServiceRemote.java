package org.jboss.sts.picketlink.ws;

import javax.ejb.Remote;
import javax.jws.WebService;

@Remote
@WebService
public interface EchoServiceRemote {

    void echo(String echo);
}
