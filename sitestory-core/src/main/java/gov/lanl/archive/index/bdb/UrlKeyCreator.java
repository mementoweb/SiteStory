package gov.lanl.archive.index.bdb;

import com.sleepycat.je.SecondaryKeyCreator;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.SecondaryKeyCreator;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import java.io.IOException;

public class UrlKeyCreator implements SecondaryKeyCreator {
	private ResourceBinding theBinding;
	 public UrlKeyCreator(ResourceBinding theBinding1) {
         theBinding = theBinding1;
 }
	
	 public boolean createSecondaryKey(SecondaryDatabase secDb,
             DatabaseEntry keyEntry, 
             DatabaseEntry dataEntry,
             DatabaseEntry resultEntry) {

try {
//ResourceRecord pd = (ResourceRecord) theBinding.entryToObject(dataEntry);
//ResourceBinding binding = new ResourceBinding();
ResourceRecord record = (ResourceRecord) theBinding.entryToObject(dataEntry);
String url  = record.getUrl();
String type = record.getType();
//System.out.println("url from sec:"+url);
resultEntry.setData((type+"|"+url).getBytes("UTF-8"));
} catch (IOException e) {
	  e.printStackTrace();
}
return true;
}
	 
}
