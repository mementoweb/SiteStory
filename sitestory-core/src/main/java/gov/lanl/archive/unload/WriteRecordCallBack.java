package gov.lanl.archive.unload;

import java.io.IOException;
import org.archive.io.WriterPoolMember;
import org.archive.io.warc.WARCWriter;
import org.archive.io.warc.WARCWriterPool;

import gov.lanl.archive.unload.UnloadCallBack;
import gov.lanl.archive.Memento;

public class WriteRecordCallBack implements UnloadCallBack {
	  static DbWarcWriter wr;
	  static WARCWriterPool pool;
	 // final static int MAX_ACTIVE = 3;
	  //final static int MAX_WAIT_MILLISECONDS = 2000;
	 // public static final String DB_PATH = System.getProperty( "ta.storage.basedir", "target/db" );
	  
	  public	WriteRecordCallBack( WARCWriterPool pool )
	    {
	       this.pool = pool;
	        wr = new DbWarcWriter();   
	    }
	 
	
	
	
	public void methodToCallBack(Memento m, boolean stop)  {
		 WriterPoolMember writer;
		// WARCWriter w = null;
		  try {
			   writer = pool.borrowFile();
			     
			   WARCWriter w = (WARCWriter)writer;
		        
		        if (stop) {
		        	System.out.println("unloading records finished");
		        	 w.close();
				        //pool.returnFile(w);
		         }
				else {
					  wr.writeRecords( w, m);
					  w.checkSize();
			   	       
				 
				}
		        pool.returnFile(w);
		        
		        System.out.println("I've been called back");
		        
	       } catch (IOException e1) {
		           // TODO Auto-generated catch block
		          e1.printStackTrace();
	         }
			  
	       /*finally {
		       // Cursors must be closed.
		       try {
				w.close();
				pool.returnFile(w);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		       
		   }
		 */
		    
		}
	
	
	
	  
	
}
