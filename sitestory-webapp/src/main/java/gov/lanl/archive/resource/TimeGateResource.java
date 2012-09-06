package gov.lanl.archive.resource;

import gov.lanl.archive.ArchiveConfig;
import gov.lanl.archive.Index;
import gov.lanl.archive.Memento;




import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
@Path("/timegate/{id:.*}")
public class TimeGateResource {


 private static Index idx;
 //static SimpleDateFormat  httpformatter;
 static ThreadSafeSimpleDateFormat formatterout;
 static ThreadSafeSimpleDateFormat  httpformatter;
 static MementoCommons mc;
 protected final URI baseUri;
 static DatatypeFactory dtf;
 //static String indextimemap;
	 static {
		 
			 idx = ArchiveConfig.getMetadataIndex();
			 TimeZone tz = TimeZone.getTimeZone("UTC");
			 httpformatter = new ThreadSafeSimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");	
			 //indextimemap = System.getProperty( "ta.indextimemap");
		        TimeZone tzo = TimeZone.getTimeZone("GMT");
		        httpformatter.setTimeZone(tzo);
		        formatterout = new ThreadSafeSimpleDateFormat("yyyyMMddHHmmss");
		        formatterout.setTimeZone(tz);
		        MyServletContextListener cl= MyServletContextListener.getInstance();
			    cl.setAttribute("idx", idx);
		        
	 }
	 
	 
	 
	 public	TimeGateResource( @Context UriInfo uriInfo )
	    {
	        this.baseUri = uriInfo.getBaseUri();
	        mc = new MementoCommons(baseUri);
	            System.out.println("init");
	    }
	   
	 
	 @POST
	
	 public Response replytoPOST() {
		 ResponseBuilder r = Response.status(405);
		  r.header("Allow", "GET,HEAD"); 
		  r.header("Vary","negotiate,accept-datetime");
		 return r.build();
	 }
	 
	 @PUT
	 public Response replytoPUT() {
		 ResponseBuilder r = Response.status(405);
		  r.header("Allow", "GET,HEAD"); 
		  r.header("Vary","negotiate,accept-datetime");
		 return r.build();
	 }
	 
	 @DELETE
	 public Response replytoDELETE() {
		 ResponseBuilder r = Response.status(405);
		  r.header("Allow", "GET,HEAD"); 
		  r.header("Vary","negotiate,accept-datetime");
		 return r.build();
	 }
	 
	 
	 @HEAD
	 public Response  getHTimegate( @Context HttpHeaders hh, @Context UriInfo ui, @PathParam("id") String id,@Context UriInfo uriInfo ) throws ParseException, URISyntaxException {
		return getTimegate( hh, ui, id,uriInfo );
	 }
	 
	@GET
	// I may need to copy all  logic to @HEAD
	public Response  getTimegate( @Context HttpHeaders hh, @Context UriInfo ui, @PathParam("id") String idp,@Context UriInfo uriInfo ) throws ParseException, URISyntaxException {
		 URI baseurl = ui.getBaseUri();
		 URI ur = uriInfo.getRequestUri(); 
		 //System.out.println("request url:"+ur.toString());
		
		 String id = ur.toString().replaceFirst(baseUri.toString()+"timegate/", "");
		 System.out.println("get into get:"+id);
		 List <String> hdatetime = hh.getRequestHeader("Accept-Datetime");
		 try {
			dtf = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	
		
		 Date date = idx.getRecent(id);
		 System.out.println("recent date"+ date);
		    if (date==null) {
		    	  //bad url-id  precedence
		    	 ResponseBuilder r = Response.status(404);
		    	   r.header("Vary","negotiate,accept-datetime");
			       return r.build();
		    }
		   
		 if (hdatetime==null) {
			 hdatetime = new ArrayList();
			
			 // no datetime header case set date to recent
			 
			// Date date = idx.getRecent(id);
			  //  if (date==null) {
			    	  //bad url-id 
			    //	 ResponseBuilder r = Response.status(404);
			    	//   r.header("Vary","negotiate,accept-datetime");
				      // return r.build();
			    //}
			 System.out.println("formater out"+ httpformatter.format(date));
			
			 hdatetime.add(0,   httpformatter.format(date));
		   }
		
		 
		 String origlink ="<"+id+">;rel=\"original\"";
      	 String timemap = " , <"+baseurl.toString() +"timemap/link/" + id+">;rel=\"timemap\"; type=\"application/link-format\"";
		// String timebundle = " , <"+baseurl.toString() +"timebundle/" + id+">;rel=\"timebundle\"";
		 //if (indextimemap!=null) {
	       //    timemap=" , <"+indextimemap+ id+">;rel=\"timemap\"; type=\"application/link-format\"";           
			// }
		 
		  //if (datetime!=null) {
			  Date dtdate = mc.checkDtDateValidity(hdatetime.get(0)) ;
			   		  
			             if (dtdate==null) {
			            	 
			               Memento m = idx.get(id, date);
			               System.out.println("wrong date");
			               String links = mc.composeLinkHeader(m.getAccessdate(),m.getLastMemento().getAccessdate(),m.getFirstMemento().getAccessdate(),id);
			                ResponseBuilder r = Response.status(400);
				          
				           r.header("Vary","negotiate,accept-datetime");
                          // r.header("TCN", "list"); 
                           r.header("Link",origlink  +timemap +links);
				          //bad not parsable date
					        return r.build();
			               }
			  
			             long reqtime = dtdate.getTime();
			             
			             //check if user specified interval of validity
			             
			             Map intervalmap = new HashMap();
			             boolean intervalerror = check_interval(hdatetime.get(0),reqtime, intervalmap);
			             
			          
			             if (intervalerror) {
			            	  Memento m = idx.get(id, date);
				               System.out.println("interval problem date");
				               String links = mc.composeLinkHeader(m.getAccessdate(),m.getLastMemento().getAccessdate(),m.getFirstMemento().getAccessdate(),id);
				                ResponseBuilder r = Response.status(400);
					           r.header("Vary","negotiate,accept-datetime");
	                           r.header("Link",origlink  +timemap +links);
					          //bad not parsable interval date
						        return r.build();
			            	  
			              }
			             
			              
					             Memento m = idx.get(id, dtdate);
					      					             
					             
					             String location = baseurl.toString()+"memento/" +formatterout.format(m.getAccessdate())+"/" +id;
					             
					    		 String links = mc.composeLinkHeader(m.getAccessdate(),m.getLastMemento().getAccessdate(),m.getFirstMemento().getAccessdate(),id);
					    		 StringBuffer sb = new StringBuffer(links);
					    		 // String mem =  mc.composeLink(m.getAccessdate(),id,"memento"); 
					    		 
					    		
					    		 //String nextmem="";
					    		 if (m.getNextMemento()!= null) {
					    				if (!(m.getNextMemento().getAccessdate().equals(m.getFirstMemento().getAccessdate()))) {
					    					String next = mc.composeLink(m.getNextMemento().getAccessdate(),id,"memento next");
					    				    sb.append(next);   }
					    			    else {
					    			    	int m_index = sb.lastIndexOf("\"first\"");
					    		  			sb.insert(m_index + 1, "prev ");
					    			    }
					    		 }
					    				
					    				
					    		 
					    		 if (m.getPrevMemento()!= null) {
					    			 
					    		      if (!(m.getPrevMemento().getAccessdate().equals(m.getLastMemento().getAccessdate()))){
					    			 
					    		    	  String prevmem =  mc.composeLink(m.getPrevMemento().getAccessdate(),id,"memento prev");
					    	           sb.append(prevmem);
					    		      }
					    	           else {
					    					int m_index = sb.lastIndexOf("\"last\"");
					    		  			sb.insert(m_index + 1, "next  ");
					    				}

					    		 }		
					       
					    		
					    		links=sb.toString();
					    		 
					    		//memento out of interval requested
					    		if (intervalmap.containsKey("intdate1")&&intervalmap.containsKey("intdate2")) {
					    			Date intdate1=(Date) intervalmap.get("intdate1");
					    			Date intdate2=(Date) intervalmap.get("intdate2");
					    			
					    			if  (m.getAccessdate().before(intdate1)|| m.getAccessdate().after(intdate2) ) {
					    				//nuzno podumat' mozet next memento not defined
					    				  //case when next memento part of interval but usual memento not
					    				 if (m.getNextMemento()!= null) {
					    				              if(  m.getNextMemento().getAccessdate().after(intdate2)){
					    				                   ResponseBuilder r = Response.status(406);
					    				                                   r.header("Vary","negotiate,accept-datetime");
					    				                                   r.header("Link",origlink+ links +timemap );
	                                                                       return  r.build();
					    				               }
					    				                else {
					    					
					    					              ResponseBuilder r = Response.status(302);
					    					              String chlocation = baseurl.toString()+"memento/" +formatterout.format(m.getNextMemento().getAccessdate())+"/" +id;
					    					              String chlinks = mc.composeLinkHeader( m.getNextMemento().getAccessdate(),m.getLastMemento().getAccessdate(),m.getFirstMemento().getAccessdate(),id);	
					    					              StringBuffer chsb = new StringBuffer(chlinks);
					    					              //need to check this it seems mad
					    					           //   if (!(m.getAccessdate().equals(m.getLastMemento().getAccessdate()))){
					    						    			 
										    		    //	  String prevmem =  mc.composeLink(m.getAccessdate(),id,"memento prev");
										    	          // chsb.append(prevmem);
										    		      //}
										    	           //else {
										    				//	int m_index = chsb.lastIndexOf("\"last\"");
										    		  			//chsb.insert(m_index + 1, "next  ");
										    				//}
					    					              chlinks=chsb.toString();
					    					              r.header("Location",chlocation);
					    					              r.header("Vary","negotiate,accept-datetime");
					    					              r.header("Link",origlink+ chlinks +timemap );
		                                                  return  r.build();
				         	  
					    				                 }
					    				 }
					    				 else {
					    					 
					    					  ResponseBuilder r = Response.status(406);
			                                   r.header("Vary","negotiate,accept-datetime");
			                                   r.header("Link",origlink+ links +timemap );
			 
                                              return  r.build();
					    					 
					    					 
					    				 }
					    				 
					    				 
					    				 
					    			}
					    			
					    		}
					    		
					    		
					    		
		                         ResponseBuilder r = Response.status(302);
	                                        r.header("Location",location);
	                                        r.header("Vary","negotiate,accept-datetime");
                                          //  r.header("TCN", "choice"); 
                                            r.header("Link",origlink+ links +timemap );
	                                      return  r.build();
			         
		  
			
	      
		 // }
		/*  else {
			    Date date = idx.getRecent(id);
			    if (date==null) {
			    	  //bad url-id 
	          		
				       return Response.status(404).build();
			    }
			    MementoResource res = new MementoResource( baseurl);
				
				return res.getMemento(id, formatterout.format(date))  ;
			
		 } */
		  
	
}
	
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
	
	
}