package gov.lanl.archive.resource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringUtils;

@Path("/timebundle/{id:.*}")
public class TimeBundleResource {

@GET


public Response getMap (@Context HttpHeaders hh, @Context UriInfo ui, @PathParam("id") String idp ) {
	
		 URI baseurl = ui.getBaseUri();
	
		 URI ur = ui.getRequestUri(); 
		 System.out.println("request url:"+ur.toString());
		
		 String id = ur.toString().replaceFirst(baseurl.toString()+"timebundle/", "");
		 System.out.println("get into get:"+id);
		 
		 
		 List<String> formatlist = hh.getRequestHeader("Accept");
		
		 List<String> mimeTypesSupported = Arrays.asList(StringUtils.split(
	                "application/rdf+xml,application/link-format", ','));
		 String url = baseurl.toString() +"timemap/rdf/"+id; 
		
				 
          if ( formatlist!=null) {
        	  
        	   System.out.println(formatlist.size()+formatlist.get(0));
        	   String bestmatch = MIMEParse.bestMatch(mimeTypesSupported,  formatlist.get(0));
        	   System.out.println("bestmatch"+bestmatch);
        	   if (bestmatch.equals("application/link-format")) {
            	   url = baseurl.toString() +  "timemap/link-format/"+id; 
            	   }
        	   
        	   if (bestmatch.equals("application/rdf+xml")) {
        	   url = baseurl.toString() +  "timemap/rdf/"+id; 
        	   }
        	   else {
        		   ResponseBuilder r = Response.status(406);
        		   return r.build();
        	   }
		  }
       
		
		 
		try {
		
			return Response.seeOther(new URI(url)).build();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	 }
		 
	 
	
	
}
