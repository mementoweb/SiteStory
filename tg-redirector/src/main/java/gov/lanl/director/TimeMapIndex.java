package gov.lanl.director;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
/*
@author Lyudmila Balakireva

*/
@Path("/timemap/{id:.*}")

public class TimeMapIndex {


 

 //static MementoCommons mc;
 protected final URI baseUri;
 static DatatypeFactory dtf;
 static AppConfig config; 
static String filename;
  
	 static {
		  InitServletContextListener cl= InitServletContextListener.getInstance();
		   filename = cl.getAttribute("path");
		    System.out.println("filename" +filename);
		    config = new AppConfig();
	   	    config.processConfig(filename);
	 }
 
	 public	TimeMapIndex( @Context UriInfo uriInfo )
	    {
	         this.baseUri = uriInfo.getBaseUri();
	       //  mc = new MementoCommons(baseUri);
	        // InputStream in = this.getClass().getClassLoader().getResourceAsStream("timegates.xml");
	   	    // config = new AppConfig();
	   	     
	   	    // config.processConfig(filename);
	    }
	   
	 
	 
	
	
	@GET
	// I may need to copy all  logic to @HEAD
	public Response  getTimegate( @Context HttpHeaders hh, @Context UriInfo ui, @PathParam("id") String id ) throws ParseException, URISyntaxException {
		// URI u = ui.getAbsolutePath();
		// System.out.println("absolute path"+u.toString());
		 URI ur = ui.getRequestUri(); 
		 System.out.println("request url:"+ur.toString());
		 URI baseurl = ui.getBaseUri();
		 System.out.println("baseurl"+baseurl.toString());
		 String url = ur.toString().replaceFirst(baseurl.toString()+"timemap/", "");
		 System.out.println("get into get:"+url);
		
		
		           //config.processConfig();
		          // SortedMap map = config.getmap();
		          // List list = config.getList();
		           List tmlist = config.getTMList();
		           StringBuffer sb = new StringBuffer("<"+ url +">;rel=\"original\"\n");
		           sb.append (" , <"+baseurl.toString() +"timemap/" + url+">;rel=\"self timemap index\"; type=\"application/link-format\"");
		           String desc="";
		           for (int i=0;i<tmlist.size();i++) {
			    		String tg = (String) tmlist.get(i);
			    		if (i==0) {desc=" first";} else {desc="";}
			    		if (i==(tmlist.size()-1)) desc=" last";
			    	 sb.append (" , <"+ tg  + url+">;rel=\"timemap"+desc+ "\"; type=\"application/link-format\"");
		           }
		           
		         ResponseBuilder r = Response.ok(sb.toString());
		   		
	             String timemap = " <"+baseurl.toString() +"timemap/" + url+">;anchor=\""+url+"\" ;rel=\"self timemap\"; type=\"application/link-format\"";
                 r.header("Link", timemap );
                   
                 return  r.build();            
				    		
					    		
		                      
			   	
}
	
	
	
	
}