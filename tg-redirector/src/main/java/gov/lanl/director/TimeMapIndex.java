package gov.lanl.director;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;

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
//static String filename;
static SimpleDateFormat httpformat;
	 static {
		 // InitServletContextListener cl= InitServletContextListener.getInstance();
		 //  filename = cl.getAttribute("path");
		  //  System.out.println("filename" +filename);
		    config = new AppConfig();
	   	    config.processConfig();
	   	    httpformat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
	   	    TimeZone tzo = TimeZone.getTimeZone("GMT");
	        httpformat.setTimeZone(tzo);
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
		// System.out.println("request url:"+ur.toString());
		 URI baseurl = ui.getBaseUri();
		// System.out.println("baseurl"+baseurl.toString());
		 String url = ur.toString().replaceFirst(baseurl.toString()+"timemap/", "");
		// System.out.println("get into get:"+url);
		
		
		           //config.processConfig();
		          // SortedMap map = config.getmap();
		          // List list = config.getList();
		           List tmlist = config.getTMList();
		           List ld = config.getList();
		           StringBuffer sb = new StringBuffer("<"+ url +">;rel=\"original\"\n");
		           sb.append (" , <"+baseurl.toString() +"timemap/" + url+">;rel=\"self \"; type=\"application/link-format\"");
		           String desc="";
		           for (int i=0;i<tmlist.size();i++) {
			    		String tg = (String) tmlist.get(i);
			    		tgconfig tc = (tgconfig) ld.get(i);
			    		 Date sd = tc.getStartdate();
			    		 Date ed = tc.getEnddate();
			    		 
			    		//if (i==0) {desc=" first";} else {desc="";}
			    		//if (i==(tmlist.size()-1)) desc=" last";
			    		if (i==(tmlist.size()-1)) {
			    			Date ed0 = new Date();
			    			if (ed.after(ed0)) {
			    				ed=ed0;
			    			}
			    		
			    		}
			    	 sb.append (" , <"+ tg  + url+">;rel=\"timemap"+desc+ "\"; from=\"" +httpformat.format(sd)+ "\"; until=\"" +httpformat.format(ed)+ "\"; type=\"application/link-format\"");
		           }
		           
		         ResponseBuilder r = Response.ok(sb.toString());
		   		
	             String timemap = " <"+baseurl.toString() +"timemap/" + url+">;anchor=\""+url+"\" ;rel=\"timemap\"; type=\"application/link-format\"";
                 r.header("Link", timemap );
                   
                 return  r.build();            
				    		
					    		
		                      
			   	
}
	
	
	
	
}