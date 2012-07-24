package gov.lanl.archive.webapp;



import gov.lanl.archive.ArchiveConfig;
import gov.lanl.archive.resource.MyServletContextListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import java.util.logging.Logger;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public enum MementoServer {
	INSTANCE;
	
	public static final int DEFAULT_PORT = 9999;
   private GrizzlyWebServer server;
    private SelectorThread threadSelector;
    private int port = DEFAULT_PORT;
    public static final int DEFAULT_MAX_THREADS = 128;
    public static final int DEFAULT_MIN_THREADS = 5;

    public static final String CONFIG_MIN_THREADS = "rest.min.grizzly.threads";
    public static final String CONFIG_MAX_THREADS = "rest.max.grizzly.threads";
    private int minThreads = DEFAULT_MIN_THREADS;
    private int maxThreads = DEFAULT_MAX_THREADS;
    private static final Logger logger = Logger.getLogger(MementoServer.class.getName());
    public void startServer() {
        startServer( DEFAULT_PORT);
        
    }
    
    public void startServer( int port  ) {
        this.port = port;
        //final HashMap<String, String> initParams = new HashMap<String, String>();
        //initParams.put("com.sun.jersey.config.property.packages", "gov.lanl.archive.resource");

        try {
        	
        	server = new GrizzlyWebServer( port );
        	Map<String, String> prop = ArchiveConfig.loadConfigFile();
        	
        	   // Set max/min threads
               setThreadLimits(prop);
        	// server.setMaxThreads(  DEFAULT_MAX_THREADS );
            // server.setCoreThreads( DEFAULT_MIN_THREADS );
            // Create our REST service
            ServletAdapter jersey = new ServletAdapter();
            jersey.setHandleStaticResources(true);
            jersey.setServletInstance( new ServletContainer() );

            // Tell jersey where to find REST resources
            jersey.addInitParameter( "com.sun.jersey.config.property.packages",
                    "gov.lanl.archive.resource" );
            jersey.setServletPath( "" );
           // jersey.addServletContextListener(new MyServletContextListener());
            jersey.addServletListener(MyServletContextListener.class.getName());
            server.addGrizzlyAdapter( jersey );
          
            
          
            server.start();
        	
        	/*
        	 System.out.println("Starting grizzly...");
        	 logger.info("Starting Loggin...");
			 threadSelector = GrizzlyWebContainerFactory.create(getLocalhostBaseUri( port ), initParams);
			 GrizzlyAdapter adapter  = (GrizzlyAdapter) threadSelector.getAdapter();
			 adapter.setHandleStaticResources(true); 
			 */
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
    
    public String getBaseUri()
    {
        return getLocalhostBaseUri( port );
    }
  /*  
    public void stopServer() {
        threadSelector.stopEndpoint();
    }
    */
    public void stopServer()
    {
        server.stop();
    }
    public static String getLocalhostBaseUri()
    {
        return getLocalhostBaseUri( DEFAULT_PORT );
    }
    
    public static String getLocalhostBaseUri( int port )
    {
        return "http://localhost:" + port + "/";
    }
    
   /* Check if there are thread limits set in the config file, use them if they
    * are integers. Otherwise fall back to defaults.
    */
    
    
 
   private void setThreadLimits(Map prop)
   {
	   
       if ( prop.containsKey(CONFIG_MIN_THREADS ) )
       {
           try
           {
               minThreads = Integer.valueOf( (String) prop.get(CONFIG_MIN_THREADS ) );
           }
           catch ( Exception e )
           {
           }
       }

       if ( prop.containsKey( CONFIG_MAX_THREADS ) )
       {
           try
           {
               maxThreads = Integer.valueOf( (String) prop.get(CONFIG_MAX_THREADS ) ); 
               System.out.println("test" +  maxThreads);
           }
           catch ( Exception e )
           {
           }
       }

       // Ensure min threads is never larger than max threads
       minThreads = maxThreads >= minThreads ? minThreads : maxThreads;
       System.out.println("maxthreads"+ maxThreads);
       System.out.println("maxthreads"+ minThreads);
       server.setMaxThreads( maxThreads );
       server.setCoreThreads( minThreads );
   }
    
    
}
