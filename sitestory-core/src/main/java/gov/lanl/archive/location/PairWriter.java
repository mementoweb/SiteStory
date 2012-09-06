package gov.lanl.archive.location;




import gov.lanl.archive.ArchiveConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PairWriter {
	// public static final String DB_PATH = System.getProperty( "ta.storage.basedir", "target/db" );
	  public static final String DB_PATH = ArchiveConfig.prop.get("ta.storage.basedir");
	  private static Logger log= Logger.getLogger(PairWriter.class.getName());; 
	 
	 private static boolean write(InputStream src, File dest) {
	        InputStream in = null;
	        OutputStream out = null;
	        byte[] buf = null;
	        int bufLen = 5000 * 1024;
	       // System.out.println("sdes");
	        try {
	            in = new BufferedInputStream(src);
	            out = new BufferedOutputStream(new FileOutputStream(dest));
	            buf = new byte[bufLen];
	            byte[] tmp = null;
	            int len = 0;
	            int control_len=0;
	            while ((len = in.read(buf, 0, bufLen)) != -1) {
	        	  //System.out.println("sdes"+len);
	                tmp = new byte[len];
	                System.arraycopy(buf, 0, tmp, 0, len);
	                out.write(tmp);
	                control_len=len+control_len;
	            }
	           // log.info("wrote to disk:" +control_len);
	        } catch (Exception e) {
	            //System.out.println("here");
	            e.printStackTrace();
	            return false;
	        } finally {
	            if (in != null)
	                try {
	                    in.close();
	                } catch (Exception e) {
	                }
	            if (out != null)
	                    try {
	                        out.close();
	                    } catch (Exception e) {
	                    }
	        }
	        return true;
	    }
	    
	 //f2813ea2-563c-4b92-8748-36c821ed27ca
	 
	 
	 public void  write (String uuid,InputStream inputStream,String ext) {
		 
		   File f;
		   // System.out.println("uuid" +uuid);
		   String dir = DB_PATH +File.separator + "storage"+ File.separator+uuid.substring(0,2) + File.separator + uuid.substring(2, 4);
		  
		   //if (ext.equals("req")) {
		   //log.info("dir:" +dir);
		   //}
		   String name = uuid.substring(4);
		   if (ext!=null) {
		    name = uuid.substring(4)+"." + ext;
		   }
		   
		    if (ext.equals("req")) {
		    	//if (log.isLoggable(Level.INFO)) {
		         log.info("file:" + dir +File.separator+ name);
		    	//}
		    }
		    if (ext.equals("body")) {
		    	//if (log.isLoggable(Level.INFO)) {
		         log.info("file:" + dir +File.separator+ name);
		    	//}
		    }
	        if (!(f = new File(dir)).exists())
	        { f.mkdirs(); }
	        
	        f = new File(f,name);
	        
	         if (!f.exists()) {
				boolean fos = write(inputStream, f);
				if (!fos) {
					//throw new Exception( "An error occurred attempting to write to " + f.getAbsolutePath());
				}
	        }
		 
	 } 
	 
	 public void delete (String uuid1 ,String ext1) {
		   File f;
		 //  System.out.println("uuid" +uuid1);
		   String dir = DB_PATH +File.separator + "storage"+ File.separator+uuid1.substring(0,2) + File.separator + uuid1.substring(2, 4);
		   //System.out.println("dir" +dir);
		   String name = uuid1.substring(4);
		   if (ext1!=null) {
		    name = uuid1.substring(4)+"." + ext1;
		   }
		   f = new File(dir);
		   f = new File(f,name);
		   
		// Make sure the file or directory exists and isn't write protected
		    if (!f.exists())
		      //throw new IllegalArgumentException(
		       {//System.out.println( "Delete: no such file or directory: " + uuid1+"ext" + ext1);
		        return;
		       }
 
		    if (!f.canWrite()) {
		      //throw new IllegalArgumentException 
		     log.info("Delete: write protected: "
		          +  uuid1+"ext" + ext1);
		       return;
		    }

		    // If it is a directory, make sure it is empty
		    if (f.isDirectory()) {
		      String[] files = f.list();
		      if (files.length > 0) {
		       // throw new IllegalArgumentException(
		         log.info( "Delete: directory not empty: " +  uuid1+"ext" + ext1);
		      return;
		      }
		    }

		    // Attempt to delete it
		    boolean success = f.delete();

		    if (!success) {   log.info("Delete: deletion failed" + uuid1 + ext1);     }
		    //  throw new IllegalArgumentException 
		           
               return;		  
          
	     }
	 
	 
}
