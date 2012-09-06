package gov.lanl.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;
/*
@author Lyudmila Balakireva
*/

public class ArchiveConfig {
	  
	  // public static final String DB_PATH = System.getProperty("ta.storage.basedir", "target/db" );    
	   private static Index idx;
	   public static   Map<String, String> prop;
	   public static List iplist;
	 //  public static String destroy;
	   public static  Index   getMetadataIndex () {
		   
		   if ( idx == null ) { 
	      try {
	    	  System.out.println("config");
	    	   prop = loadConfigFile( );
	    	   setIPlist();
	    	   if( prop.containsKey("ta.index")) {
			   //idx = (Index) Class.forName(System.getProperty( "ta.index", "gov.lanl.archive.index.bdb.IndexImplB")).newInstance();
	    		   String idxname=prop.get("ta.index");
	    		   idx = (Index) Class.forName(idxname).newInstance();
	    	   }
	    	   else {
	    		   idx = (Index) Class.forName("gov.lanl.archive.index.bdb.IndexImplB").newInstance();
	    	   }
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   }
		   return idx;
	   }
	   
	   public static void shutdown() {
		   if (idx!=null) {
		   idx.close();
		   }
	   }
	   /*
	   public static void setDestroy() {
		   destroy ="true";
	   }
	  */
	   private static void setIPlist() {
		    iplist = new ArrayList();
		    int i = 1;
		   if ( prop.containsKey("put.ip.1") ) {
			
			   do {
				  
			   iplist.add((String) prop.get("put.ip."+i));
			   i=i+1;
			   } while (prop.containsKey("put.ip."+i));
			   
		   }
		   
		   
	   }
	   
	   public static Map<String, String> loadConfigFile( )
	    {
		   //standalone server would not have init param if commented out 
	       // File configFile = new File( new File(  System.getProperty("ta.storage.basedir", "./db" ) ), "ta.properties" );
		  // File configFile = null;
	        
	       // if ( configFile.exists() )
	        //{
	            //System.out.println( "Using configuration "
	              //                  + configFile.getAbsolutePath() );
	            //return configFile.exists() ? loadConfigurations( configFile.getAbsolutePath() )
		          //      : new HashMap<String, String>();
	        //}
	        //else {
	        	  ClassLoader cl = ArchiveConfig.class.getClassLoader();

	        	  java.io.InputStream in;

	              if (cl != null) {
	                  in = cl.getResourceAsStream("ta.properties");
	              } else {
	                  in = ClassLoader.getSystemResourceAsStream("ta.properties");
	                  
	              }
	              System.out.println( "Using configuration from classpath");
               
	              return in!=null ? loadProperties( in )
	  	                : new HashMap<String, String>();
	        	  
	        //}
	      
	    } 
	   
	   public static Map<String,String> loadConfigurations( String file )
	       {
	   	        Properties props = new Properties();
	   	        try
	   	        {
	   	            FileInputStream stream = new FileInputStream( new File( file ) );
	   	            try
	   	            {
	   	                props.load( stream );
	   	            }
	   	            finally
	   	            {
	   	                stream.close();
	   	            }
	   	        }
	   	        catch ( Exception e )
	   	        {
	   	            throw new IllegalArgumentException( "Unable to load " + file, e );
	   	        }
	   	        Set<Entry<Object,Object>> entries = props.entrySet();
	   	        Map<String,String> stringProps = new HashMap<String,String>();
	   	        for ( Entry<Object,Object> entry : entries )
	   	        {
	   	            String key = (String) entry.getKey();
	   	            String value = (String) entry.getValue();
	   	            stringProps.put( key, value );
	   	            System.setProperty(key, value);
	   	        }
	   	        return stringProps;
	   	    }
	   
	   public static Map<String,String> loadProperties( InputStream stream)
       {
   	        Properties props = new Properties();
   	        try
   	        {
   	          
   	            try
   	            {
   	                props.load( stream );
   	            }
   	            finally
   	            {
   	                stream.close();
   	            }
   	        }
   	        catch ( Exception e )
   	        {
   	            throw new IllegalArgumentException( "Unable to load ta.properties", e );
   	        }
   	        Set<Entry<Object,Object>> entries = props.entrySet();
   	        Map<String,String> stringProps = new HashMap<String,String>();
   	        for ( Entry<Object,Object> entry : entries )
   	        {
   	            String key = (String) entry.getKey();
   	            String value = (String) entry.getValue();
   	            stringProps.put( key, value );
   	            System.setProperty(key, value);
   	        }
   	        return stringProps;
   	    }
	   
}
