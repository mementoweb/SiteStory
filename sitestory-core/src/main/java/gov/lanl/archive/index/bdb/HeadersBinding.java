package gov.lanl.archive.index.bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class HeadersBinding extends TupleBinding{

	
	public void objectToEntry(Object obj, TupleOutput to) {
    	HeadersRecord record = (HeadersRecord) obj;
    
    	to.writeString(record.getId());
    	to.writeString(record.getIP()); 
        to.writeString(record.getReqHeaders());
        to.writeString(record.getResHeaders());
     
    }
		
   public Object entryToObject(TupleInput ti) {
	    	HeadersRecord record = new HeadersRecord();
	    	record.setId(ti.readString());
	    	record.setIP(ti.readString());
	    	record.setReqHeaders(ti.readString());
	    	record.setResHeaders(ti.readString());	    	
	        return record;
	    }

	
}
