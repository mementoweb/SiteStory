package gov.lanl.archive.index.bdb;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.NavigableMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import gov.lanl.archive.Index;
import gov.lanl.archive.Memento;
import com.sleepycat.je.*;

import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.util.url.*;
//import gov.lanl.archive.AggressiveUrlCanonicalizer; 
import gov.lanl.archive.AddedNormalization;
import gov.lanl.archive.CallBack;
import gov.lanl.archive.location.PairWriter;
import gov.lanl.archive.unload.UnloadCallBack;

public class IndexImplB implements Index{
	 protected BDBEnv bdbEnv;
	 public static final String DB_PATH = System.getProperty("ta.storage.basedir", "target/db" ); 
	 protected String databaseDirectory=System.getProperty("ta.index.basedir",  DB_PATH  + File.separator+"bdbindex" );
	 protected boolean readOnly = false;
	 final   Date mdate = new Date(32865621205000l);
	 final String maxdate = Long.toString(32865621205000l);
	  private static Logger log= Logger.getLogger(IndexImplB.class.getName());; 
	 public IndexImplB() {
		 open(false);
	 }
	 
	
	
	 
	
	public String NormalizeUrl(String url) {
		String normurl=null;
		try {
	    UrlCanonicalizer can  =	new AggressiveUrlCanonicalizer();
	
		 normurl = can.urlStringToKey(url);
		 log.info("ia normalized url:"+normurl);
		 if (url.startsWith("http://")) {
			 //taking out additional normalization steps since not compatible with wayback while exporting.
		 //    AddedNormalization norm = new AddedNormalization();
		    // String nurl = norm.normalize("http://"+normurl);
			 //H vS wants to keep protocol in db
		     normurl="http://"+normurl;
		     //log.fine("added N:"+nurl);
		 }
		}
		
		catch (Exception e) {
	           // TODO Auto-generated catch block
	           log.log( Level.SEVERE,url,e);
     }
		return normurl;
	}
	
	
	 @Override
	
	public boolean add (Memento m) {
		
		   Transaction txn = null;
		   boolean status = false;
	       try {
	           TransactionConfig config = new TransactionConfig();
	           
	        //   config.setNoSync(true);
	           txn = bdbEnv.getEnv().beginTransaction(null, config);
	           txn.setLockTimeout(50000000);
	           String requrl = m.getReqUrl();
	           String url=NormalizeUrl(requrl);
	           m.setUrl(url);
	           Memento lm = check_last ( url,txn);
	           if (lm==null) {
	        	   //first time
	        	   m.setType("1");
	        	   m.setId(m.getDupId());
	        	   insertRecord( m,txn) ;
	        	   addLast( url,txn);
	        	   //insertRecordtoLastdateDb( m,txn);
	        	   status = true;
	           }
	           
	           else {
	              //next time
	        	   Date date = lm.getAccessdate();
	        	  if (date.before(m.getAccessdate())) {
	        		    String lastdigest = lm.getDigest();
	        		        if (m.getDigest().equals(lastdigest)) {
	        			       m.setType("0");
	        			       m.setId(lm.getId());
	        			       insertRecord( m,txn);
	        			       status = false;
	        			     }
	        		        else {
	        			      m.setType("1");
	        			      m.setId(m.getDupId());
	        			     
	        			      insertRecord( m,txn);
	        			     // insertRecordtoLastdateDb( m, txn);
	        			       status=true;
	        			      }
	        		   
	        		   }
	        	  else {
	        		  // if next date to insert  older then current last. 
	        		  CursorConfig curcon = new CursorConfig ();
	        		  curcon.setReadCommitted(true);
	        		  Cursor  cursor =  bdbEnv.getResourceRecordDb().openCursor(txn, curcon);
	        		  
	        		  String rightdigest ="";
	        		  String leftdigest ="";
	        		  Memento prev =null;
	        		  Memento mem  =null;
	        		  DatabaseEntry data = new DatabaseEntry();
	        		  DatabaseEntry  key = new DatabaseEntry((url+"|"+Long.toString(m.getAccessdate().getTime())).getBytes("UTF-8")); 
	        		  
	        		  OperationStatus st =   cursor.getSearchKeyRange(key, data, LockMode.DEFAULT); // nuzno voobwe to proveryat' chto url the same
	        		   if (st == OperationStatus.SUCCESS) {
	        	    	     //System.out.println("after success cursor");
	        	    	     String keyString = new String(key.getData(), "UTF-8");
	        	    	     String strdate = keyString.substring((url+"|").length());
	        	    	     
	        	    	         mem = new Memento();
	        	    		   ResourceBinding binding = new ResourceBinding();
	              			   ResourceRecord record = (ResourceRecord) binding.entryToObject(data);
	              			   ResourceRecordToMemento(record, mem);  
	              			  // System.out.println("last date:"+ mem.getAccessdate());
	              			    rightdigest =   mem.getDigest();
	              			  // System.out.println("last digest:"+ mem.getDigest());
	              			  if (m.getAccessdate().equals( mem.getAccessdate())) {
	              				  //just dublicate we do not care about 
	              				  cursor.close();
	              				  txn.commitSync();
	              				  log.finest("case0");
	              				  return status;
	              				  }
	              			   
	              			 if(cursor.getPrev(key, data, LockMode.DEFAULT) == 
	                  	        OperationStatus.SUCCESS) {
	              				 
	              			       prev = new Memento();
		        	    		 
		              			   ResourceRecord recordp = (ResourceRecord) binding.entryToObject(data);
		              			   ResourceRecordToMemento(recordp, prev);
	              				   leftdigest =prev.getDigest();
	              				 
	              			 }
	              			   
	              			 if (m.getDigest().equals(leftdigest) && (m.getDigest().equals(rightdigest))) {
	              				   m.setType("0");
		        			       m.setId(prev.getId());
		        			       insertRecord( m,txn); 
		        			       status=false;
	              				  //status need to be set
	              				 log.finest("case1");
	              			 }
	              			 if (!m.getDigest().equals(leftdigest) && (!m.getDigest().equals(rightdigest))) {
	              				   m.setType("1");
		        			       m.setId(m.getDupId());
		        			       insertRecord( m,txn); 
	              				  //status need to be set
	              				 status = true;
	              				log.finest("case2");
	              			 }
	              			 
	              			 if (!m.getDigest().equals(leftdigest) && (m.getDigest().equals(rightdigest))) {
	              				   m.setType("1");
		        			       m.setId(m.getDupId());
		        			       insertRecord( m,txn); 
		        			       //may be need to check that record not too old;)
		        			      // insertRecordtoLastdateDb( m, txn);
		        			       //nuzno li change id? 
		        			       mem.setType("0"); //update
		        			      // removeRecord( mem,txn);
		        			       //txn.commitNoSync();
		        			       insertRecord( mem,txn); 
		        			       //update of next record needed
	              				  //status need to be set
		        			       //may be can be returned false,but id changed for next ??
	              				 status=true;
	              				log.finest("case3");
	              			 }
	              			 
	              			 if (m.getDigest().equals(leftdigest) && (!m.getDigest().equals(rightdigest))) {
	              				   m.setType("0");
		        			       m.setId(prev.getId());//was getDupId
		        			       insertRecord( m,txn); 
		        			       log.finest("case4");
	              				 status =false;
	              			 }
	              			 log.info("putfunction:"+m.getResheaders());
	              			// insertRecord_to_headers_blob(m,txn);
	              			 
	              			 
	        	    	     //String dataString = new String (data.getData(),"UTF-8");
	        	    	     System.out.println("adding to db"+url);
	        		   }
	        		  cursor.close();
	        	  }
	        	   
	        	   
	           }
	           
	           
	           
	           txn.commitSync();
	            
	            return status;
		
	       }
	       catch (Exception e) {
	    	   e.printStackTrace();
	           try {
	               if (txn != null)
	                   txn.abort();
	           } catch (DatabaseException dbe2) {
	        	   log.log( Level.SEVERE,"transaction aborting ",e); 
	        	   //dbe2.printStackTrace();
	           }
	          // ErrorHandler.error("putResourceRecord", e);
	          // log.error(e,e);
	       }
			return status;
		
		
	}
	
	public void addLast(String url,Transaction txn) {
		 //9223372036854775807 long max
		//  32865621205000  "Mon, 21 Jun 3011 21:13:25 MDT"
		try {
	     ResourceRecord rec = new ResourceRecord();
	     rec.setUrl(url);
	    // long l =  32865621205000l;
	    
	     rec.setDate(maxdate);
	    		
		 DatabaseEntry theKey = new DatabaseEntry(url.getBytes("UTF-8"));
		 ResourceBinding binding = new ResourceBinding();
		 DatabaseEntry key1 = new DatabaseEntry((url+"|"+maxdate).getBytes("UTF-8")); 
		 DatabaseEntry metadata = new DatabaseEntry();
         //System.out.println("her1");
         binding.objectToEntry(rec, metadata);
         bdbEnv.getResourceRecordDb().put(txn, key1, metadata);
		}
		catch (Exception e) {
	           // TODO Auto-generated catch block
			   log.log( Level.SEVERE, "db problem for:" + url ,e); 
	          // e.printStackTrace();
        }
	}
	
	
	public  Memento check_last (String url,Transaction txn) {
		
		 Memento m = null;
		 Cursor cursor = null;
	    try {
		
		 DatabaseEntry key = new DatabaseEntry((url+"|"+"32865621205000").getBytes("UTF-8")); 
		 DatabaseEntry data = new DatabaseEntry();
		 cursor =  bdbEnv.getResourceRecordDb().openCursor(null, null);
		 OperationStatus status =  cursor.getSearchKey(key, data, LockMode.DEFAULT);
		 
		   //OperationStatus retVal =  bdbEnv.getRecordDb.get(null, theKey, theData,LockMode.DEFAULT);
           if (status == OperationStatus.SUCCESS)
           { 
         	   
        	    // System.out.println("success");
         	   
        	     if(cursor.getPrev(key, data, LockMode.DEFAULT) == 
         	        OperationStatus.SUCCESS) {
        	    	 m = new Memento();
      	    	   ResourceBinding binding = new ResourceBinding();
      			   ResourceRecord record = (ResourceRecord) binding.entryToObject(data);
      			   ResourceRecordToMemento(record, m);  
      			   log.finest("last date:"+ m.getAccessdate());
      		       String keyString = new String(key.getData(), "UTF-8");
	    	       log.finest("key:"+keyString);
        	         return m;
        	     }
           }
           
	    }
	    
	    catch (Exception e) {
	           // TODO Auto-generated catch block
	    	 log.log( Level.SEVERE, "db problem for:" + url ,e); 
	          // e.printStackTrace();
     }
	    finally {
	    	cursor.close();
	    }
           
           return m;
	}
	
	
	public void insertRecord(Memento m,Transaction txn) {
		try {
			
		  Date date = m.getAccessdate();
		  String datestr = Long.toString(date.getTime());
		ResourceBinding binding = new ResourceBinding();
	    DatabaseEntry key1 = new DatabaseEntry((m.getUrl()+"|"+datestr).getBytes("UTF-8")); 
        ResourceRecord rec =    MementoToResourceRecord(m);
        
        DatabaseEntry metadata = new DatabaseEntry();
        //System.out.println("her1");
        binding.objectToEntry(rec, metadata);
        bdbEnv.getResourceRecordDb().put(txn, key1, metadata);
        
        
        //
        HeadersBinding hbinding = new HeadersBinding();
	    DatabaseEntry key2 = new DatabaseEntry((m.getDupId()).getBytes("UTF-8")); 
	    System.out.println("in insert record blob header:" + m.getDupId());
        HeadersRecord rec2 =   MementoToHeadersRecord(m);
        
        DatabaseEntry metadata2 = new DatabaseEntry();
        //System.out.println("her1");
        hbinding.objectToEntry(rec2, metadata2);
        OperationStatus status = bdbEnv.getHeadersBlob().put(txn, key2, metadata2);
        //just check what is it
        addHeaders(m,null);
        
        
		}
		
		catch (Exception e) {
	    	   e.printStackTrace();
	           try {
	               if (txn != null)
	                   txn.abort();
	           } catch (DatabaseException dbe2) {
	        	   log.log( Level.SEVERE, "db problem " ,dbe2); 
	        	 //  dbe2.printStackTrace();
	           }
		}
		
	}
	
	public void insertRecord_to_headers_blob(Memento m,Transaction txn) {
		try {
			
		//  Date date = m.getAccessdate();
		 // String datestr = Long.toString(date.getTime());
		HeadersBinding binding = new HeadersBinding();
	    DatabaseEntry key1 = new DatabaseEntry((m.getDupId()).getBytes("UTF-8")); 
	    System.out.println("in insert blob header:" + m.getDupId());
        HeadersRecord rec =   MementoToHeadersRecord(m);
        
        DatabaseEntry metadata = new DatabaseEntry();
        //System.out.println("her1");
        binding.objectToEntry(rec, metadata);
        OperationStatus status = bdbEnv.getHeadersBlob().put(txn, key1, metadata);
        //just check what is it
        addHeaders(m,null);
		}
		
		catch (Exception e) {
	    	   e.printStackTrace();
	           try {
	               if (txn != null)
	                   txn.abort();
	           } catch (DatabaseException dbe2) {
	        	   log.info( "db problem in insering header:" +dbe2.getMessage()); 
	        	   System.out.println( "db problem in insering headers" +dbe2);
	        	  dbe2.printStackTrace();
	           }
		}
		
	}
	
	 public void addHeaders(Memento m,Transaction txn ) {
		 try {
			 m.setReqheaders("not in db");
			 m.setResheaders("not in db");
			 m.setIp("000.00.000.0");
		   HeadersBinding binding = new HeadersBinding();
		   DatabaseEntry key = new DatabaseEntry(m.getDupId().getBytes("UTF-8"));
		   System.out.println("in add headers blob header:" + m.getDupId());
		   DatabaseEntry foundData = new DatabaseEntry();
		   Database db =  bdbEnv.getHeadersBlob();
		  
		   if (db.get(txn, key, foundData,LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
			   //byte[] retData = foundData.getData();
			   HeadersRecord a = (HeadersRecord) binding.entryToObject(foundData);
			   m.setIp(a.getIP());
			   System.out.println("from add headers:"+a.getReqHeaders());
			   m.setReqheaders(a.getReqHeaders());
			   m.setResheaders(a.getResHeaders());
			   }
		   else{
			   System.out.println("from add headers: blob bot in db");  
		   }
		  }
		  catch (Exception e) {
			  System.out.println("error in finding headers:"+m.getUrl());
			  e.getStackTrace();
			    // Exception handling goes here
			}

		   
		  // OperationStatus status =  cursor.getSearchKey(foundKey, foundData, LockMode.DEFAULT);
	}
	
	 /*
	 public void insertRecordtoLastdateDb(Memento m,Transaction txn) {
		 try {
			  Date date = m.getAccessdate();
			  String datestr = Long.toString(date.getTime());
		      DatabaseEntry key1 = new DatabaseEntry(m.getUrl().getBytes("UTF-8")); 
		      DatabaseEntry data = new DatabaseEntry(datestr.getBytes("UTF-8")); 
		      bdbEnv.lastdateDb.put(txn, key1, data);
		 
		 }
			
			catch (Exception e) {
		    	   e.printStackTrace();
		           try {
		               if (txn != null)
		                   txn.abort();
		           } catch (DatabaseException dbe2) {
		        	   log.log( Level.SEVERE, "db problem " ,dbe2);
		        	  // dbe2.printStackTrace();
		           }
			}
		 
	 }
	*/
	public void removeRecord(Memento m,Transaction txn) {
		try {
		  Date date = m.getAccessdate();
		  String datestr = Long.toString(date.getTime());
		ResourceBinding binding = new ResourceBinding();
	    DatabaseEntry key1 = new DatabaseEntry((m.getUrl()+"|"+datestr).getBytes("UTF-8")); 
        ResourceRecord rec =    MementoToResourceRecord(m);
        
        DatabaseEntry metadata = new DatabaseEntry();
       // System.out.println("her1");
        binding.objectToEntry(rec, metadata);
        bdbEnv.getResourceRecordDb().delete(txn, key1);
		}
		
		catch (Exception e) {
	    	   e.printStackTrace();
	           try {
	               if (txn != null)
	                   txn.abort();
	           } catch (DatabaseException dbe2) {
	        	   log.log( Level.SEVERE, "db problem " ,dbe2);
	        	   //dbe2.printStackTrace();
	           }
		}
		
	}
	

	 
     
   
     
	 @Override

   public List getMementos(String rurl) {
		  String url = NormalizeUrl(rurl);
 Cursor cursor = null;
	   
	   List list = new Vector();
	   
	   try {
	              
	       // Open the cursor. 
	       cursor =  bdbEnv.getPrimaryUrlIndex().openCursor(null, null);
	       DatabaseEntry foundKey = new DatabaseEntry(("1|"+url).getBytes("UTF-8"));
	       DatabaseEntry foundData = new DatabaseEntry();
	       OperationStatus status =  cursor.getSearchKey(foundKey, foundData, LockMode.DEFAULT);
	       while (status == OperationStatus.SUCCESS) {
               String keyString = new String(foundKey.getData(), "UTF-8");
               String dataString = new String(foundData.getData(), "UTF-8");
               String strdate = dataString.substring((url+"|").length());
                if (strdate.equals( maxdate)) {break; } 
             //  System.out.println("Mem date"+strdate);
              
           Memento mem = new Memento();
           mem.setUrl(url);
           
           mem.setAccessdate(StringtoDate(strdate));
           list.add(mem);
               status = cursor.getNextDup(foundData, foundData, LockMode.DEFAULT);
           }
         return list;
	       
	   } catch (Exception de) {
		   log.log( Level.SEVERE, "db problem " ,de);
	      // System.err.println("Error accessing database." + de);
	   } finally {
	       // Cursors must be closed.
	       cursor.close();
	   }
	   	       
	       return null;
   }
   
   
  
 
   
 @Override
	public List getUntil(String date, String startkey) {
	 List mems = new Vector();
	 SecondaryCursor  cursor = null;
	   try {
	       
		   DatabaseEntry key = new DatabaseEntry(date.getBytes("UTF-8"));
		
		   DatabaseEntry data = new DatabaseEntry( );
	       // Open the cursor. 
	       cursor =  bdbEnv.getIndexDateDb().openSecondaryCursor(null, null);
	      // System.out.println(date);
	       OperationStatus status =  cursor.getSearchKeyRange(key,  data,  LockMode.DEFAULT); 
	       //System.out.println("after cursor");
	       int count =0;
	       if (status == OperationStatus.SUCCESS) {
	    	//   System.out.println("after success cursor");
	    	   String ffkey = new String(key.getData(), "UTF-8"); 
			  // System.out.println("first"+ffkey);
	    	   Memento m = new Memento();
	    	   ResourceBinding binding = new ResourceBinding();
			   ResourceRecord record = (ResourceRecord) binding.entryToObject(data);
			   ResourceRecordToMemento(record, m);
			   String urla = m.getUrl();
				  Date tadate= m.getAccessdate();
				  log.info(urla+"|"+tadate);
			   
			    String fkeym = new String(key.getData(), "UTF-8"); 
			   //may not including the actual date just add mu-s ?
			   if(!fkeym.equals(maxdate)){
			   mems.add(m);
			   }
			   
			   while (cursor.getNextDup(key, data, LockMode.DEFAULT) ==  OperationStatus.SUCCESS) {
				   String fkey = new String(key.getData(), "UTF-8"); 
				   log.finest("from dup"+fkey);
				    m = new Memento();
				 
		    	    binding = new ResourceBinding();
				    record = (ResourceRecord) binding.entryToObject(data);
				    ResourceRecordToMemento(record, m);
				    String url = m.getUrl();
					  Date tdate= m.getAccessdate();
					  log.finest(url+"|"+tdate);
				   if(!fkeym.equals(maxdate)){
					   mems.add(m);
					   }
			
				   count =count+1;
			   }
			   for ( int i=0;i<count;i++) {
				   cursor.getPrev(key, data, LockMode.DEFAULT);
			   }
			   
			 //  printRecord(m);
			   //or getPrevDup???
			   while (cursor.getPrev(key, data, LockMode.DEFAULT) == 
       	        OperationStatus.SUCCESS) {
				  // System.out.print("");
				   String fkey = new String(key.getData(), "UTF-8"); 
				   log.finest(fkey);
				   Memento mu = new Memento();
		    	 
				   ResourceRecord recordm = (ResourceRecord) binding.entryToObject(data);
				   ResourceRecordToMemento(recordm, mu);
				   String url = mu.getUrl();
				  Date tdate= mu.getAccessdate();
				   log.finest(url+"|"+tdate);
				   mems.add(mu);
							   
				  
				
				   
				   
				   
				   
				   //printRecord(mu);
			   }
			   
	    	//   String foundData = new String(data.getData(), "UTF-8");
	    	   
	    	  // retVal = mySecCursor.getNextDup(secondaryKey,foundData,LockMode.DEFAULT);
	    	   }
	       
	  return mems;
	   } catch (Exception de) {
		   log.log( Level.SEVERE, "db problem " ,de);
		  // de.printStackTrace();
	       //System.err.println("Error accessing database." + de);
	   } finally {
	       // Cursors must be closed.
	       cursor.close();
	   }
	 
		// TODO Auto-generated method stub
		return null;
	}
 
 // need to take this out
 /*
   public NavigableMap<String, String> getFeed (String domain) {
	   Cursor  cursor = null;
	   NavigableMap  <String, String> nmap = new  ConcurrentSkipListMap<String, String>(); 
	   
	   try {
		 DatabaseEntry key = new DatabaseEntry();
		 //key.setPartial(true);
		 DatabaseEntry data = new DatabaseEntry( );
		 
		 cursor =  bdbEnv.getlastdateDb().openCursor(null, null);
		
		 		
          OperationStatus ret =   cursor.getNext(key, data, LockMode.DEFAULT);
          
          while (cursor.getNext(key, data, LockMode.DEFAULT) ==
        	  OperationStatus.SUCCESS) {
        	  
        	  String keyString = new String(key.getData(), "UTF-8");
        	  String dataString = new String(data.getData(), "UTF-8");
        	  nmap.put( dataString, keyString);
        	  
          }
          
          cursor.close();
		 		
		 
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		   log.log( Level.SEVERE, "db problem " ,e);
		//e.printStackTrace();
	}
	 finally {
	       // Cursors must be closed.
	       cursor.close();
	   }
	   
	 return nmap;
	 
   }
   */
   
   //special function for adding memento validity info to warc files
 
   public String getValidityB(String datestr,String url) {
	   Cursor cursor = null;
	   String enddate= "0";
	   try {
		  System.out.println("Validity function1:" +url + "date:" +datestr);
		       cursor =  bdbEnv.getPrimaryUrlIndex().openCursor(null, null);
	       DatabaseEntry foundKey = new DatabaseEntry(("1|"+url).getBytes("UTF-8"));
	       DatabaseEntry foundData = new DatabaseEntry((url+"|"+datestr).getBytes("UTF-8"));
	
	       OperationStatus status =  cursor.getSearchBoth(foundKey, foundData, LockMode.DEFAULT);
	       if (status == OperationStatus.SUCCESS) {
	    	   
	    	      String keyString = new String(foundKey.getData(), "UTF-8");
	              String dataString = new String(foundData.getData(), "UTF-8");
	                        //enddate = dataString.substring((url+"|").length());
	                        System.out.println("Validity function2:" +keyString +dataString );    
	                        
              status = cursor.getNextDup(foundKey, foundData, LockMode.DEFAULT); //changed from foundData
              if (status == OperationStatus.SUCCESS) {
               keyString = new String(foundKey.getData(), "UTF-8");
               dataString = new String(foundData.getData(), "UTF-8");
                        enddate = dataString.substring((url+"|").length());
                        System.out.println("Validity function3:" +keyString +dataString );          
              return enddate;
              }
                            
          }
	   }
	   catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			   log.log( Level.SEVERE, "db problem " ,e);
			//e.printStackTrace();
		}
		 finally {
		       // Cursors must be closed.
		       cursor.close();
		   }
	       return enddate;  
   }
   
   public int getHits(String url,String startDate, String endDate) {
	   Cursor cursor = null;
	   int i=1; //startdate
	   try {
	 		 TransactionConfig config = new TransactionConfig();
		     //   config.setNoSync(true);
		            Transaction txn = bdbEnv.getEnv().beginTransaction(null, config);
		            System.out.println("getHits2:" + url + "|"+startDate+":"+endDate ); 
		              cursor =  bdbEnv.getResourceRecordDb().openCursor(txn, null);
		            DatabaseEntry key = new DatabaseEntry((url+"|"+startDate).getBytes("UTF-8"));
		            //DatabaseEntry key = new DatabaseEntry(url.getBytes("UTF-8"));
		            DatabaseEntry data = new DatabaseEntry();
		          //  key.setPartialLength(url.getBytes().length);
		            //SearchKey or Search KeyRange ?
		            OperationStatus ret =   cursor.getSearchKey(key, data, LockMode.DEFAULT);
		      while (ret == OperationStatus.SUCCESS) { 
		    	  //skip right boarder as different digest
	    	     ret = cursor.getNext(key, data, LockMode.DEFAULT);
	    	     String dataString = new String(key.getData(), "UTF-8");
	    	     //just to test comment out this section
	    	     Memento mu = new Memento();
	    	     ResourceBinding binding = new ResourceBinding();
				   ResourceRecord recordm = (ResourceRecord) binding.entryToObject(data);
				   ResourceRecordToMemento(recordm, mu);
				   String type = mu.getType();
				   if (type.equals("1")) break;
	    	     //if (!url.equals(dataString.substring(dataString.indexOf("|")))) {
	    	     //String dateStr = dataString.substring((url+"|").length());
	    	    //String dateStr = dataString.substring(url.length()+1);
	      	     System.out.println(dataString  +"type" +type);
	      	     //if (dateStr.equals(endDate)) {
	   	    		//break;
	   	    	 //}
	    	     i=i+1;
	    	     //}
	    	     //else{
	    	    	// break;
	    	     //}
	     //	if (dateStr.equals(startDate)) {
	    	//	break;
	    	//}
	    	
	    }
	   }
	   catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			   log.log( Level.SEVERE, "db problem " ,e);
			e.printStackTrace();
		}
		 finally {
		       // Cursors must be closed.
		       cursor.close();
		   }
	    
	   return i;
   }
   
   
 @Override
	public void processUnload(String date, UnloadCallBack unloadcallback) {
	// List mems = new Vector();
	
	 
	 SecondaryCursor  cursor = null;
	   try {
		   String  nowdate = String.valueOf((new Date()).getTime());
		   DatabaseEntry key = new DatabaseEntry(date.getBytes("UTF-8"));
		
		   DatabaseEntry data = new DatabaseEntry( );
	       // Open the cursor. 
		   CursorConfig cf=new CursorConfig();
		   cf.setReadCommitted(true);
		  
	       cursor =  bdbEnv.getIndexDateDb().openSecondaryCursor(null, null);
	      System.out.println("from process unload"+date);
	      
	       OperationStatus status =  cursor.getSearchKeyRange(key,  data,  LockMode.DEFAULT); 
	       System.out.println("found data:"+cursor.count());
	     //  System.out.println("after cursor");
	       int count =0;
	      // Date rightdate =  mdate; // infinity date //will change to unavailable
	       if (status == OperationStatus.SUCCESS) {
	    	  // System.out.println("after success cursor");
	    	      String ffkey = new String(key.getData(), "UTF-8"); 
			      log.finest("first"+ffkey);
	    	      Memento m = new Memento();
	    	      ResourceBinding binding = new ResourceBinding();
			      ResourceRecord record = (ResourceRecord) binding.entryToObject(data);
			      ResourceRecordToMemento(record, m);
			      String urla = m.getUrl();
				  Date tadate= m.getAccessdate();
				  log.info("from unload:"+urla+"|"+tadate);
			   
			      String fkeym = new String(key.getData(), "UTF-8"); 
			      //may not including the actual date just add mu-s ?
			          if(!fkeym.equals(maxdate)){
				         addHeaders(m,null);
				         if (m.getDupId().equals(m.getId())) {
				        	String nextDate = getValidityB(fkeym,m.getUrl());
				        	 
				        	//if (nextDate.equals("0")) {  nextDate = date;} //what is that ?
				        	if (nextDate.equals(maxdate)) {  nextDate = nowdate;  }
				        	
				        	 int hits = getHits(m.getUrl(),fkeym,nextDate);
				        	 m.setCounter(hits);
				        	 m.setNextdate( new Date(Long.parseLong(nextDate)));
				         }
				         unloadcallback.methodToCallBack(m, false);
			       //mems.add(m);
			             }
			   
			   while (cursor.getNextDup(key, data, LockMode.DEFAULT) ==  OperationStatus.SUCCESS) {
				      String fkey = new String(key.getData(), "UTF-8"); 
				      log.finest("from dup"+fkey);
				      m = new Memento();
				 
		    	      binding = new ResourceBinding();
				      record = (ResourceRecord) binding.entryToObject(data);
				      ResourceRecordToMemento(record, m);
				      String url = m.getUrl();
					  Date tdate= m.getAccessdate();
					  log.finest(url+"|"+tdate);
				       if(!fkeym.equals(maxdate)){
				    	     if (m.getDupId().equals(m.getId())) {
					        	 String nextDate = getValidityB(fkey,m.getUrl()); 
					        	 //if (nextDate.equals("0")) {  nextDate = date;} //what is that ?
						        	if (nextDate.equals(maxdate)) {  nextDate = nowdate;  }
					        	 m.setNextdate( new Date(Long.parseLong(nextDate)));
					        	
					        	 int hits = getHits(m.getUrl(),fkey,nextDate);
					        	 m.setCounter(hits);
					         }
					   addHeaders(m,null);
					   unloadcallback.methodToCallBack(m, false);
					  // mems.add(m);
					   }
			          count =count+1;
			       }
			       for ( int i=0;i<count;i++) {
				   cursor.getPrev(key, data, LockMode.DEFAULT);
			       }
			   
			 //  printRecord(m);
			   //or getPrevDup???
			  
			   while (cursor.getPrev(key, data, LockMode.DEFAULT) == 
    	        OperationStatus.SUCCESS) {
				   //System.out.print("");
				   String fkey = new String(key.getData(), "UTF-8"); 
				   log.finest(fkey);
				   Memento mu = new Memento();		    	 
				   ResourceRecord recordm = (ResourceRecord) binding.entryToObject(data);
				   ResourceRecordToMemento(recordm, mu);
				   String url = mu.getUrl();
				   Date tdate= mu.getAccessdate();
				   log.info("getPrev"+url+"|"+tdate);
				   //mems.add(mu);
				   if (mu.getDupId().equals(mu.getId())) {
			        	 String nextDate = getValidityB(fkey,mu.getUrl()); 
			        	 //if (nextDate.equals("0")) {  nextDate = date;} //what is that ?
				        	if (nextDate.equals(maxdate)) {  nextDate = nowdate;  }
			        	 mu.setNextdate( new Date(Long.parseLong(nextDate)));
			        	
			        	 int hits = getHits(mu.getUrl(),fkey,nextDate);
			        	 mu.setCounter(hits);
			         }
				   addHeaders(mu,null);
				   unloadcallback.methodToCallBack(mu, false);		   
				  				   				   
				   //printRecord(mu);
			   }
			   
	    	//   String foundData = new String(data.getData(), "UTF-8");
	    	   
	    	  // retVal = mySecCursor.getNextDup(secondaryKey,foundData,LockMode.DEFAULT);
	    	   }
	            //addHeaders(mu,null);
	           unloadcallback.methodToCallBack(null, true);
	  //return mems
	   } catch (Exception de) {
		   log.info(  "unload problem "  + de);
		   
		   de.printStackTrace();
	       //System.err.println("Error accessing database." + de);
	   } finally {
	       // Cursors must be closed.
	       cursor.close();
	   }
	 
		// TODO Auto-generated method stub
		//return null;
	}

 
 
 
 
 public String getLast(String url) {
	  String lastdate;
 	  Cursor cursor = null;
      try {
   	   cursor = bdbEnv.getPrimaryUrlIndex().openCursor(null, null); 
   	  
    	      DatabaseEntry key = new DatabaseEntry(("1|"+url).getBytes("UTF-8"));
    	      log.finest("url:"+url);
              //DatabaseEntry theData = new DatabaseEntry();
              DatabaseEntry data = new DatabaseEntry((url+"|"+maxdate).getBytes("UTF-8")); 
    	   
             // OperationStatus retVal =  bdbEnv.getPrimaryUrlIndex().get(null, theKey, theData,LockMode.DEFAULT);
              
             OperationStatus status =  cursor.getSearchBothRange(key, data,  LockMode.DEFAULT); 
                  if (status == OperationStatus.SUCCESS)
             { 
            	   if(cursor.getPrevDup(key, data, LockMode.DEFAULT) == 
           	        OperationStatus.SUCCESS) {
            	     //System.out.println("success");
          	         String lastdatekey = new String(data.getData(), "UTF-8"); 
          	         lastdate = lastdatekey.substring((url+"|").length());
          	         return lastdate;
            	   }
             }
             
     }	
     
	    catch (UnsupportedEncodingException e) {
	           // TODO Auto-generated catch block
	 	    log.log( Level.SEVERE, "db problem " ,e);
	          // e.printStackTrace();
       }

	    finally {
		       // Cursors must be closed.
		       cursor.close();
		   }
	   return null;
	  
 }

 
   
public String  getFirst(String url){
	   
	   Cursor cursor = bdbEnv.getPrimaryUrlIndex().openCursor(null, null); 
	 
	try {
		  DatabaseEntry key = new DatabaseEntry(("1|"+url).getBytes("UTF-8"));
	      DatabaseEntry data = new DatabaseEntry( "0".getBytes("UTF-8"));
	      OperationStatus status =  cursor.getSearchBothRange(key, data,  LockMode.DEFAULT); 
	    
	       if (status == OperationStatus.SUCCESS) {
     	  String foundData = new String(data.getData(), "UTF-8"); 
     	   String strdate = foundData.substring((url+"|").length());
     	      log.finest("First in Str:"+strdate);
     	   
     	  // Date next = new Date(Long.parseLong(foundData));
           // System.out.println("First:"+next);
     	    return strdate;   
           }
	   
      } catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
   	   log.log( Level.SEVERE, "db problem " ,e);
		//e.printStackTrace();
	    }
      finally {
     	    // Cursors must be closed.
     	    cursor.close();
     	}

return null;
}

   
   public String getId (String url,String datestr){
	   String id =null;
	    try {
			DatabaseEntry key = new DatabaseEntry((url+"|"+datestr).getBytes("UTF-8"));
			DatabaseEntry foundData = new DatabaseEntry();
			
			
			  OperationStatus retVal =  bdbEnv.getResourceRecordDb().get(null, key, foundData,LockMode.DEFAULT);
              if (retVal == OperationStatus.SUCCESS)
              { 
            	  ResourceBinding binding = new ResourceBinding();
            	  ResourceRecord record = (ResourceRecord) binding.entryToObject(foundData);
            	  id = record.getId();
              }
			
		} catch (UnsupportedEncodingException e) {
			   log.log( Level.SEVERE, "db problem " ,e);
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} 
	   
	   return id;
   }
   
   
   
   Date keyToDate(String key,String url) {
	   String strdate = key.substring((url+"|").length());
  	   Date next = new Date(Long.parseLong(strdate));
  	   return next;
   }
   
   
   public Memento getMemento(String url,Date date) {
	   
	   Cursor cursor = bdbEnv.getPrimaryUrlIndex().openCursor(null, null); 
	     Memento m = new Memento();
		   try {
		     String fdate = getFirst(url);
		     
		     String ldate = getLast(url);
		    // log.finest("last:"+StringtoDate(ldate));
		     
		     //log.finest("first:"+StringtoDate(fdate));
		      if (fdate==null) {
		    	 m.setStatuscode(404);
		    	 return m;
		      }
		      
		  	if (fdate.equals(ldate)) {
	    		//then just one record and this is memento so 
	    		//need just get records from dbrecord
	    		  // Memento mem = new Memento();
	    		 log.finest("one record");
	    		  setMemento( m,  fdate, url, fdate,ldate);
	    		  addHeaders(m,null); 
	    		  return m;
	    	 }
	   
		    Long datereq =  date.getTime();
	          long l = Long.parseLong(ldate);
	          if (datereq.longValue()>l ) {
	        	  log.finest("outside last");
	        	  //out of range so you do not need to look for memento
	        	  //make momento the lastdate ; 
	        	   setMemento( m,  ldate, url, fdate,ldate);
	        	   addHeaders(m,null);
		    	   //may be I can find prev (before last).
		    	   return m;
	          }                                
	          long f = Long.parseLong(fdate);
	          if (datereq.longValue() < f ) {
	        	  log.finest("outside first");
	        	  //out of range so you do not need to look for memento
	        	  //make momento the lastdate ; 
	        	   setMemento( m,  fdate, url, fdate,ldate);
	        	   addHeaders(m,null);
		    	   //may be I can find next (after last).
		    	   return m;
	          }  
	          DatabaseEntry key = new DatabaseEntry(("1|"+url).getBytes("UTF-8"));
	          String datakey =url+"|"+ Long.toString( date.getTime());
	 	      DatabaseEntry data = new DatabaseEntry(datakey.getBytes("UTF-8"));
	 	      OperationStatus status =  cursor.getSearchBothRange(key,  data,  LockMode.DEFAULT); 
	 	      
	 	     if (status == OperationStatus.NOTFOUND) {
	 	    	log.finest("not in range");
	            return null;
	         } else if (status == OperationStatus.SUCCESS) {
	         	
	         	 String foundData = new String(data.getData(), "UTF-8"); 
	         	   String strdate = foundData.substring((url+"|").length());
	         	  Date next = new Date(Long.parseLong(strdate));
	                 log.finest("Next:"+next);
	                //specific bdb  
	                 if (next.equals(date)) { 
	           		  
	             		//  m.setAccessdate(next);
	             		  setMemento( m,  strdate, url, fdate,ldate);
	             		  Date lastdate = new Date(Long.parseLong(ldate));
	             		  if (!next.equals(lastdate)) {
	             		     if(cursor.getNextDup(key, data, LockMode.DEFAULT) == 
	               	          OperationStatus.SUCCESS) {
	               		   
	             			  Memento nextmemento = new Memento();
	             			  
	             			  String next2 = new String(data.getData(), "UTF-8"); //but not date yet
	             			 
	                 		    nextmemento.setAccessdate(keyToDate(next2,url));
	                 		    m.setNextMemento(nextmemento);
	                 		    
	               		        log.finest( "next:"+keyToDate(next2,url));
	               	          }	  
	             		     if(cursor.getPrevDup(key, data, LockMode.DEFAULT) == 
	                	        OperationStatus.SUCCESS) {
	                		 //this memento //but not date
	                		    String memento = new String(data.getData(), "UTF-8");
	                		     log.finest( "Mememnto/prev:"+keyToDate(memento,url));
	                	      }
	                	   }    

	             	     if(cursor.getPrevDup(key, data, LockMode.DEFAULT) == 
	              	        OperationStatus.SUCCESS) {
	              		 //this memento
	             	    	 
	             	    	    String prev = new String(data.getData(), "UTF-8");
	                 		    Memento prevmemento = new Memento();
	                 		    prevmemento.setAccessdate(keyToDate(prev,url));
	                 		    m.setPrevMemento(prevmemento);
	              		   // String memento = new String(data.getData(), "UTF-8");
	              		     log.finest( "Mememnto/prev2:"+keyToDate(prev,url));
	              	      }
	             	     
	             	  
	             	    }
	                  if (next.after(date)) {
	           		  boolean isprev = false;
	           	  if(cursor.getPrevDup(key, data, LockMode.DEFAULT) == 
	         	        OperationStatus.SUCCESS) {
	           		   String mementostr = new String(data.getData(), "UTF-8");
	           		  String memento = mementostr.substring((url+"|").length());
	           		   setMemento( m,  memento, url, fdate,ldate);
	           		 //  addHeaders(m,null);
	         		    log.finest( "Mememnto second case:"+new Date(Long.parseLong(memento)));
	         		    isprev=true;
	         	       }
	           	if(cursor.getPrevDup(key, data, LockMode.DEFAULT) == 
        	        OperationStatus.SUCCESS) {
          		    String prev = new String(data.getData(), "UTF-8");
          		    Memento prevmemento = new Memento();
          		    prevmemento.setAccessdate(keyToDate(prev,url));
          		   
          		    m.setPrevMemento(prevmemento);
        		    log.finest( "Prev:"+keyToDate(prev,url));
        	      }
	           	
	            if(isprev) {
        	        Memento nextmem = new Memento();
        	        nextmem.setAccessdate(next);
        	        m.setNextMemento(nextmem);
        	  }
        	  else {
        		  //memento hits first record
        		  setMemento( m,  strdate, url, fdate,ldate); //foundData not correct string
        		  //addHeaders(m,null);
        	  }
        	  }
	                  //just here ??
	                  addHeaders(m,null);
           return m;   
        }
		     } catch (UnsupportedEncodingException e) {
		 		// TODO Auto-generated catch block
		  	   log.log( Level.SEVERE, "db problem " ,e);
		 		//e.printStackTrace();
		 	    }
		          finally {
		         	    // Cursors must be closed.
		         	    cursor.close();
		         	}

		    return null;
		    }
   
   
   
   
   Date StringtoDate(String str) {
	   Date mdate = new Date(Long.parseLong(str));
	   return mdate;
   }
	public ResourceRecord MementoToResourceRecord(Memento m) {
	 	ResourceRecord record = new ResourceRecord();  
	 	record.setType(m.getType());
		record.setId(m.getId());
		record.setDigest(m.getDigest());
		//record.setIp(m.getIp());
		record.setMimetype(m.getMimetype());
		record.setLength(new Long(m.getLength()));
		record.setUrl(m.getUrl());
		record.setDate(Long.toString(m.getAccessdate().getTime()));
		//  record.setDate(m.getAccessdate().getTime());
	//	if (m.getLastmodified()!=null) {
		//	record.setLastmodified(m.getLastmodified().getTime());
		//record.setLastmodified(Long.toString(m.getLastmodified().getTime()));
		//}
		//else {
			//record.setLastmodified("0");
			//record.setLastmodified(0);
		//}
		record.setDupId(m.getDupId());
		record.setReqLength(m.getReqheaderslength());
		record.setResLength(m.getResheaderslength());
		record.setCompress(m.getCompress());
		record.setLang(m.getLang());
		record.setCode(m.getCode());
		return record;
	}
	 
	public  HeadersRecord MementoToHeadersRecord(Memento m) {
		HeadersRecord record = new HeadersRecord();
		record.setId(m.getId());
		record.setIP(m.getIp());
		System.out.println("setting headers for db:"+m.getReqheaders());
		record.setReqHeaders(m.getReqheaders());
		record.setResHeaders(m.getResheaders());
		return record;
	}
	
	public void ResourceRecordToMemento(ResourceRecord rec,Memento m) {
		  Date mdate = new Date(Long.parseLong(rec.getDate()));
		 // Date mdate = new Date(rec.getDate());
		m.setAccessdate(mdate);
		m.setDigest(rec.getDigest());
		m.setId(rec.getId());
		//m.setIp(rec.getIp());
		m.setMimetype(rec.getMimetype());
		m.setUrl(rec.getUrl());
		//Date lastmodifieddate = new Date(rec.getLastmodified());
		//m.setLastmodified(lastmodifieddate);
		m.setLength(rec.getLength());
		m.setDupId(rec.getDupId());
		m.setType(rec.getType());
		m.setReqheaderslength(rec.getReqLength());
		m.setResheaderslength(rec.getResLength());
		m.setCompress(rec.getCompress());
		m.setLang(rec.getLang());
		m.setCode(rec.getCode());
	}
	
	public void printRecord(Memento m){
		System.out.println(m.getType());
		System.out.println("Meme Date:"+m.getAccessdate());
		System.out.println(m.getDigest());
		System.out.println(m.getMimetype());
		System.out.println(m.getId());
		System.out.println(m.getUrl());
		//System.out.println("Last:"+m.getLastMemento().getAccessdate());
		//System.out.println("First:"+m.getFirstMemento().getAccessdate());
		if (m.getNextMemento()!=null) {
		System.out.println("Next:"+m.getNextMemento().getAccessdate());
		}
		if (m.getPrevMemento()!=null){
		System.out.println("Prev:"+m.getPrevMemento().getAccessdate());
		}
	}
	
   public void setMemento(Memento m, String date, String url,String fdate,String ldate) throws UnsupportedEncodingException {
	   DatabaseEntry theKey = new DatabaseEntry((url+"|"+date).getBytes("UTF-8"));
       DatabaseEntry theData = new DatabaseEntry();
	   OperationStatus retVal =  bdbEnv.getResourceRecordDb().get(null, theKey, theData,LockMode.DEFAULT);
	   if (retVal == OperationStatus.SUCCESS)
          { 
		   ResourceBinding binding = new ResourceBinding();
		   ResourceRecord record = (ResourceRecord) binding.entryToObject(theData);
		   ResourceRecordToMemento(record, m);
		   Memento first = new Memento();
		   first.setAccessdate( new Date(Long.parseLong(fdate)));
		   m.setFirstMemento(first);
		   Memento last = new Memento();
		   last.setAccessdate( new Date(Long.parseLong(ldate)));
		   m.setLastMemento(last);
		   }
  }
	
   //some thin memento
   
	
   public void setNMemento(Memento m, String date, String url,String fdate,String ldate) throws UnsupportedEncodingException {
	   DatabaseEntry theKey = new DatabaseEntry((url+"|"+date).getBytes("UTF-8"));
       DatabaseEntry theData = new DatabaseEntry();
	   OperationStatus retVal =  bdbEnv.getResourceRecordDb().get(null, theKey, theData,LockMode.DEFAULT);
	   if (retVal == OperationStatus.SUCCESS)
          { 
		   ResourceBinding binding = new ResourceBinding();
		   ResourceRecord record = (ResourceRecord) binding.entryToObject(theData);
		   ResourceRecordToMemento(record, m);
		   m.setFirstdate( new Date(Long.parseLong(fdate)));
		   m.setLastdate(new Date(Long.parseLong(ldate)));
		  // Memento first = new Memento();
		   //first.setAccessdate( new Date(Long.parseLong(fdate)));
		   //m.setFirstMemento(first);
		  // Memento last = new Memento();
		  // last.setAccessdate( new Date(Long.parseLong(ldate)));
		   //m.setLastMemento(last);
		   }
  }
	
	 /**
     * Open an index instance
     * 
     * @param readonly
     *            allow index modification
     */
    public void open(boolean readonly) {
    	if (bdbEnv == null || bdbEnv.getEnv() == null)
            bdbEnv = new BDBEnv(databaseDirectory, readonly);
    	else {
    		bdbEnv.openDatabases(readonly);
    	}
    }

    /**
     * Close databases related to env; call before close. Closes the databases, but not the env
     * @throws IndexException
     */
    public void closeDatabases() throws Exception {
        bdbEnv.closeDatabases();
    }
    
    /**
     * Close current index instance
     */
    public void close() {
        try {
			bdbEnv.shutDown();
			bdbEnv = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public BDBEnv getDbEnvironment() {
		return bdbEnv;
	}

	public void setDbEnvironment(BDBEnv dbEnvironment) {
		this.bdbEnv = dbEnvironment;
	}




	@Override
	public Memento get(String url, Date accessdatetime) {
		// TODO Auto-generated method stub
		  String nurl = NormalizeUrl(url);
		  Memento m =  getMemento(nurl,accessdatetime);
		return m;
	}



	@Override
	public Date getRecent(String rurl) {
		  String url = NormalizeUrl(rurl);
	 	 String datestr = getLast( url);
	 	 log.finest("in recent:"+datestr);
		 Date mdate = null;
		 if (datestr !=null) {
			 
			 mdate =new Date(Long.parseLong(datestr));
		 }
		// TODO Auto-generated method stub
		return mdate;
	}

	@Override
	public void delete(String rurl,String date,CallBack callback) {
		//assuming date 
		// TODO Auto-generated method stub
		//normolaze url first
		if (rurl==null) {
			delete_by_date (date,callback);
			
		}
				
		if (date==null) {
		  String url = NormalizeUrl(rurl);
		  delete_by_url (url,callback);
		}
		
		if ((date!=null)&&(rurl!=null)) {
			  String url = NormalizeUrl(rurl);
			  delete_by_date_url (url,date,callback);
			
		}
		
		
	}

	// this function will delete all mementoes for particular url
	
	public void delete_by_url (String url,CallBack callback) {
		 Transaction txn = null;
		 Cursor cursor = null;
	   	 try { 
	   		 
	   		 TransactionConfig config = new TransactionConfig();
	     //   config.setNoSync(true);
	            txn = bdbEnv.getEnv().beginTransaction(null, config);
			    
	             cursor =  bdbEnv.getResourceRecordDb().openCursor(txn, null);
	            DatabaseEntry key = new DatabaseEntry(url.getBytes("UTF-8"));
	            DatabaseEntry data = new DatabaseEntry();
	            key.setPartialLength(url.getBytes().length);
	            //SearchKey or Search KeyRange ?
	            OperationStatus ret =   cursor.getSearchKeyRange(key, data, LockMode.DEFAULT);
	            
	            while (ret == OperationStatus.SUCCESS) {
	 	    	   log.finest("delete range");
	 	    	   String keyString = new String(key.getData(), "UTF-8");
	 	    	     ResourceBinding binding = new ResourceBinding();
				     ResourceRecord record = (ResourceRecord) binding.entryToObject(data);
	 	    		 String uuid = record.getDupId();  
	 	    	     String fullurl = record.getUrl();
	 	    	    DatabaseEntry dkey = new DatabaseEntry(uuid.getBytes("UTF-8"));
	 	    	     log.finest("fullurl" +fullurl);
	 	    	    String strdate = keyString.substring((url+"|").length());
	 	    	     log.finest("strdate" + strdate);
	 	    	   //make shure that we are not deleting by domain
	 	    	   if (fullurl.equals(url)) {
	 	    	         log.finest("deleting " +keyString);
	 	    	         cursor.delete();
	 	    	         bdbEnv.getHeadersBlob().delete(txn, dkey);
	 	    	        // txn.commit(); 
	 	    	           //no representation for dummy record at file system
	 	    	           if(!strdate.equals(maxdate)){
	 	    	        	  
	 	 				      callback.methodToCallBack(uuid);
	 	    	           }
	 	    	   }
	 	    	   else {
	 	    		   //range query may get different urls;
	 	    		   break;
	 	    	   }
	 	    	   ret = cursor.getNext(key, data, LockMode.DEFAULT);
	 	    	   
	 	       }
	            cursor.close();
	            
	            txn.commitSync(); //need commiting after each thousend records?
	 	       
		    } catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			   log.log( Level.SEVERE, "db problem " ,e);
			
			  try {
	              if (txn != null)
	            	  if (cursor!=null)
	            	  {cursor.close(); }
	                  txn.abort();
	          } catch (DatabaseException dbe2) {
	       	   log.log( Level.SEVERE, "db problem " ,dbe2);
	       	   //dbe2.printStackTrace();
	          }
		}
	            
	}
	
	 //this will delete range
	
	public void delete_by_date_url (String url,String date,CallBack callback) {
		 Transaction txn = null;
		 Cursor  cursor = null;
	   	 try { 
	   		 
	   		 TransactionConfig config = new TransactionConfig();
	     //   config.setNoSync(true);
	            txn = bdbEnv.getEnv().beginTransaction(null, config);
			    
	              cursor =  bdbEnv.getResourceRecordDb().openCursor(txn, null);
	            DatabaseEntry key = new DatabaseEntry((url+"|"+date).getBytes("UTF-8"));
	            //DatabaseEntry key = new DatabaseEntry(url.getBytes("UTF-8"));
	            DatabaseEntry data = new DatabaseEntry();
	          //  key.setPartialLength(url.getBytes().length);
	            //SearchKey or Search KeyRange ?
	            OperationStatus ret =   cursor.getSearchKeyRange(key, data, LockMode.DEFAULT);
	            int count = 0;
	            while (ret == OperationStatus.SUCCESS) {
	 	    	    log.finest("delete range by date and url");
	 	    	    String keyString = new String(key.getData(), "UTF-8");
	 	    	  
	 	    		  String strdate = keyString.substring((url+"|").length());
	 	    		  
	 	    	     ResourceBinding binding = new ResourceBinding();
   				     ResourceRecord record = (ResourceRecord) binding.entryToObject(data);
   				     String fullurl = record.getUrl();
	 	    		 String uuid = record.getDupId();  
	 	    		   DatabaseEntry dkey = new DatabaseEntry(uuid.getBytes("UTF-8"));
	 	    	   if (url.equals(fullurl)) {
	 	    		  log.finest("deleting " +keyString);
	 	    	   if (count==0) {
	 	    		   if (strdate.equals(date)) {
	 	    			  cursor.delete();
	 	    			  bdbEnv.getHeadersBlob().delete(txn, dkey);
	 	    			//  txn.commit();
	 	    			  if(!strdate.equals(maxdate)){
	 	    			   callback.methodToCallBack(uuid);
	 	    			  }
	 	    		   }
	 	    		   
	 	    	   }else
	 	    	   {
	 	    	   cursor.delete();
	 	    	   bdbEnv.getHeadersBlob().delete(txn, dkey);
	 	    	               if(!strdate.equals(maxdate)){
	 				               callback.methodToCallBack(uuid);
	 	    	                }
	 	    	   }
	 	    	   
	 	    	   }
	 	    	   else {
	 	    		   break;
	 	    	   }
	 	    	   ret = cursor.getPrev(key, data, LockMode.DEFAULT);
	 	    	   count=count+1;
	 	         }
	            
	            cursor.close();
	            
	            txn.commitSync(); //need commiting after each thousend records?
	 	       
		    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			
			  try {
	              if (txn != null)
	            	  if (cursor!=null) {
	            	  cursor.close();
	            	  }
	                  txn.abort();
	          } catch (DatabaseException dbe2) {
	       	   log.log( Level.SEVERE, "db problem " ,dbe2);
	       	   //dbe2.printStackTrace();
	          }
		}
	            
		
	}
	
	public void delete_by_date (String date, CallBack callback) {
		 Transaction txn = null;
	   	 try { 
	   		 
	   		 TransactionConfig config = new TransactionConfig();
	     //   config.setNoSync(true);
	            txn = bdbEnv.getEnv().beginTransaction(null, config);
			    
	            
	            SecondaryCursor  cursor = null;
	     	  
	     	       
	     		   DatabaseEntry key = new DatabaseEntry(date.getBytes("UTF-8"));
	     		
	     		   DatabaseEntry data = new DatabaseEntry( );
	     	       // Open the cursor. 
	     	       cursor =  bdbEnv.getIndexDateDb().openSecondaryCursor(txn, null);
	     	       log.finest(date);
	     	       OperationStatus status =  cursor.getSearchKeyRange(key,  data,  LockMode.DEFAULT); 
	     	       log.finest("after cursor");
	     	       if (status == OperationStatus.SUCCESS) {
	     	    	 
	     			 //  printRecord(m);
	     			   while (cursor.getPrev(key, data, LockMode.DEFAULT) == 
	            	        OperationStatus.SUCCESS) {
	     				   
	     				   
	     				   ResourceBinding binding = new ResourceBinding();
	    				   ResourceRecord record = (ResourceRecord) binding.entryToObject(data);
	    				   String uuid = record.getDupId();
	    				   String datestr = record.getDate();
	    				   String url = record.getUrl();
	    				   DatabaseEntry rkey =
	    			    	   new DatabaseEntry(  (url +"|"+datestr).getBytes("UTF-8"));
	    				   DatabaseEntry dkey = new DatabaseEntry(uuid.getBytes("UTF-8"));
	    				   bdbEnv.getResourceRecordDb().delete(txn,rkey);
	    				   bdbEnv.getHeadersBlob().delete(txn, dkey);
	    				   
	    				    // txn.commit();
	    				   if(!datestr.equals(maxdate)){
	    				     callback.methodToCallBack(uuid);
	    				   }
	     			  
	     				   String fkey = new String(key.getData(), "UTF-8"); 
	     				   log.finest(fkey);
	     				
	     			   }
	     	       }
	     			   
	     			        
	            cursor.close();
  
	            txn.commitSync(); //need commiting after each thousend records?
	 	       
		    } catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			   log.log( Level.SEVERE, "db problem " ,e);
			
			  try {
	              if (txn != null)
	                  txn.abort();
	          } catch (DatabaseException dbe2) {
	       	   log.log( Level.SEVERE, "db problem " ,dbe2);
	       	  // dbe2.printStackTrace();
	          }
		}
	            
	}	
	
	
	
	
   //deleting by url; 
	/*
    public void   delete (String url) {
    	 Transaction txn = null;
   	 try { 
   		 
   		 TransactionConfig config = new TransactionConfig();
     //   config.setNoSync(true);
            txn = bdbEnv.getEnv().beginTransaction(null, config);
		    
            Cursor  cursor =  bdbEnv.getRangeRecordDb().openCursor(txn, null);
          
		    DatabaseEntry key = new DatabaseEntry(url.getBytes("UTF-8"));
		    DatabaseEntry foundData = new DatabaseEntry();
		    OperationStatus ret = cursor.getSearchKey(key, foundData, LockMode.DEFAULT);
	       while (ret == OperationStatus.SUCCESS) {
	    	   System.out.println("delete range");
	    	   cursor.delete();
	    	   ret = cursor.getNextDup(key, foundData, LockMode.DEFAULT);
	    	   
	       }
		       cursor.close();
		       
		       Cursor  cursord =  bdbEnv.getDigestDb().openCursor(txn, null);
		          
			  
			    DatabaseEntry foundDatad = new DatabaseEntry();
			    OperationStatus retd = cursord.getSearchKey(key, foundDatad, LockMode.DEFAULT);
		       if (retd ==
		           OperationStatus.SUCCESS) {
		    	   System.out.println("delete digest");
		    	    cursord.delete();   
		    	    
		    	   // retd = cursor.getNextDup(key, foundDatad, LockMode.DEFAULT); 
		       }
			       cursord.close();
			       
			       Cursor  cursorld =  bdbEnv.getlastdateDb().openCursor(txn, null);
			          
				   
				    DatabaseEntry foundDatald = new DatabaseEntry();
				    OperationStatus retld = cursorld.getSearchKey(key, foundDatald, LockMode.DEFAULT);
			       if (retld ==
			           OperationStatus.SUCCESS) {
			    	   System.out.println("delete lastdate");
			    	    cursorld.delete();
			    	 //   retld = cursor.getNextDup(key, foundDatald, LockMode.DEFAULT);    
			    	    
			       }
				       cursorld.close();
				       
		       
		       
	       DatabaseEntry secondaryKey =
	    	   new DatabaseEntry( url.getBytes("UTF-8"));
	    	   DatabaseEntry secData = new DatabaseEntry();
	    	   SecondaryCursor mySecCursor =
	    		   bdbEnv.getIndexUrlDb().openSecondaryCursor(txn, null);
	    	   OperationStatus retVal = mySecCursor.getSearchKey(secondaryKey,
	    			   foundData,
	    			   LockMode.DEFAULT);
	    			   while (retVal == OperationStatus.SUCCESS) {
	    			   				   
	    				   ResourceBinding binding = new ResourceBinding();
	    				   ResourceRecord record = (ResourceRecord) binding.entryToObject(foundData);
	    				   
	    				   String datestr = record.getDate();
	    				   DatabaseEntry rkey =
	    			    	   new DatabaseEntry(  (url +"|"+datestr).getBytes("UTF-8"));
	    				   bdbEnv.getResourceRecordDb().delete(txn,rkey);
	    				  
	    				   
	    				   
	    			  // mySecCursor.delete();
	    			   retVal = mySecCursor.getNextDup(secondaryKey,
	    			   foundData,
	    			   LockMode.DEFAULT);
	    			   }
	    			   mySecCursor.close();
	       txn.commitSync();
	       
	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		
		
		  try {
              if (txn != null)
                  txn.abort();
          } catch (DatabaseException dbe2) {
       	   
       	   dbe2.printStackTrace();
          }
	}
   	 
    	
   }
*/
	
}
