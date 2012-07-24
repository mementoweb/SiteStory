package gov.lanl.archive.resource;

import gov.lanl.archive.ArchiveConfig;

import gov.lanl.archive.Index;
import gov.lanl.archive.Memento;

import gov.lanl.archive.location.PairReader;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.annotation.PreDestroy;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

//import org.htmlparser.Parser;

import net.htmlparser.jericho.*;
/*
@author Lyudmila Balakireva
*/

@Path("/memento/{date}/{id:.*}")

public class MementoResource {
	   static ThreadSafeSimpleDateFormat  httpformatter;
	   private static Index idx;
	   MementoCommons mc;
	  // static String indextimemap;
	   protected final URI baseUri;
	   String location;
	   static {
	       
        httpformatter = new ThreadSafeSimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");	     
        TimeZone tzo = TimeZone.getTimeZone("GMT");
        httpformatter.setTimeZone(tzo);
        idx = ArchiveConfig.getMetadataIndex();
        //indextimemap = System.getProperty( "ta.indextimemap");
        MyServletContextListener cl= MyServletContextListener.getInstance();
	    cl.setAttribute("idx", idx);
    }
	   String url;
	  /* 
	   * should be here?
	   @PreDestroy
       public void preDestroy() {
		   shutdown();
       }
	   */
	   public	MementoResource( @Context UriInfo uriInfo )
	    {
	        this.baseUri = uriInfo.getBaseUri();
	       
	        mc = new MementoCommons(baseUri);
	            System.out.println("init");
	    }
	   
	   public	MementoResource( URI baseUri )
	   {
	       this.baseUri = baseUri;
	       mc = new MementoCommons(baseUri);
	   }
	   
	   
	    @POST		
		 public Response replytoPOST() {
			 ResponseBuilder r = Response.status(405);
			  r.header("Allow", "GET,HEAD"); 
			 return r.build();
		 }
		 
		 @PUT
		 public Response replytoPUT() {
			 ResponseBuilder r = Response.status(405);
			  r.header("Allow", "GET,HEAD"); 
			 return r.build();
		 }
		 
		 @DELETE
		 public Response replytoDELETE() {
			 ResponseBuilder r = Response.status(405);
			  r.header("Allow", "GET,HEAD"); 
			 return r.build();
		 }   
	   
	   
	   
	@GET
	public  Response getMemResponse(@PathParam("id") String id,@PathParam("date") String date, @Context HttpHeaders hh,@Context UriInfo uriInfo) throws ParseException {
		URI ur = uriInfo.getRequestUri(); 
		 //System.out.println("request url:"+ur.toString());
		
		  url = ur.toString().replaceFirst(baseUri.toString()+"memento/"+date +"/", "");
		  System.out.println("get into get:"+url);
		
		Response r = getMemento( url, date, hh);
		return r;
	}
	
	
	@HEAD
	public  Response getHEADResponse(@PathParam("id") String id,@PathParam("date") String date,@Context UriInfo uriInfo) throws ParseException {
		URI ur = uriInfo.getRequestUri(); 
		 //System.out.println("request url:"+ur.toString());
		
		  url = ur.toString().replaceFirst(baseUri.toString()+"memento/"+date +"/", "");
		 System.out.println("get into get:"+url);
		Response r =  getHead( url, date);
		return r;
	}
	
	public  Response getMemento(String id,String date, HttpHeaders hh) throws ParseException {
		
		 List<String> encodinglist = hh.getRequestHeader("Accept-Encoding");
		 
		// if accept encoding is missing than it any encording
		
	    Date resourcedate = mc.checkMementoDateValidity(date);
	    if (resourcedate!=null){
		Memento m = idx.get(id, resourcedate);
		
		if (m.getStatusCode()==404) {
			//bad url-id 
			return Response.status(404).build();
		}
	    
		String locationid = m.getId();
		System.out.println("digest:" + m.getDigest());
		PairReader reader = new PairReader();
		
		String code = m.getCode();
		
		
		
		
		
		
	
	//	InputStream in = reader.read(locationid,"body");
		System.out.println("mimetype:"+m.getMimetype());
	     //	if (!m.getCompress().equals("")) {
			
		//}
		 String origlink ="<"+id+">;rel=\"original\"";
    	 String timemap = " , <"+baseUri.toString() +"timemap/link/" + id+">;rel=\"timemap\"; type=\"application/link-format\"";
		 String timebundle = " , <"+baseUri.toString() +"timebundle/" + id+">;rel=\"timebundle\"";
		 String timegate =" , <"+baseUri.toString() +"timegate/" + id+">;rel=\"timegate\" ";
		 //if (indextimemap!=null) {
           //timemap=" , <"+indextimemap+ id+">;rel=\"timemap\"; type=\"application/link-format\"";           
		 //}
		 
		 /*
		 String mem = mc.composeLink(m.getAccessdate(),id,"memento");  
		 
		 String lastmem =  mc.composeLink(m.getLastMemento().getAccessdate(),id,"last-memento");   
		 String firstmem =   mc.composeLink(m.getFirstMemento().getAccessdate(),id,"first-memento"); 
		 String nextmem="";
		 if (m.getNextMemento()!= null) {
		  nextmem =  mc.composeLink(m.getNextMemento().getAccessdate(),id,"next-memento");   
		 }
		 String prevmem="";
		 if (m.getPrevMemento()!= null) {
	           prevmem =  mc.composeLink(m.getPrevMemento().getAccessdate(),id,"prev-memento");   
		 }		
		*/
		 String links = mc.composeLinkHeader(m.getAccessdate(),m.getLastMemento().getAccessdate(),m.getFirstMemento().getAccessdate(),id);
		 StringBuffer sb = new StringBuffer(links);
		 // String mem =  mc.composeLink(m.getAccessdate(),id,"memento"); 
		 
		
	
		 
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
			 
		    	  String prev =  mc.composeLink(m.getPrevMemento().getAccessdate(),id,"memento prev");
	           sb.append(prev);
		      }
	           else {
					int m_index = sb.lastIndexOf("\"last\"");
		  			sb.insert(m_index + 1, "next  ");
				}

		 }		
   
		 
		 links = sb.toString();
		 
			if (code.equals("302")||code.equals("303")) {
				InputStream in = reader.read(locationid,"res");
				
				byte[] bytes;
				try {
					bytes = new byte[in.available()];
					in.read(bytes);
					String s = new String(bytes);
					parseServerInfo(s);
				} catch (IOException ignore) {
					// TODO Auto-generated catch block
					ignore.printStackTrace();
				}
				//nuzno get code -String to int
			 	         int icode = Integer.parseInt(code);
				 ResponseBuilder r = Response.status(icode);
				
				 
				 r.header("Location", location);
				 r.header("Memento-Datetime",httpformatter.format(m.getAccessdate()));
			     r.header("Link",origlink+links +timemap+timebundle + timegate);
				return r.build();
				         
			}
		 
		 
		 
		 InputStream in = reader.read(locationid,"body");
		 String htmlres="";
		 if (m.getMimetype().contains("text/html") || m.getMimetype().contains("application/xhtml+xml")){
			// Parser parser;
			 
			// parser = new Parser ();
			 BufferedReader br
	        	= new BufferedReader(
	        		new InputStreamReader(in));
	 
	    	StringBuilder sb1 = new StringBuilder();
	 
	    	String line;
	    	try {
				while ((line = br.readLine()) != null) {
					sb1.append(line);
				}
			  
	           br.close();
	           
	           URL ourl= new URL(id);
	           final int port = ourl.getPort();
	           String path = ourl.getPath();
	           //if (!(path.lastIndexOf("/")==path.length())){
	        	 //  path=path.substring(0, path.lastIndexOf("/"));
	           //}
	            
	           String baseurl = ourl.getProtocol() + "://" + ourl.getHost()
	            + (port != -1 && port != 80 ? ":" + port : "")
	            + path;
	           
	           String html = sb1.toString();
	          // parser = new Parser (sb1.toString());
			  
	            Source source = new Source(html);
			    OutputDocument outputDocument = new OutputDocument(source);
			    List<StartTag> l = source.getAllStartTags(HTMLElementName.BASE);
			    if (l.isEmpty()) {
			    	String base= "<base href=\""+baseurl+"\" />";
			    	System.out.println("base url"+base);
			    	
			    	//int tablePos=source.toString().indexOf(table);   
			    	
			     List<Element> headElements = source.getAllElements(HTMLElementName.HEAD);
			    	                     if (headElements.isEmpty()) {
			    		                  System.out.println("page does not have head element");
			    	                     }
			    	                     else {
			    		                  //for (Element element : headElements) {
			    		                 Element element =  headElements.get(0);
			    		                 List<StartTag> stt = element.getAllStartTags();
			    		                 StartTag st = stt.get(0);
			    		                 //int j = st.getBegin();
			    		                 int j=st.getEnd();
			    		                 outputDocument.insert(j, base);
			    		                 htmlres = outputDocument.toString();
			    	                      //}
			    	                      }
                 }
			    else {
			    	//can be that <base element exists but  href not
			    	// List<Element> baseElements = source.getAllElements(HTMLElementName.BASE);
			    	 //StartTag st = l.get(0);
			    	 //Attributes al = st.getAttributes();
			    	 //Attribute hrefAttribute = al.get("href");
			    	 //if (hrefAttribute == null) {
			    		 
			    	 //}
			    	System.out.println( "base exists url"+baseurl);
			    	
			    }
		      } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			    
		 }
		 ResponseBuilder r = null;
		 if (htmlres.length()>0) {
			 r = Response.ok(htmlres,m.getMimetype());
		 }
		 else {    r=Response.ok(in,m.getMimetype());}
		 if (!m.getCompress().equals("")) {
			r.header("Content-Encoding",m.getCompress());	
			//not acceptable 406? or just put headers?
			}
		 if (!m.getLang().equals("")) {
			 r.header("Content-Language",m.getLang());	
		 }
	     r.header("Memento-Datetime",httpformatter.format(m.getAccessdate()));
	     r.header("Link",origlink+links +timemap+timebundle + timegate);
	     return r.build();
	     
	    }
	   
	    	else {
	    		//bad date
	    		return Response.status(400).build();	
	    	}
		
	    }
	     
public  Response getHead(String id,String date) throws ParseException {
		
	    Date resourcedate = mc.checkMementoDateValidity(date);
	    if (resourcedate!=null){
		Memento m = idx.get(id, resourcedate);
		
		if (m.getStatusCode()==404) {
			//bad url-id 
			return Response.status(404).build();
		}
	    
	//	String locationid = m.getId();
	//	System.out.println("digest:" + m.getDigest());
	//	PairReader reader = new PairReader();
	//	InputStream in = reader.read(locationid);
		System.out.println("mimetype:"+m.getMimetype());
		
		 String origlink ="<"+id+">;rel=\"original\"";
    	 String timemap = " , <"+baseUri.toString() +"timemap/link/" + id+">;rel=\"timemap\"; type=\"application/link-format\"";
		 String timebundle = " , <"+baseUri.toString() +"timebundle/" + id+">;rel=\"timebundle\"";
		 String timegate =" , <"+baseUri.toString() +"timegate/" + id+">;rel=\"timegate\" ";
		// if (indextimemap!=null) {
	      //     timemap=" , <"+indextimemap+ id+">;rel=\"timemap\"; type=\"application/link-format\"";           
			// }
		
		 String links = mc.composeLinkHeader(m.getAccessdate(),m.getLastMemento().getAccessdate(),m.getFirstMemento().getAccessdate(),id);
		 StringBuffer sb = new StringBuffer(links);
		 // String mem =  mc.composeLink(m.getAccessdate(),id,"memento"); 
		 
		
	
		 
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
			 
		    	  String prev =  mc.composeLink(m.getPrevMemento().getAccessdate(),id,"memento prev");
	           sb.append(prev);
		      }
	           else {
					int m_index = sb.lastIndexOf("\"last\"");
		  			sb.insert(m_index + 1, "next  ");
				}

		 }		
   
		 
		 links = sb.toString();
		 
		 ResponseBuilder r = Response.status(200);
		 
		 if (!m.getCompress().equals("")) {
				r.header("Content-Encoding",m.getCompress());	
				//not acceptable 406? or just put headers?
				}
			 if (!m.getLang().equals("")) {
				 r.header("Content-Language",m.getLang());	
			 }
		 
	     r.header("Memento-Datetime",httpformatter.format(m.getAccessdate()));
	     r.header("Link",origlink+links +timemap+timebundle + timegate);
	     return r.build();
	     
	    }
	   
	    	else {
	    		//bad date
	    		return Response.status(400).build();	
	    	}
		
	    }
	     
	
	
public synchronized void   parseServerInfo(String headers) {
	// System.out.println("serverheaders:"+headers);
	 //long lh= headers.length();
	 // m.setResheaders(headers);
	  //m.setResheaderslength(lh);
	  StringTokenizer st = new StringTokenizer(headers,"\r\n");
	  
     while (st.hasMoreTokens()) {
             String line =  st.nextToken();
           
             if (line.indexOf(":")>0) {
             String name = line.substring(0,line.indexOf(":"));
                        
                       
                        if (name.equals("Location")) {
                            location= ((line.substring(line.indexOf(":")+1)).trim());
                                                                          
                          
                        }

                        
                       
             }
            
       }	 
}
	
	
}
