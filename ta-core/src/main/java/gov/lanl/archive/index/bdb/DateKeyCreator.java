package gov.lanl.archive.index.bdb;

import com.sleepycat.je.SecondaryKeyCreator;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.SecondaryKeyCreator;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;

import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;

public class DateKeyCreator implements SecondaryKeyCreator {
//	private TupleBinding theBinding;
	// public DateKeyCreator(TupleBinding theBinding1) {
      //   theBinding = theBinding1;
 //}
	
	 public boolean createSecondaryKey(SecondaryDatabase secDb,
             DatabaseEntry keyEntry, 
             DatabaseEntry dataEntry,
             DatabaseEntry resultEntry) {

try {
//ResourceRecord pd = 
//(ResourceRecord) theBinding.entryToObject(dataEntry);

ResourceBinding binding = new ResourceBinding();
ResourceRecord record = (ResourceRecord) binding.entryToObject(dataEntry);
//long accessDate = record.getDate();
String accessDate =record.getDate();
//System.out.println("date from sec:"+accessDate);
//ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
//DataOutputStream dos = new DataOutputStream(baos); 
//dos.writeLong(accessDate);
//dos.flush();
//resultEntry.setData(baos.toByteArray());
resultEntry.setData(accessDate.getBytes("UTF-8"));
} catch (IOException e) {
	  e.printStackTrace();
}
return true;
}
	 
}
