package gov.lanl.archive.resource;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import gov.lanl.archive.ArchiveConfig;
import gov.lanl.archive.Index;
import gov.lanl.archive.unload.WriteRecordCallBack;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.archive.io.WriterPoolMember;
import org.archive.io.WriterPoolSettings;
import org.archive.io.arc.ARCConstants;
import org.archive.io.warc.WARCWriterPool;
import org.archive.io.warc.WARCWriterPoolSettings;
import org.archive.io.warc.WARCWriterPoolSettingsData;
import org.archive.uid.RecordIDGenerator;
import org.archive.uid.UUIDGenerator;


/*
@author Lyudmila Balakireva
*/
@Path("/warcunload/{date}/{id:.*}")

public class WARCUnloadResource {
	  private static Index idx;
	  static WriteRecordCallBack callback; 
	  static WARCWriterPool pool;
	  //static DbWarcWriter wr;
	 final static int MAX_ACTIVE;
	 final static int MAX_WAIT_MILLISECONDS;
	  private static  String SUFFFIX = "SUF";
	  MementoCommons mc;
	  protected final URI baseUri;
	  public static final String DB_PATH ;
	  public static String UNLOAD_DIR;
	  
	         //System.getProperty( "ta.storage.basedir", "target/db" );
	  //final AtomicInteger serial=new AtomicInteger();
	  static {
		  String dbpath ="target/db";
		  if (ArchiveConfig.prop.containsKey("ta.storage.basedir")) {
			  dbpath =  ArchiveConfig.prop.get( "ta.storage.basedir");
		  }
		  DB_PATH = dbpath;
		  String unload_dir = DB_PATH  + File.separator+"warcfiles";
				  if (ArchiveConfig.prop.containsKey("warcfiles.unload.dir")) {
					  unload_dir=ArchiveConfig.prop.get("warcfiles.unload.dir");
				  }
				  UNLOAD_DIR = unload_dir;  
		  String maxwait= "200000";
		  if (ArchiveConfig.prop.containsKey("ta.warcwriterpool.maxwait"))
		  {       maxwait =     ArchiveConfig.prop.get("ta.warcwriterpool.maxwait");                         }
	         //maxwait = System.getProperty( "ta.warcwriterpool.maxwait", "200000" );
	         MAX_WAIT_MILLISECONDS =  Integer.parseInt(maxwait);
	         String maxactive = "5";
	         if (ArchiveConfig.prop.containsKey("ta.warcwriterpool.maxactive")){
	        	 maxactive= ArchiveConfig.prop.get("ta.warcwriterpool.maxactive");
	         }
             //String maxactive =  System.getProperty( "ta.warcwriterpool.maxactive", "5" );
             MAX_ACTIVE = Integer.parseInt(maxactive);
            // final AtomicInteger serial=new AtomicInteger();
           //  final WARCWriterPoolSettings set =    getSettings(true);  
            // RecordIDGenerator generator = new UUIDGenerator();
	         pool = new  WARCWriterPool(new AtomicInteger(), getSettings(true), MAX_ACTIVE,  MAX_WAIT_MILLISECONDS);
	         idx = ArchiveConfig.getMetadataIndex();
	         MyServletContextListener cl= MyServletContextListener.getInstance();
			    cl.setAttribute("idx", idx);
	         callback = new WriteRecordCallBack(pool);
	   	  //  pool = new  WARCWriterPool(new AtomicInteger(), getSettings(true), MAX_ACTIVE,  MAX_WAIT_MILLISECONDS);
	   	   // wr = new DbWarcWriter();
	    }
	  
	  public	WARCUnloadResource( @Context UriInfo uriInfo )
	    {
	        this.baseUri = uriInfo.getBaseUri();
	        mc = new MementoCommons(baseUri);
	            System.out.println("init");
	            
	    }
	 
	 
	  @GET

	  public Response UnloadSelected(@PathParam("id") String idp,@PathParam("date") String date,@Context UriInfo ui) {
		  try {
			  URI ur = ui.getRequestUri(); 
				 System.out.println("request url:"+ur.toString());
				 URI baseurl = ui.getBaseUri();
				 System.out.println("baseurl"+baseurl.toString());
				 String id = ur.toString().replace(baseurl.toString()+"warcunload/"+date+"/", "");
				 System.out.println("get into get:"+id);
			  
			  
			  Date untildate = mc.checkMementoDateValidity(date);
			  long nowLong = untildate.getTime();
	          String datestr = Long.toString(nowLong);
	         
			  idx.processUnload(datestr,callback);
			  
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		  
		  
      return Response.status(200).build();		
      }
	  
	  
	  
	 
	 
	  public static WARCWriterPoolSettings getSettings(final boolean isCompressed) {
	        return new WARCWriterPoolSettings() {
	           // public long getMaxSize() {
	           //     return ARCConstants.DEFAULT_MAX_ARC_FILE_SIZE;
	           // }
	            
	            public String getPrefix() {
	                return "MEM";
	            }
	            
	            public String getSuffix() {
	                return "SUF";
	            }
	            
	            public List<File> getOutputDirs() {
	            	
	               //File arcdir = new File(System.getProperty( "warcfiles.unload.dir", DB_PATH  + File.separator+"warcfiles"));
	            	File arcdir = new File(UNLOAD_DIR);
	                File [] files = {arcdir};
	                return Arrays.asList(files);
	            }
	            
	           // public boolean isCompressed() {
	             //   return isCompressed;
	            //}
	            
	            public List getMetadata() {
	            	  List <String> list = new ArrayList();
	      	    	list.add("format:WARC File Format 1.0\n");
	      	    	list.add("conformsTo:http://bibnum.bnf.fr/WARC/WARC_ISO_28500_version1_latestdraft.pdf\n");
	      	    	list.add("operator:tr-achive\n");
	      	    	return list;
	            }

				@Override
				public List<File> calcOutputDirs() {
					// File arcdir = new File(System.getProperty( "warcfiles.unload.dir", DB_PATH  + File.separator+"warcfiles"));
					 File arcdir = new File(UNLOAD_DIR);
					 File [] files = {arcdir};
		                return Arrays.asList(files);
					// TODO Auto-generated method stub
					//return null;
				}

				@Override
				public boolean getCompress() {
					// TODO Auto-generated method stub
					return  isCompressed;
				}

				@Override
				public boolean getFrequentFlushes() {
					// TODO Auto-generated method stub
					return true;
				}

				@Override
				public long getMaxFileSizeBytes() {
					// TODO Auto-generated method stub
					return ARCConstants.DEFAULT_MAX_ARC_FILE_SIZE;
				}

				@Override
				public String getTemplate() {
					// TODO Auto-generated method stub
					return WriterPoolMember.DEFAULT_TEMPLATE;
				}

				@Override
				public int getWriteBufferSize() {
					// TODO Auto-generated method stub
					return 16*1024;
				}

				@Override
				public RecordIDGenerator getRecordIDGenerator() {
					// TODO Auto-generated method stub
					return new UUIDGenerator();
				}
	          	
	        };
	  }
	  
}
