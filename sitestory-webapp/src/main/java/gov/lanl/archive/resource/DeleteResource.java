package gov.lanl.archive.resource;

import gov.lanl.archive.ArchiveConfig;
import gov.lanl.archive.Index;
import gov.lanl.archive.CallBack;
import gov.lanl.archive.location.DeleteCallBack;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/*
@author Lyudmila Balakireva
*/
@Path("/delete/{date}/{id:.*}")

public class DeleteResource {
	 private static Index idx;
	 private MementoCommons mc;	
	 protected final URI baseUri;
	 private CallBack callback;
		 static {			 
				 idx = ArchiveConfig.getMetadataIndex();			
		 }
	
		  public	DeleteResource( @Context UriInfo uriInfo )
		    {
		        this.baseUri = uriInfo.getBaseUri();
		        mc = new MementoCommons(baseUri);
		        callback = new DeleteCallBack();
		            System.out.println("init");
		    }
	
	@GET

    public Response DeleteSelected(@PathParam("idp") String id,@PathParam("date") String date, @Context UriInfo uriInfo ) {
	  try {
		  String mydate = null;
		  URI ur = uriInfo.getRequestUri(); 
			 //System.out.println("request url:"+ur.toString());
			
			  id = ur.toString().replaceFirst(baseUri.toString()+"delete/" +date +"/", "");
			 System.out.println("get into get:"+id);
		  if (date.equals("*")) {
			  Date ndate = new Date();
			  
			  //date = null; //so changed logic not deleting everything, but  untill now 
			  mydate = Long.toString(ndate.getTime());
			  System.out.println("deleting by id" +id + "until"  + ndate);
		  }
		  else {
			 Date resourcedate = mc.checkMementoDateValidity(date);
			 if (resourcedate==null) return Response.status(404).build();
			 mydate = Long.toString(resourcedate.getTime());
		  }
		  
		  if (id.equals("*")) { id = null; 
		 
		  System.out.println("deleting all urls until  date" + mydate);  
		  }
		  
		 //unix format expecting
		  
		  idx.delete(id,mydate,callback);
		  
		  return  Response.status(200).build();	
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	  
	  
    return  Response.status(505).build();	
    }
}
