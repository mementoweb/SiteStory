package gov.lanl.archive.resource;
import gov.lanl.archive.ArchiveConfig;

import gov.lanl.archive.Index;
import gov.lanl.archive.Memento;

import gov.lanl.archive.location.PairWriter;


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/*
@author Lyudmila Balakireva
*/

@Path("/put/{id:.*}")

public class PutResource {
	    private static final int _CR = 13;  
	    private static final int _LF = 10; 
	    static ThreadSafeSimpleDateFormat  hformatter;
	    private static Index idx;
	    private static List iplist;
	    private static  PairWriter wr; 
	    private static Logger log; 
	    	
	    static {
	    
	         //TimeZone tz = TimeZone.getTimeZone("UTC");
	    	    log = Logger.getLogger(PutResource.class.getName());
	    	    log.info("put service init");
			    idx = ArchiveConfig.getMetadataIndex();
			    wr = new PairWriter();
			    MyServletContextListener cl= MyServletContextListener.getInstance();
			    cl.setAttribute("idx", idx);
	            hformatter = new ThreadSafeSimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");	     
	            TimeZone tzo = TimeZone.getTimeZone("GMT");
	            hformatter.setTimeZone(tzo);
	           // idx = ArchiveConfig.getMetadataIndex();
	            iplist = ArchiveConfig.iplist;
	     }
	
		
	
	@PUT
	 public   Response put_data (InputStream iinput, @PathParam("id") String url,@Context HttpServletRequest req) {
				
		   
			   	   // log.info("from put id:" +url);
			   	    
			   //		chunked = false;
			   String compress="";
			   		
			   String		code = "";
			   String		verb="";
			   String location="";	
			   String path="";
			   String host="";
			   int mcount = 0;
			  // PairWriter wr = new PairWriter();
			   
			   long threadId = Thread.currentThread().getId();
			   try {
			   Map localvalues= new HashMap();
			   		String ra = req.getRemoteAddr();
			   		//System.out.println("remote adress:"+ra);
			   		
			   		 if (iplist.size()!=0) { //stop list not set
			   			 
			   			if (!iplist.contains(ra)) {
			   				//if (log.isLoggable(Level.INFO)) {
			   				log.info("unathorized attempt to put:"+ra + "from :" + url);
			   				//}
			   				return Response.status(403).build();		
			   			}
			   		 }
			   		 else {
			   			log.finest("ip list not configured");
			   		 }
			   		 
			   	
			   		
			   	//InputStream input =	req.getInputStream();
			   		
			Memento m = new Memento();
			
			
			BufferedInputStream	input = new BufferedInputStream(iinput);
			setClientInfo(cutHeaders(input),  m,localvalues);
			setServerInfo(cutHeaders (input), m,localvalues);
			
			if (localvalues.containsKey("verb")) {
				verb=(String) localvalues.get("verb");
			}
			if (localvalues.containsKey("code")) {
				code=(String) localvalues.get("code");
			}
			
			if (localvalues.containsKey("location")) {
				location=(String) localvalues.get("location");
			}
			if (localvalues.containsKey("compress")) {
				compress=(String) localvalues.get("compress");
			}
			if (localvalues.containsKey("host")) {
				host=(String) localvalues.get("host");
			}
			if (localvalues.containsKey("path")) {
				path=(String) localvalues.get("path");
			}
			
			String orgurl = "http://" + host +path;
			System.out.println("req_url from put:" + orgurl);
			System.out.println("url from put:" + url);
			
			m.setUrl(orgurl);
			m.setReqUrl(orgurl);
			
			
			//log.info("Code:"+code);
			
			if (!verb.equals("GET")) {
				//if (log.isLoggable(Level.INFO)) {
				log.info("attempt to record :"+ code + "verb"+ verb+"from :" + orgurl);
				//}
				return Response.status(403).build();
			}
			
			
			
			if (code.equals("302") || code.equals("303")) 
			{
                
                //calculation of message digest over location 
				//if (log.isLoggable(Level.FINE)) {
				log.fine ( "recording 302"+ code);
				//}
                           byte[] defaultBytes = location.getBytes();
                        
                                     MessageDigest algorithm = MessageDigest.getInstance("SHA1");
                                                   algorithm.reset();
                                                   algorithm.update(defaultBytes);
                                            byte messageDigest[] = algorithm.digest();
                                            String rdigest =  new String (Base32.encode( messageDigest));
                                                    m.setDigest(rdigest);
                                
              
                                        m.setLength(0);
                                        String ruuid = UUID.randomUUID().toString();
                                        m.setDupId(ruuid);
                                        m.setCode(code);
                                       idx.add(m);
                
               
                // InputStream ris = new ByteArrayInputStream(m.getReqheaders().getBytes("UTF-8"));
                 //wr.write(ruuid,ris,"req");
                 
                 //InputStream rin = new ByteArrayInputStream(m.getResheaders().getBytes("UTF-8"));
                 //wr.write(ruuid,rin,"res");
                 //if (m.getIp()!=null) {
                 //InputStream rinp = new ByteArrayInputStream(m.getIp().getBytes("UTF-8"));
                 //wr.write(ruuid, rinp, "ip");
                 //}
                   return Response.status(204).build();
              }

			if (!code.equals("200")) {
				//if (log.isLoggable(Level.INFO)) {
				log.info("attempt to put http code:"+ code +"from :" + orgurl);
				//}
				return Response.status(403).build();
   			 
   		    }
			
			
			 MessageDigest hash = MessageDigest.getInstance("SHA1");
			
			 DigestInputStream digestInputStream = null;
			
			 InputStream inc = null;
			 InputStream inz = null; 
			 BufferedInputStream bf=null;
			 //stream not arriving chunked even if header set "chunked"
			// if (chunked) {
		       	//System.out.println("chunked");
		    	//	 inc = new ChunkedInputStream(new BufferedInputStream(input));
		    		 
		    		     if (compress.equals("gzip")||compress.equals("x-gzip")) {
		    		    	 //overwrite compression
		    		    	 m.setCompress("");
		    			  // System.out.println("compressed and chunked");
		    			   inz = new BufferedInputStream(new GZIPInputStream(input));
		    			  // inz = new GZIPInputStream(input);
		    			   digestInputStream = new DigestInputStream(inz, hash);
		    			 
		    		       }
		    		     else if (compress.equals("compress")||compress.equals("x-compress")) {
		    		    	 m.setCompress("");
		    		    	  inz = new BufferedInputStream(new ZipInputStream(input));
		    		    	  digestInputStream = new DigestInputStream(inz, hash);
		    		     }
		    		     else if (compress.equals("deflate")) {
		    		    	 m.setCompress("");
		    		    	// System.out.println("compressed and chunked");
		    		    	 inz = new BufferedInputStream (new InflaterInputStream(input));
		    		    	 digestInputStream = new DigestInputStream(inz, hash);
		    		    	 
		    		     }
		    		      else {
		    		    	  m.setCompress(compress); // if it still "" fine too
		    		    	  //not encoded but If compress!=null I need to save this content encording in db to serve correct one.
		    		    	  //chunked
		    		    	  //System.out.println("chunked");
		    		    	 //  bf =  new BufferedInputStream(input);
		    		        digestInputStream = new DigestInputStream( new BufferedInputStream(input), hash);
		    		       }
		        	
		    
			
				// DigestInputStream digestInputStream = new DigestInputStream(input, hash);
				 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
								    
				    byte[] buf = new byte[2048];
				    int len = 0;
				   
				    while ((len = digestInputStream.read(buf)) != -1) {
				    	mcount = mcount+len;
				    	 
				    	byteArrayOutputStream.write(buf, 0, len);
				    }
				    
				
				      digestInputStream.close();
				      String digest =  new String (Base32.encode(digestInputStream.getMessageDigest().digest()));
			 
			   m.setDigest(digest);
			   byte[] newInput = byteArrayOutputStream.toByteArray();
			   long length = newInput.length;
			   m.setLength(length);
			   //if (log.isLoggable(Level.INFO)) {
			   log.info("length:" + length +" from :" + url + "thread" + threadId);
			   //}
			   UUID id = UUID.randomUUID();
			   log.info("uuid:" + id +" from :" + url + "thread" + threadId);
			   m.setDupId(id.toString());
			  
			   boolean check = idx.add(m);
			   if (check) {
				   // new record
				   //log.info("new record");
				   String uuid = m.getId();
				   //if (log.isLoggable(Level.INFO)) {
				   log.info(" new record uuid"+uuid);
				   //}
				   ByteArrayInputStream bis = new ByteArrayInputStream(newInput);
				   wr.write(uuid, bis,"body");
				 //  InputStream i = new ByteArrayInputStream(m.getReqheaders().getBytes("UTF-8"));
				  // wr.write(uuid,i,"req");
				   
				   //InputStream in = new ByteArrayInputStream(m.getResheaders().getBytes("UTF-8"));
				   //wr.write(uuid,in,"res");
				     //      if (m.getIp()!=null) {
				       //      InputStream inp = new ByteArrayInputStream(m.getIp().getBytes("UTF-8"));
				         //   wr.write(uuid, inp, "ip");
				           // }
			   }
			  //else {
				   // old record record only headers
				  
				  // String uuid = m.getDupId();
				 
					//   log.info("old record uuid "+ uuid + "thread:" + threadId);
				
			//	   InputStream i = new ByteArrayInputStream(m.getReqheaders().getBytes("UTF-8"));
				//   wr.write(uuid,i,"req");
				   
				  // InputStream in = new ByteArrayInputStream(m.getResheaders().getBytes("UTF-8"));
				   //wr.write(uuid,in,"res");
				   //if (m.getIp()!=null) {
				   //InputStream inp = new ByteArrayInputStream(m.getIp().getBytes("UTF-8"));
				   //wr.write(uuid, inp, "ip");
				    //}
			//   }
			   
			   
			 
			
			   
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			   e.printStackTrace();
			   log.log( Level.SEVERE, "io problem  for url "+ url +"mcount:" +mcount+"threadid:" + threadId ,e);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			   log.log( Level.SEVERE, "algoritm problem " ,e);
		} 
		
		return Response.status(204).build();
		
		
	}

	 
	
	
 public   void   setClientInfo(String headers, Memento m,Map map) {
	 //if (log.isLoggable(Level.FINE)) {
	     log.fine("clientheaders:"+headers);
	 //}
	 
	   String ip="";
       StringBuffer sb = new StringBuffer();
       StringTokenizer stc = new StringTokenizer(headers,"\r\n");
   while (stc.hasMoreTokens()) {
           String linec =  stc.nextToken();
           if (linec.indexOf(":")>0) {
           String name = linec.substring(0,linec.indexOf(":"));
                       if (name.equals("X-Client-IP")) {
                          m.setIp(linec.substring(linec.indexOf(":")+1));    
                        }
                       else if (name.equals("Host")) {
                    	  String host = linec.substring(linec.indexOf(":")+1);
                    	  map.put("host",host); 
                    	  
                    	  sb.append(linec);
                          sb.append("\r\n");
                       }
                       else {
                           sb.append(linec);
                           sb.append("\r\n");
                       }
           }
           else {
        	   //need to exclude HEAD
        	   sb.append(linec);
        	   sb.append("\r\n");
        	   StringTokenizer st = new StringTokenizer(linec," ");
        	   String verb =  st.nextToken();
        	   String path = st.nextToken();
        	   map.put("verb", verb);
        	   map.put("path", path);
        	    
           }
           
   }
         String cheaders = sb.toString();
         long lh = cheaders.length();
         m.setReqheaderslength(lh);
         m.setReqheaders(cheaders);
	   
	 
	 
 }
	
     public  void   setServerInfo(String headers, Memento m,Map map) {
    	// if (log.isLoggable(Level.INFO)) {
    	  log.info("serverheaders:"+headers);
    	 //}
    	 int j =  headers.indexOf("H");
    	 if (j>0) {
    		 headers=headers.substring(j);
    		// System.out.println("headers:"+headers);
    	 }
    	 long lh= headers.length();
    	  m.setResheaders(headers);
    	  m.setResheaderslength(lh);
    	 // char[] a = headers.toCharArray();
    	//  System.out.println();
    	  //for(int index=0; index < a.length ; index++)
    	  //{    System.out.print(index + " "  + a[index]); }
    	  
    	  StringTokenizer st = new StringTokenizer(headers,"\r\n");
    	  
	      while (st.hasMoreTokens()) {
	               String line =  st.nextToken();
	               //System.out.println("line" +line);
	              if (line.indexOf(":")>0) {
	              String name = line.substring(0,line.indexOf(":"));
	                         if (name.equals("Date")) {
	                           String  datefromheaders=(line.substring(line.indexOf(":")+1)).trim();
	                           if (log.isLoggable(Level.INFO)) {  
	                               log.info("datefrom headers"+datefromheaders);
	                           }
	                           Date d = null;
							           try {
								             d = hformatter.parse(datefromheaders);
								            
								             m.setAccessdate(d);
								            // System.out.println("year"+d.getYear());
				                             String date =  hformatter.format(m.getAccessdate());
				                             if (log.isLoggable(Level.INFO)) {
				                             log.info("date formatted"+date);
				                             }
				                            // System.out.println("after try"+date);
							                } catch (ParseException e) {
								            // TODO Auto-generated catch block
								             e.printStackTrace();
							                }
							               
	                           
	                         }
	                         if (name.equals("Content-Type")) {
	                           String  mimetype=line.substring(line.indexOf(":")+1);
	                             m.setMimetype(mimetype);
	                         }
	                         
	                         if (name.equals("Content-Length")) {
	                            String clength=(line.substring(line.indexOf(":")+1)).trim();
	                           // System.out.println("clength"+clength+"/");
	                            long l = Long.parseLong(clength);
	                              m.setLength(l);
	                         }
	                         
	                        // if (name.equals("Transfer-Encoding")) {
	                        	//  chunked = true;
		                           
		                         //}
	                         
	                         if (name.equals("Content-Encoding")) {
	                        	 //m.setCompress(name);
	                        	String  compress = (line.substring(line.indexOf(":")+1)).trim();
	                        	  map.put("compress", compress);
	                        	//  m.setCompress(compress);
	                        	
	                         }
	                         if (name.equals("Content-Language")) {
	                        	  m.setLang ((line.substring(line.indexOf(":")+1)).trim());
	                        		                        		
	                        	
	                         }
	                         if (name.equals("Location")) {
                               String  location= ((line.substring(line.indexOf(":")+1)).trim());
                                    map.put("location", location);                                           
                               
                             }

	                         
	                         /*
	                         if (name.equals("Last-Modified")) {
	                           String  last_modified=(line.substring(line.indexOf(":")+1)).trim();
	                           
	                             try {
	                            	 System.out.println ("lastmodified"+last_modified);
	                            	 Date lm = hformatter.parse(last_modified);
									         m.setLastmodified(lm);
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
	                         }
	                         */
	                         
	              }
	              else {
	            //  System.out.println("line"+line);
	            	 String code = line.substring(9,12).trim();
	            	  map.put("code", code);
	              }
	        }	 
     }
	
	 public String cutHeaders (InputStream in)  {
		 StringBuffer sb = new StringBuffer();
		  try {
		   int _ch = -1;  
		   boolean endofheaders =false;
		      int pos=0;
		      while (!endofheaders) {
			  
			  
				_ch = in.read();
			 
			 // System.out.println(_ch);
			  //if  (_ch != _CR && _ch != _LF)  
			    //  {  
			         sb.append((char) _ch);  
			        
			      //} 
			if (sb.length()>5) {
			  if (sb.charAt(pos-3) == _CR && sb.charAt(pos-2)==_LF &&sb.charAt(pos-1) == _CR&&sb.charAt(pos)==_LF) {
			      endofheaders = true; 
			  }
			} 
			 pos++;
		      }
		  
		  
	     } catch (Exception e) {
			// TODO Auto-generated catch block
	    	// log.info("input stream"+sb.toString());
	    	  System.out.println("headers"+sb.toString());
			 e.printStackTrace();
			 throw new RuntimeException(e);
		} 
		     // System.out.println("headers"+sb.toString());
		     return new String( sb.toString() );
		      //return sb.toString();
	   }
	   
	
	
	
}
