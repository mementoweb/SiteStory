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
import javax.ws.rs.HEAD;
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
@Path("/{id:.*}")

public class TimeGateDirector {


 

 //static MementoCommons mc;
 protected final URI baseUri;
 static DatatypeFactory dtf;
 static AppConfig config; 
//static String filename;
  
	 static {
		  //InitServletContextListener cl= InitServletContextListener.getInstance();
		   //filename = cl.getAttribute("path");
		    //System.out.println("filename" +filename);
		    config = new AppConfig();
	   	    config.processConfig();
	 }
 
	 public	TimeGateDirector( @Context UriInfo uriInfo )
	    {
	         this.baseUri = uriInfo.getBaseUri();
	       //  mc = new MementoCommons(baseUri);
	        // InputStream in = this.getClass().getClassLoader().getResourceAsStream("timegates.xml");
	   	    // config = new AppConfig();
	   	     
	   	    // config.processConfig(filename);
	    }
	   
	 

	 @HEAD
	 public Response  getHTimegate( @Context HttpHeaders hh, @Context UriInfo ui, @PathParam("id") String id ) throws ParseException, URISyntaxException {
		return getRTimegate( hh, ui, id );
	 }
	
	
	@GET
	// I may need to copy all  logic to @HEAD
	public Response  getRTimegate( @Context HttpHeaders hh, @Context UriInfo ui, @PathParam("id") String id ) throws ParseException, URISyntaxException {
		// URI u = ui.getAbsolutePath();
		// System.out.println("absolute path"+u.toString());
		 URI ur = ui.getRequestUri(); 
		 //System.out.println("request url:"+ur.toString());
		 URI baseurl = ui.getBaseUri();
		// System.out.println("baseurl"+baseurl.toString());
		 String url = ur.toString().replaceFirst(baseurl.toString(), "");
		 System.out.println("get into get:"+url);
 		
         String timemap = "<"+baseurl.toString() +"timemap/" + 
         url+">;rel=\"timemap index\"; type=\"application/link-format\"";

         String origlink ="<"+url+">;rel=\"original\"";
		           //config.processConfig();
		          SortedMap map = config.getmap();
		           List list = config.getList();
		 String tgurl=null ;
		 List <String> hdatetime = hh.getRequestHeader("Accept-Datetime");
		 try {
			dtf = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 Date date = null;
	    if (hdatetime ==null) { //"go to the last one" //should I sort
	    	tgurl = (String) map.get(map.lastKey());
	    	  ResponseBuilder r = Response.status(302);
	    	   r.header("Link", origlink+","+timemap );
               r.header("Location",tgurl+url);
              // r.header("Link",origlink);
             //  r.header("Vary","negotiate,accept-datetime");
             return  r.build();
	    	
	    }
	    else {
	    	 date = config.checkDtDateValidity(hdatetime.get(0));
	    	 
	    	   if ( date==null) {
		    	   ResponseBuilder r = Response.status(404);
		    	   return  r.build();
		      }
	    	 
	    	for (int i=0;i<list.size();i++) {
	    		tgconfig tg = (tgconfig) list.get(i);
	    		boolean check= tg.checkTimeGate(date, url);
	    		if (check==true) {
	    			tgurl=tg.getUrl();
	    			System.out.println("url found:"+tgurl);
	    			break;
	    		}
	    		
	    	}
	    }
		
	    
	    
	    if ( tgurl==null) {
	              if (date.before((Date) map.firstKey())) {
	    	         tgurl=(String) map.get(map.firstKey());
	              }
	             else {
	                //default //what if some gaps in coverage?
	    	        tgurl=(String) map.get(map.lastKey());
	              }
	    }
       				 		
	    System.out.println("tg:vary added");
		                         ResponseBuilder r = Response.status(302);
		                         
		                         r.header("Link", origlink +","+timemap );
		                         //r.header("Vary","negotiate,accept-datetime");
	                                       r.header("Location",tgurl+url);
	                                      // r.header("Link",origlink);
                                          //  r.header("TCN", "choice"); 
                                           // r.header("Link",origlink+ links +timemap +timebundle);
	                                      return  r.build();
			         
		  
			
	   
		  
	
}
	/*
	public boolean check_interval(String hdatetime,long reqtime, Map map) {
		// Map map = new HashMap();
		boolean intervalerror=false;
         if (hdatetime.indexOf(";")>0) {
			 
				String[] result = hdatetime.split("\\;");
		
				 if  (result.length <3 ) {
					 System.out.println("date parsinf length:" +result.length);
					 intervalerror = true; 
					//we are going to punish for ; after date
				 }
				     if (intervalerror==false) {
				           if (result[1].length()>0)
				           {   try {
					            String strdur = result[1];
					                    if (strdur.startsWith("-")) {
						                 strdur=strdur.substring(1);
					                     }
					                Duration dur = dtf.newDuration(strdur.trim());
					                Calendar cal = GregorianCalendar.getInstance() ;
					                long  ldur = dur.getTimeInMillis(cal);
					                long ltime = reqtime-ldur;
	                                 Date intdate1 =new Date(ltime);
                                     map.put("intdate1", intdate1);
					                  System.out.println("left"+intdate1);
				                    } catch (RuntimeException e) {
				    	            System.out.println(" interval in wrong ");
					                // TODO Auto-generated catch block
					                //what to do if duration is wrong
				    	            intervalerror = true;
					                // e.printStackTrace();
				                    }
				         }
				   if (result[2].length()>0)
				    {
					 try {
						  String strdur = result[2];
						  if (strdur.startsWith("+")) { strdur=strdur.substring(1);}
					      Duration dur = dtf.newDuration(strdur.trim());
					      Calendar cal = GregorianCalendar.getInstance() ;
					      long rdur = dur.getTimeInMillis(cal);
					
					      long rtime = reqtime+rdur;
					      Date intdate2 = new Date (rtime);
					      map.put("intdate2", intdate2);
					      System.out.println("right"+intdate2);
					     } catch (RuntimeException e) {
						 System.out.println(" interval in wrong ");
						  // TODO Auto-generated catch block
						  //what to do if duration is wrong
						 intervalerror=true;
						 //e.printStackTrace();
					     }
				     } //if
				   
				 }//if "true"
				 		 			 
				 
         }//if
         return intervalerror;
	}
	*/
	
}