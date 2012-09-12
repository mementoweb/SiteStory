package gov.lanl.archive.index.bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class StatsBinding extends TupleBinding{

	
	public void objectToEntry(Object obj, TupleOutput to) {
    	StatsRecord record = (StatsRecord) obj;
    
    	to.writeString(record.getDomain());
    	//to.writeString(record.getIP()); 
        to.writeLong(record.getNumFiles());
        to.writeLong(record.getNumBytes());
        to.writeString(record.getStart());
     
    }
		
   public Object entryToObject(TupleInput ti) {
	    	StatsRecord record = new StatsRecord();
	    	record.setDomain(ti.readString());
	    	//record.setIP(ti.readString());
	    	record.setNumFiles(ti.readLong());
	    	record.setNumBytes(ti.readLong());
	    	record.setStart(ti.readString());
	        return record;
	    }

	
}
