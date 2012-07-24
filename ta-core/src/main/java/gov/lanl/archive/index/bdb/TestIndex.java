package gov.lanl.archive.index.bdb;

import gov.lanl.archive.Index;
import gov.lanl.archive.Memento;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;

public class TestIndex {

public static void main(String[] args) throws Exception {
	
		
		IndexImplB idx = new IndexImplB();
		TestIndex test = new TestIndex();
		idx.open(false);
		Memento m = new Memento();
		 SimpleDateFormat  formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
		Date tesdate = formatter.parse("Mon, 21 Jun 2009 21:13:25 MDT");
		m.setAccessdate(new Date());
		//m.setAccessdate(tesdate);
		m.setMimetype("text/html");
		m.setId("ad-b2");
		m.setUrl("http://test3");
		m.setReqUrl("http://test3");
		m.setIp("123");
		m.setLength(3);
		m.setDigest("KWXDS9XYZA");
		//m.setLastmodified(new Date());
		m.setDupId("ad-b2344");
		idx.add (m);
		 
		//idx.NormalizeUrl("http://www.test3/../ba");
		//idx.NormalizeUrl("http://www.example.com/a%c2%b1b");
		//idx.NormalizeUrl("http://www.example.com/display?lang=en&article=fred");
		//idx.NormalizeUrl("HTTP://www.Example.com/index.html");
		//idx.NormalizeUrl("http://www.example.com/%7Eusername/");
		//idx.NormalizeUrl("http://www.example.com/bar.html#section1");
		//idx.NormalizeUrl("http://www.example.com/default.asp");
		//idx.NormalizeUrl("http://www.example.com/barfoo?bar=foo+bar");
		//idx.testdb(  idx.getDbEnvironment().getRangeRecordDb());
		test.testdb( idx.getDbEnvironment().getResourceRecordDb());
		
		
		
		 //idx.getTestMemento("http://test1", new Date());
		
		//?   idx.getTestKeyMemento( "http://test1", new Date());
		
		//System.out.println("digestdb");
		//idx.testdb(  idx.getDbEnvironment().getDigestDb());
		//System.out.println("datedb");
		//idx.testdb(  idx.getDbEnvironment().getlastdateDb());
		//String ldatestr = idx.getLast("http://test");
		
		
	
		//Memento mem = idx.getMemento("http://test", new Date(Long.parseLong(ldatestr)));
		 //SimpleDateFormat  formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
		//Date lastdate = formatter.parse("Mon, 21 Jun 3011 21:13:25 MDT");
	      //System.out.println ("almargedon:"+ lastdate.getTime());
		//System.out.println(Long.MAX_VALUE);
		
		//System.out.println(new Date(Long.parseLong("9223372036854775807")));
        // Date myDate =formatter.parse("Mon, 21 Jun 2010 21:13:25 MDT");
        // Date myDate =formatter.parse("Mon, 18 Oct 2010 15:31:10 MDT");
        // Date myDate = new Date(Long.parseLong("1287437470102"));
        Memento mem = idx.get("http://test3", new Date());
		idx.printRecord(mem);
		
		// idx.testSecdb(idx.getDbEnvironment().getIndexDateDb()) ;
		 //idx.testSecdb(idx.getDbEnvironment().getIndexDigestDb()) ;
		test.testSecdb(idx.getDbEnvironment().getIndexUrlDb()) ;
		// idx.testSecdb(idx.getDbEnvironment().getIndexIdDb()) ;
		 System.out.println("Test until");
		
			//idx.delete("http://test1");
		// idx.getUntil("1287691370325", null);
		//idx.getFirst("http://www.test3");
		 System.out.println("Test last");
		//idx.getLastTest("http://test");
		
		//idx.getLast("http://test3");
		idx.closeDatabases();
		idx.close();
	}
	


public void getTestKeyMemento( IndexImplB idx,String url, Date date) {
	Cursor cursor = null;
	 DatabaseEntry key;
	 System.out.println("test memento");
	try {
		
		//key = new DatabaseEntry(url.getBytes("UTF-8"));
	    //key.setPartial(0, url.getBytes("UTF-8").length, true); 
  //	 key.setPartial(true);
	   // key.setPartialLength(url.getBytes().length);
       Date testdate = new Date();
       key = new DatabaseEntry((url+"|"+Long.toString(testdate.getTime())).getBytes("UTF-8")); 
      DatabaseEntry data = new DatabaseEntry();
     //ResourceRecord rec = new ResourceRecord();
     //rec.setUrl(url);
    // rec.setDate(date.getTime());
     //rec.setType(1);
     ResourceBinding binding = new ResourceBinding();
     //binding.objectToEntry(rec, data);
    // data.setPartial(true);
     //data.setPartial(0, 1, true);
     // data.setSize(Integer.MAX_VALUE);
     //data.setOffset(1);
     //int max = Integer.MAX_VALUE +1;
    
     //data.setPartialLength(1);
     cursor =  idx.bdbEnv.getResourceRecordDb().openCursor(null, null);
    // Database urlsdb =   bdbEnv.openDatabase(true,  "urlindex"); 
    //cursor = urlsdb.openCursor(null, null);
     
   //  OperationStatus status =  cursor.getSearchBothRange(key,  data,  LockMode.READ_UNCOMMITTED); 
       OperationStatus status =   cursor.getSearchKeyRange(key, data, LockMode.DEFAULT);
     //OperationStatus status =  cursor.getSearchBothRange(key, data, LockMode.DEFAULT);
     if (status == OperationStatus.SUCCESS) {
    	   System.out.println("after success cursor");
    	     String keyString = new String(key.getData(), "UTF-8");
    	     System.out.println("key:"+keyString);
    	    // String dataString = new String(data.getData(), "UTF-8");
    	     //System.out.println("data:"+dataString);
    	   Memento m = new Memento();
    	  // System.out.println( "size:" +data.getSize());
    	   ResourceBinding binding1 = new ResourceBinding();
		  ResourceRecord record = (ResourceRecord) binding1.entryToObject(data);
		   idx.ResourceRecordToMemento(record, m);
		   
		   idx.printRecord(m);
		  // DatabaseEntry datap = new DatabaseEntry();
		  while ( cursor.getPrev(key, data,LockMode.DEFAULT)==OperationStatus.SUCCESS) {
			  System.out.println("after next cursor");
			   keyString = new String(key.getData(), "UTF-8");
	    	 System.out.println("key:"+keyString);
	    	  //    dataString = new String(data.getData(), "UTF-8");
	    	    // System.out.println("data:"+dataString);
			  
			  
		   //if (status == OperationStatus.SUCCESS) {
			  Memento m2 = new Memento();
	    	  
			   ResourceRecord record1 = (ResourceRecord) binding.entryToObject(data);
			   idx.ResourceRecordToMemento(record1, m2);
			   idx.printRecord(m2);
		   }
		   
		   
     }
	
     cursor.close();
   //  urlsdb.close();
} catch (UnsupportedEncodingException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
}



public void  testdb( Database db) throws UnsupportedEncodingException {
	   
	   Cursor cursor = null;
	   try {
	       
	      
	       
	       // Open the cursor. 
	       cursor =  db.openCursor(null, null);

	       // Cursors need a pair of DatabaseEntry objects to operate. These hold
	       // the key and data found at any given position in the database.
	       DatabaseEntry foundKey = new DatabaseEntry();
	       DatabaseEntry foundData = new DatabaseEntry();

	       // To iterate, just call getNext() until the last database record has 
	       // been read. All cursor operations return an OperationStatus, so just
	       // read until we no longer see OperationStatus.SUCCESS
	       while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) ==
	           OperationStatus.SUCCESS) {
	           // getData() on the DatabaseEntry objects returns the byte array
	           // held by that object. We use this to get a String value. If the
	           // DatabaseEntry held a byte array representation of some other 
	           // data type (such as a complex object) then this operation would
	           // look considerably different.
	           String keyString = new String(foundKey.getData(), "UTF-8");
	           String dataString = new String(foundData.getData(), "UTF-8");
	           System.out.println("Key | Data : " + keyString + " | " + 
	                          dataString + "");
	       }
	   } catch (DatabaseException de) {
	       System.err.println("Error accessing database." + de);
	   } finally {
	       // Cursors must be closed.
	       cursor.close();
	   }
	   
	   
}


public String  getLastTest(IndexImplB idx,String url){
	   
	   Cursor cursor = idx.bdbEnv.getResourceRecordDb().openCursor(null, null); 	 
	try {
		  DatabaseEntry key = new DatabaseEntry(url.getBytes("UTF-8"));
		  Date date = new Date();
	      DatabaseEntry data = new DatabaseEntry( Long.toString(date.getTime()).getBytes("UTF-8"));
	    //  DatabaseEntry data = new DatabaseEntry();
	     //  OperationStatus status =  cursor.getSearchBothRange(key, data,  LockMode.DEFAULT); 
	      OperationStatus status =   cursor.getSearchKey(key, data,LockMode.DEFAULT);
	     System.out.println( "Cursor size"+cursor.count());
	     DatabaseEntry data0 = new DatabaseEntry();
	     //                            cursor.getRecordNumber(data0,LockMode.DEFAULT);
	     
	     System.out.println();
	      //OperationStatus status  = cursor.getPrevDup(key, data,  LockMode.DEFAULT);
	        if (status == OperationStatus.SUCCESS) {
	        	cursor.getLast(key, data,LockMode.DEFAULT);
	    	   // cursor.getPrevDup(key, data,LockMode.DEFAULT);
  	   String foundData = new String(data.getData(), "UTF-8"); 
  	    System.out.println("Last in Str:"+foundData);
  	    Date next = new Date(Long.parseLong(foundData));
         System.out.println("Last:"+next);
  	    return foundData;   
       }
	   
   } catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
   finally {
  	    // Cursors must be closed.
  	    cursor.close();
  	}

return null;
}



public void getTestMemento(IndexImplB idx, String url, Date date) {
	Cursor cursor = null;
	 DatabaseEntry key;
	 System.out.println("test memento");
	try {
		
		key = new DatabaseEntry(url.getBytes("UTF-8"));
	    //key.setPartial(0, url.getBytes("UTF-8").length, true); 
  //	 key.setPartial(true);
	   // key.setPartialLength(url.getBytes().length);
       Date testdate = new Date();
      DatabaseEntry data = new DatabaseEntry((url+"|"+Long.toString(testdate.getTime())).getBytes("UTF-8")); 
     //ResourceRecord rec = new ResourceRecord();
     //rec.setUrl(url);
    // rec.setDate(date.getTime());
     //rec.setType(1);
     ResourceBinding binding = new ResourceBinding();
     //binding.objectToEntry(rec, data);
    // data.setPartial(true);
     //data.setPartial(0, 1, true);
     // data.setSize(Integer.MAX_VALUE);
     //data.setOffset(1);
     //int max = Integer.MAX_VALUE +1;
    
     //data.setPartialLength(1);
     //cursor =  bdbEnv.getResourceRecordDb().openCursor(null, null);
     Database urlsdb =  idx.bdbEnv.openDatabase(true,  "urlindex"); 
    cursor = urlsdb.openCursor(null, null);
     
   //  OperationStatus status =  cursor.getSearchBothRange(key,  data,  LockMode.READ_UNCOMMITTED); 
      // OperationStatus status =   cursor.getSearchKeyRange(key, data, LockMode.DEFAULT);
     OperationStatus status =  cursor.getSearchBothRange(key, data, LockMode.DEFAULT);
     if (status == OperationStatus.SUCCESS) {
    	   System.out.println("after success cursor");
    	     String keyString = new String(key.getData(), "UTF-8");
    	     System.out.println("key:"+keyString);
    	     String dataString = new String(data.getData(), "UTF-8");
    	     System.out.println("data:"+dataString);
    	   //Memento m = new Memento();
    	  // System.out.println( "size:" +data.getSize());
    	   //ResourceBinding binding1 = new ResourceBinding();
		//   ResourceRecord record = (ResourceRecord) binding1.entryToObject(data);
		  // ResourceRecordToMemento(record, m);
		   
		  // printRecord(m);
		  // DatabaseEntry datap = new DatabaseEntry();
		  while ( cursor.getPrevDup(key, data,LockMode.DEFAULT)==OperationStatus.SUCCESS) {
			  System.out.println("after next cursor");
			   keyString = new String(key.getData(), "UTF-8");
	    	     System.out.println("key:"+keyString);
	    	      dataString = new String(data.getData(), "UTF-8");
	    	     System.out.println("data:"+dataString);
			  
			  
		   //if (status == OperationStatus.SUCCESS) {
			//  Memento m2 = new Memento();
	    	  
			  // ResourceRecord record1 = (ResourceRecord) binding.entryToObject(data);
			  // ResourceRecordToMemento(record1, m2);
			//   printRecord(m2);
		   }
		   
		   
     }
	
     cursor.close();
     urlsdb.close();
} catch (UnsupportedEncodingException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
}





	public void  testSecdb( SecondaryDatabase db) throws UnsupportedEncodingException {
		   
		 SecondaryCursor  cursor = null;
		   try {
		       
		      
		       
		       // Open the cursor. 
		       cursor =  db.openSecondaryCursor(null, null);
		     
		       // Cursors need a pair of DatabaseEntry objects to operate. These hold
		       // the key and data found at any given position in the database.
		       DatabaseEntry foundKey = new DatabaseEntry();
		       DatabaseEntry foundData = new DatabaseEntry();

		       // To iterate, just call getNext() until the last database record has 
		       // been read. All cursor operations return an OperationStatus, so just
		       // read until we no longer see OperationStatus.SUCCESS
		       while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) ==
		           OperationStatus.SUCCESS) {
		           // getData() on the DatabaseEntry objects returns the byte array
		           // held by that object. We use this to get a String value. If the
		           // DatabaseEntry held a byte array representation of some other 
		           // data type (such as a complex object) then this operation would
		           // look considerably different.
		           String keyString = new String(foundKey.getData(), "UTF-8");
		           String dataString = new String(foundData.getData(), "UTF-8");
		           System.out.println("Key | Data : " + keyString + " | " + 
		                          dataString + "");
		       }
		   } catch (DatabaseException de) {
		       System.err.println("Error accessing database." + de);
		   } finally {
		       // Cursors must be closed.
		       cursor.close();
		   }
		   
		   
	   }
	
	
}
