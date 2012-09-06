package gov.lanl.archive.location;

import gov.lanl.archive.ArchiveConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class PairReader {
	// public static final String DB_PATH = System.getProperty( "ta.storage.basedir", "target/db" );
	  public static final String DB_PATH = ArchiveConfig.prop.get("ta.storage.basedir");
	 
public InputStream read (String uuid) {
	  File f;
	  String dir = DB_PATH  +File.separator + "storage"+ File.separator+ uuid.substring(0,2) + File.separator + uuid.substring(2, 4);
	  System.out.println("dir" +dir);
	  String name = uuid.substring(4);
	  f = new File(dir,name);
      if (f.exists()) {
    	  try {
			InputStream in = new FileInputStream(f);
			return in;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
	return null;
	
}
          public long getheadersFile(String uuid,String ext) {
        	  File f;
        	  String dir = DB_PATH  +File.separator + "storage"+ File.separator+ uuid.substring(0,2) + File.separator + uuid.substring(2, 4);
        	  System.out.println("dir" +dir);
        	  String name = uuid.substring(4);
        	  if (ext!=null) {
        		    name = uuid.substring(4)+"." + ext;
        		   }
        	  f = new File(dir,name);
        	 long size = f.length();
        	  return size;
        	  
          }
          
          
          
public InputStream read (String uuid,String ext) {
	  File f;
	  String dir = DB_PATH  +File.separator + "storage"+ File.separator+ uuid.substring(0,2) + File.separator + uuid.substring(2, 4);
	  System.out.println("dir" +dir);
	  String name = uuid.substring(4);
	  if (ext!=null) {
		    name = uuid.substring(4)+"." + ext;
		   }
	  f = new File(dir,name);
    if (f.exists()) {
    	 f.length();
  	  try {
			InputStream in = new FileInputStream(f);
			return in;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	return null;
	
}

public InputStream read (File f) {
	  
  if (f.exists()) {
  	 
	  try {
			InputStream in = new FileInputStream(f);
			return in;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }
	return null;
	
}


}
