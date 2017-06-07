package jboss.remoting_jmx;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @see <a href="https://docs.jboss.org/author/display/AS71/JMX+subsystem+configuration">https://docs.jboss.org/author/display/AS71/JMX+subsystem+configuration</a>
 */
public class App {

  private static final String DEFAULT_HOST = "localhost";
  private static final int DEFAULT_PORT = 9999;
  private static final String DEFAULT_JMX_SERVICE_URL = "service:jmx:remoting-jmx://" + DEFAULT_HOST + ":" + DEFAULT_PORT;

  public static void main(String[] args) throws Exception {
    JMXServiceURL url = new JMXServiceURL(
      System.getProperty("jmx.service.url", DEFAULT_JMX_SERVICE_URL));

    Map map = new HashMap();
    String[] credentials = new String[] { "admin", "AdminAdmin1!" };
    map.put("jmx.remote.credentials", credentials);
    
    JMXConnector connector = JMXConnectorFactory.connect(url, map);
    // JMXConnector connector = JMXConnectorFactory.connect(url, null);
    MBeanServerConnection connection = connector.getMBeanServerConnection();

    String version = (String) connection.getAttribute(
      new ObjectName("jboss.as:management-root=server"), "productVersion");

    System.out.println(version);

    System.out.println("Sleeping 10 sec...");
    TimeUnit.SECONDS.sleep(10);
    System.out.println("Closing...");
    connector.close();
  }

}
