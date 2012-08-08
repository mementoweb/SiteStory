package gov.lanl.archive.resource;

import java.util.Set;

import gov.lanl.archive.ArchiveConfig;
import gov.lanl.archive.webapp.MementoServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import com.sun.jersey.spi.resource.Singleton;

@Singleton

public class MyWebApplication extends Application {

	
	 //ServletConfig sc;
	 @PostConstruct 
	   public void postConstruct(){
		    //  if  (sc.getServletContext().getInitParameter("ta.storage.basedir")!=null) {
  	    //System.setProperty( "ta.storage.basedir", sc.getServletContext().getInitParameter("ta.storage.basedir") );
		  //  System.out.println("dir called"+ sc.getServletContext().getInitParameter("ta.storage.basedir"));   
  	    //}
		      //System.out.println("postconstruct called from webapp");
		     // ArchiveConfig.getMetadataIndex();
		     
		   
	   }
	   
	 
	 /*
	 @PreDestroy
	  public void preDestroy() {
		   System.out.println("closing index");
		   ArchiveConfig.setDestroy();
		   ArchiveConfig.shutdown();
		  
      }
      */
	/* 
	 @GET
		public String sayHello() {
			return "Hello Jersey";
		}
		*/
}
