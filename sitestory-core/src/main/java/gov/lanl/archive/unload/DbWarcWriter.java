package gov.lanl.archive.unload;

import gov.lanl.archive.Memento;

import gov.lanl.archive.index.bdb.IndexImplB;
import gov.lanl.archive.location.PairReader;


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.WriterPoolMember;
import org.archive.io.warc.WARCConstants;
import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.warc.WARCRecord;
import org.archive.io.warc.WARCWriter;
import org.archive.io.warc.WARCWriterPool;
import org.archive.io.warc.WARCWriterPoolSettingsData;
import org.archive.uid.RecordIDGenerator;
import org.archive.uid.UUIDGenerator;
import org.archive.util.ArchiveUtils;
import org.archive.util.anvl.ANVLRecord;

public class DbWarcWriter {
	 private static final String PREFIX = "MEM";
	 private static final AtomicInteger SERIAL_NO = new AtomicInteger();
	 public final int MAX_ACTIVE = 5;
     final int MAX_WAIT_MILLISECONDS = 20000;
   //  public static final String DB_PATH =
       //  System.getProperty( "ta.storage.basedir", "target/db" );
    static SimpleDateFormat formatterout = new SimpleDateFormat("yyyyMMddHHmmss");
	static  TimeZone tz = TimeZone.getTimeZone("GMT");
     
     
	 public static void main(String[] args) throws IOException, ParseException  {
		 
		 DbWarcWriter wr = new DbWarcWriter();
	    // wr.test();
	     wr.testread();
		 
		}
	 
 private void test() throws IOException, ParseException {
		 IndexImplB db = new IndexImplB();
		 try {
		 DbWarcWriter wr = new DbWarcWriter();
	     //wr.test();
		 
		 File arcdir = new File("/Users/ludab/projects/warcfiles");
		 File [] files = {arcdir};
		 RecordIDGenerator generator = new UUIDGenerator();
		 WARCWriterPool p = new  WARCWriterPool(SERIAL_NO, new WARCWriterPoolSettingsData(
                 "MEM", "", -1, false, null, getMetadata(), generator), wr.MAX_ACTIVE,  wr.MAX_WAIT_MILLISECONDS);
		
		// WriterPoolMember writer =
		 int a = p.getNumActive();
		 System.out.println("Num Active:"+a);
		 WriterPoolMember writer =p.borrowFile();
		  //               long position = writer.getPosition();
		                // System.out.println("position:" + position);
		    //                writer.checkSize();
		   
		 
	        
	     WARCWriter w = (WARCWriter)writer;

		// WARCWriter writer0 = new WARCWriter(SERIAL_NO, Arrays.asList(files),PREFIX, "suffix", false, -1,  wr.MakeFirstRecord ());
		   
		 //  wr.writeWarcinfoRecord(writer);
		 
		
		 SimpleDateFormat  formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
	          Date myDate = formatter.parse("Mon, 21 Jun 2010 21:13:25 MDT");
	          long nowLong = myDate.getTime();
	          String datestr = Long.toString(nowLong);
	          List <Memento> mems =  db.getUntil(datestr,null);
	          System.out.println("got the list");
	          int count=1;
	        for(Memento r:mems){
	        
	   	    wr.writeRecords( w, r);
	   	    w.checkSize();
	   	  //       position = writer.getPosition();
            //     System.out.println("position:" + position);
	   	    count=count+1;
	   	    System.out.println("here");
	   	    if( count==4) break;
	        }
	      //  checkBytesWritten();
	        w.checkSize();
	        w.close();
	        p.returnFile(w);
	   	  // writer0.close();
	   	   
	        
	        
		 }
		 finally {
	   	   db.close(); //some crap in closing
		 }
		
		  
	 }
	 
	 private void  testread () {
		 File f = new File("/Users/Lyudimila/projects/tmp/warcs/MEM-20120322195643622-00000-~~.warc.gz");
		 try {
		 WARCReader reader = WARCReaderFactory.get(f);
		 long totalRecords = 0;
		       for (final Iterator<ArchiveRecord> i = reader.iterator(); i.hasNext();  totalRecords++) {
			        WARCRecord ar = (WARCRecord)i.next();
		                ArchiveRecordHeader h = ar.getHeader();
		
	                               long l = h.getLength();
	                      String mimetype =   h.getMimetype();
	                
	                  System.out.println("mimetype"+ mimetype);
	                  System.out.println("length"+ l);
	                  
	                 // FileWriter fstream = new FileWriter("out"+l+".txt");
	                 // FileOutputStream fos = new FileOutputStream("/Users/ludab/Downloads/arc/out"+l+".txt");
	                  ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                  //BufferedWriter out = new BufferedWriter(fstream);
	                  int count = 0; 
	                  byte[] buffer = new byte[512];
	                  BufferedInputStream bis = new BufferedInputStream(ar);
	                  while  ((count = bis.read(buffer)) != -1) {
	                	 
	                        baos.write(buffer, 0, count);
	                    
	                      } 
	                  System.out.println( baos.toByteArray().length);
	            // out.close();
	             // fos.close();    
	            }
	       
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	 }
	 
	
	 private void writeWarcinfoRecord(WARCWriter writer)
	    throws IOException {
	    	ANVLRecord meta = new ANVLRecord();
	    	meta.addLabelValue("size", "1G");
	    	meta.addLabelValue("operator", "tr-achive");
	    	   meta.addLabelValue("format","WARC File Format 1.0"); 
	           meta.addLabelValue("conformsTo","http://bibnum.bnf.fr/WARC/WARC_ISO_28500_version1_latestdraft.pdf");
	    	byte [] bytes = meta.getUTF8Bytes();
	    	writer.writeWarcinfoRecord(ANVLRecord.MIMETYPE, null,
	    		new ByteArrayInputStream(bytes), bytes.length);
		}

	 
	 
	  public List MakeFirstRecord () {
		  
		  List <String> list = new ArrayList();
	    	list.add("format:WARC File Format 1.0\n");
	    	list.add("conformsTo:http://bibnum.bnf.fr/WARC/WARC_ISO_28500_version1_latestdraft.pdf\n");
	    	list.add("operator:tr-achive");
	    	return list;
	  }
	 
	  
	//  5.7	WARC-Concurrent-To
	// The WARC-Record-IDs of any records created as part of the same capture event as the current record. 
    //A capture event comprises the information automatically gathered by a retrieval against a single target-URI;
	//for example, it might be represented by a 'response' or 'revisit' record plus its associated 'request' record.  

	  
	  public   void   setInfo(String rheaders,Map map) {
		  System.out.println("from geturlInfo:"+rheaders);
				  StringBuffer sb = new StringBuffer();
		         StringTokenizer stc = new StringTokenizer(rheaders,"\r\n");
		   while (stc.hasMoreTokens()) {
		           String linec =  stc.nextToken();
		           if (linec.indexOf(":")>0) {
		        	   String name = linec.substring(0,linec.indexOf(":"));
		        	   if (name.equals("Host")) {
		        		   System.out.println("from geturlInfo host" +linec.substring(linec.indexOf(":")+1));
		                    map.put("host",linec.substring(linec.indexOf(":")+1));    
		                }
		           }
		           else {
		        	   //need to exclude HEAD
		        	   sb.append(linec);
		        	   StringTokenizer st = new StringTokenizer(linec," ");
		        	   String verb =  st.nextToken();
		        	   String url = st.nextToken();
		        	   System.out.println("from geturlInfo url" +url);
		        	   map.put("verb", verb);
		        	   map.put("url", url);
		        	    
		           }
		           
		   }
		        
			   
			 
			 
		 }
			
	  
	  
	  public void writeRecords(final WARCWriter writer, Memento m) {
		  InputStream in =null;
         try {
        	  //ThreadSafeSimpleDateFormat formatterout = new ThreadSafeSimpleDateFormat("yyyyMMddHHmmss");
        	 // TimeZone tz = TimeZone.getTimeZone("GMT");
  	           formatterout.setTimeZone(tz);
		      ANVLRecord r = new ANVLRecord();
		      //  r.addLabelValue(WARCConstants.HEADER_KEY_IP, "dummy");
		     // r.addLabelValue(WARCConstants.HEADER_KEY_PAYLOAD_DIGEST,"sha1:"+ m.getDigest());
		      // r.addLabelValue(WARCConstants.HEADER_KEY_TYPE,m.getMimetype());
		  
		     String locationid = m.getId();
			 String dupid = m.getDupId();
			 String code = m.getCode();
			 InputStream input = null;
			 String headers = m.getResheaders();
			 int j =  headers.indexOf("H");
	    	 if (j>0) {
	    		 headers=headers.substring(j);
	    		// System.out.println("headers:"+headers);
	    	 }

	    	// String rheaders = m.getReqheaders();
	    	 
	    	 //Map map = new HashMap();
	    	 //setInfo(rheaders,map);
	    //	 String urla = "";
	    	// if (map.containsKey("host") && map.containsKey("url") ) {
			  //      urla = "http://" +(String)map.get("host") + (String)map.get("url");
			    //    System.out.println("urla" +urla);
	    	 //}
	    	 String url = m.getUrl();
	    	 //System.out.println("url from writeRecords" +url);
	    	 //if (!urla.equals("")) {
	    		// url =urla;
	    	 //}
	    	 //System.out.println("urla2" +urla);
			 InputStream bis=  new ByteArrayInputStream(headers.getBytes("UTF-8"));
			 long fs = headers.length();
			 long size=0;
			 if (code.equals("302")||code.equals("303")) {
				//no body
				
				input=new ByteArrayInputStream(headers.getBytes("UTF-8"));
				size = fs;
			}
			else {
			 PairReader reader = new PairReader();
			 in = reader.read(locationid,"body");
			//InputStream bis = reader.read(dupid,"res");
			
			// bis = new ByteArrayInputStream(m.getResheaders().getBytes("UTF-8"));
			//long fs = reader.getheadersFile(dupid,"res");
		    // long fs = m.getResheaderslength();
		    //String headers = m.getResheaders();
	
		    //byte[] tbytes;
		
		   // SequenceInputStream input = null;
			//try{
			//tbytes = headers.getBytes("UTF-8");
			
			//if ( tbytes.length>0) 
			//{
		    //long size = tbytes.length + 133;//m.getLength(); //too bad magic number
		    
		  //  long hsize = f.length();
		     size = fs + m.getLength();
		     System.out.println("size of stream:" +size + "for" + url);
		    //ByteArrayInputStream bis = new ByteArrayInputStream(tbytes);
		 //   InputStream[] streams ={bis,in};
		     input = new  SequenceInputStream(bis,in);
			}
		    
			  Date d = m.getAccessdate();
			  System.out.println("mmemento:"+formatterout.format(d));
		    System.out.println("dipid"+m.getDupId());
		    if (!(m.getId().equals(m.getDupId())))
		    {
		    	/*//experiment no revisits
		    	// ANVLRecord r = new ANVLRecord();
		    	//revisit
		    	  r.addLabelValue(WARCConstants.HEADER_KEY_TRUNCATED, WARCConstants.NAMED_FIELD_TRUNCATED_VALUE_LENGTH);
		    	  r.addLabelValue(WARCConstants.HEADER_KEY_PROFILE, WARCConstants.PROFILE_REVISIT_IDENTICAL_DIGEST);		    	 
		    	 // r.addLabelValue("WARC-Refers-To","<"+new URI("urn:uuid:"+m.getId())+">");
		         // URI	rid = GeneratorFactory.getFactory().getQualifiedRecordID(WARCConstants.TYPE,WARCConstants.METADATA);
		    	  UUID uuid = UUID.randomUUID();
		    	  URI rid = new URI("urn:uuid:" +  uuid.toString()); //should /i change it to dup id ?  
		    	 // URI rid = new URI("urn:uuid:" +  m.getDupId());
		    	  //not sure what my logic was here
		    	 // URI id = writer.getRecordID();
		    	  //m.getDupId();
		    	  //Date d = m.getAccessdate();
		    	  String g = ArchiveUtils.getLog14Date(d);
		    	  System.out.println("archive date:"+g);
		          writer.writeRevisitRecord(url, ArchiveUtils.getLog14Date(d),  "application/http" ,new URI("urn:uuid:"+m.getDupId()), r, bis, fs); //was id
		          //r.addLabelValue(WARCConstants.HEADER_KEY_CONCURRENT_TO,"<"+new URI("urn:uuid:"+m.getDupId())+">"); //was id
		          //long fq = reader.getheadersFile(dupid,"req");
		          bis.close();
		          long fq = m.getReqheaderslength();
		        //  ANVLRecord rr = new ANVLRecord();
		          r.clear();
		          if(fq >0) {
		        	  r.addLabelValue(WARCConstants.HEADER_KEY_CONCURRENT_TO,"<"+new URI("urn:uuid:"+m.getDupId())+">"); //was id
		        	 // InputStream qis = reader.read(dupid,"req");
		        	  InputStream qis =  new ByteArrayInputStream(m.getReqheaders().getBytes("UTF-8"));
					  //tbytes = m.getReqheaders().getBytes("UTF-8");
					  //ByteArrayInputStream ris = new ByteArrayInputStream(tbytes);
				      writer.writeRequestRecord(url, ArchiveUtils.getLog14Date(d), WARCConstants.HTTP_REQUEST_MIMETYPE, rid, r,qis, fq);
				      qis.close();
				      }
				      */
		    }
		    else
		    {
		    	//response
		    	// ANVLRecord r = new ANVLRecord();
		    	 // metadata how long this page was valid from access point
		          r.addLabelValue(WARCConstants.HEADER_KEY_CONCURRENT_TO, "<urn:uuid:"+ m.getId()+">");
		    	  Date date_untill = m.getNextdate();
		    	  String start = ArchiveUtils.getLog14Date(d);
		    	  String end="";
		    	  if (date_untill!=null) {
		    	  //if (date_untill.before(new Date())) {
		    		  end =ArchiveUtils.getLog14Date(date_untill);
		    	  //}
		    	 String meta = "start:" + start +"\n";
		    	        meta = meta + "end:"  +end +"\n";
		    	        meta = meta + "digest:" + m.getDigest()+"\n";
		    	        meta = meta+"numberOfHits:" +m.getCounter() +"\n";
		    	        byte tmp[] = meta.getBytes("UTF-8");
		    	        InputStream mis=  new ByteArrayInputStream(tmp);    
	              writer.writeMetadataRecord(url, ArchiveUtils.getLog14Date(d), "application/warc-fields",  new URI("urn:uuid:" +   UUID.randomUUID()), r, mis, tmp.length);
	              mis.close();
		    	  }
		    	  else {
		    		  System.out.println("date untill null" + url +"," +start);
		    	  }
		    	  r.clear();
		    	  
		    	 //if 302 did not put payload digest ?
		    	 if (code.equals("302")||code.equals("303")) {
		    		//do nothing 
		    	 }
		    	 else {
		          r.addLabelValue(WARCConstants.HEADER_KEY_PAYLOAD_DIGEST,"sha1:"+ m.getDigest());
		          
		    	 }
		           writer.writeResponseRecord(url,ArchiveUtils.getLog14Date(d), WARCConstants.HTTP_RESPONSE_MIMETYPE,new URI("urn:uuid:"+m.getId()), r, input, size);
		           input.close();
		           r.clear();
		           // URI	rid = GeneratorFactory.getFactory().getQualifiedRecordID(WARCConstants.TYPE,WARCConstants.METADATA);
		           UUID uuid = UUID.randomUUID();
	    	         URI rid = new URI("urn:uuid:" +  uuid.toString());
		               r.addLabelValue(WARCConstants.HEADER_KEY_CONCURRENT_TO, "<urn:uuid:"+ m.getId()+">");
		                //long fq = reader.getheadersFile(dupid,"req"); //assuming that id==dupid
		             long fq =  m.getReqheaderslength();
		   
	           if (fq >0) {
		      //if(m.getReqheaders()!=null) {
		    	//  byte[]  reqbytes = m.getReqheaders().getBytes("UTF-8");
			  //ByteArrayInputStream ris = new ByteArrayInputStream(reqbytes);
			         //InputStream ris = reader.read(dupid,"req");
			          InputStream ris = new ByteArrayInputStream(m.getReqheaders().getBytes("UTF-8"));
		              writer.writeRequestRecord(url, ArchiveUtils.getLog14Date(d), WARCConstants.HTTP_REQUEST_MIMETYPE, rid, r,ris, fq);
		             
		              ris.close();
		             
		      }
	          
		    }
			//}
			}
			
	        catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		      
	        }
	        finally {
	        	try {
					if (in!=null) in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
         	    // Cursors must be closed.
         	   
         	}
        }
	  
	   public static List getMetadata() {
    	  List <String> list = new ArrayList();
	    	list.add("format:WARC File Format 1.0\n");
	    	list.add("conformsTo:http://bibnum.bnf.fr/WARC/WARC_ISO_28500_version1_latestdraft.pdf\n");
	    	list.add("operator:tr-achive");
	    	return list;
       }
	 
	 
	  } 

