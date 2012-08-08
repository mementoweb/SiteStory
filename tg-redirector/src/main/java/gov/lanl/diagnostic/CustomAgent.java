package gov.lanl.diagnostic;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;


 // This CustomAgent will start an RMI COnnector Server using only
 // port "example.rmi.agent.port".
 //
 //
 //
public class CustomAgent {

    private CustomAgent() { }

    public static void premain(String agentArgs) 
	throws IOException {

        // Ensure cryptographically strong random number generator used
        // to choose the object number - see java.rmi.server.ObjID
        //
        System.setProperty("java.rmi.server.randomIDs", "true");

        // Start an RMI registry on port specified by example.rmi.agent.port
        // (default 3000).
        //
        final int port= Integer.parseInt(
                System.getProperty("gov.lanl.diagnostic.port","8086"));
        System.out.println("Create RMI registry on port "+port);
        LocateRegistry.createRegistry(port);

        // Retrieve the PlatformMBeanServer.
        //
        System.out.println("Get the platform's MBean server");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        // Environment map.
        //
        System.out.println("Initialize the environment map");
        HashMap<String,Object> env = new HashMap<String,Object>();
        
        // This where we would enable security - left out of this
        // for the sake of the example....
        //

        // Create an RMI connector server.
        //
        // As specified in the JMXServiceURL the RMIServer stub will be
        // registered in the RMI registry running in the local host on
        // port 3000 with the name "jmxrmi". This is the same name the
        // out-of-the-box management agent uses to register the RMIServer
        // stub too.
        //
        // The port specified in "service:jmx:rmi://"+hostname+":"+port
        // is the second port, where RMI connection objects will be exported.
        // Here we use the same port as that we choose for the RMI registry. 
        // The port for the RMI registry is specified in the second part
        // of the URL, in "rmi://"+hostname+":"+port
        //
        System.out.println("Create an RMI connector server");
        final String hostname = InetAddress.getLocalHost().getHostName();
        //System.out.println("hostname"+hostname);
        String h = "theresourcedepot.org";
        JMXServiceURL url =
            new JMXServiceURL("service:jmx:rmi://"+hostname+
            ":"+port+"/jndi/rmi://"+hostname+":"+port+"/jmxrmi");
        System.out.println("h"+"service:jmx:rmi://"+hostname+
                ":"+port+"/jndi/rmi://"+hostname+":"+port+"/jmxrmi");
        // Now create the server from the JMXServiceURL
        //
        JMXConnectorServer cs =
            JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);

        // Start the RMI connector server.
        //
        System.out.println("Start the RMI connector server on port "+port);
        cs.start();
    }
}