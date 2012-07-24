package gov.lanl.archive.index.bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ResourceBinding extends TupleBinding{

	
	/*  public void objectToEntry(Object obj, TupleOutput to) {
	    	ResourceRecord record = (ResourceRecord) obj;
	        to.writeString(record.getId());
	        to.writeString(record.getMimetype());
	        to.writeString(record.getDigest());
	        to.writeString(record.getIp());
	         to.writeLong(record.getLength());
	        to.writeString(record.getDate()); 
	        to.writeString(record.getUrl());
	        to.writeString(record.getLastmodified());
	        to.writeString(record.getDupId());
	    }

	  */
	
	
	public void objectToEntry(Object obj, TupleOutput to) {
    	ResourceRecord record = (ResourceRecord) obj;
    
    	to.writeString(record.getType());
    	to.writeString(record.getDate()); 
        to.writeString(record.getId());
        to.writeString(record.getMimetype());
        to.writeString(record.getDigest());
      //  to.writeString(record.getIp());
         to.writeLong(record.getLength());
        to.writeString(record.getUrl());
      //  to.writeLong(record.getLastmodified());
        to.writeString(record.getDupId());
        to.writeLong(record.getReqLength());
        to.writeLong(record.getResLength());
        to.writeString(record.getCompress());
        to.writeString(record.getLang());
        to.writeString(record.getCode());
    }
	
	
	    public Object entryToObject(TupleInput ti) {
	    	ResourceRecord record = new ResourceRecord();
	    	record.setType(ti.readString());
	    	record.setDate(ti.readString());
	    	record.setId(ti.readString());
	    	record.setMimetype(ti.readString());
	    	record.setDigest(ti.readString());
	    	//record.setIp(ti.readString());
	        record.setLength(ti.readLong());
	        record.setUrl(ti.readString());
	      //  record.setLastmodified(ti.readLong());
	        record.setDupId(ti.readString());
	        record.setReqLength(ti.readLong());
	        record.setResLength(ti.readLong());
	      if(  ti.available() >0 ) {
	        record.setCompress(ti.readString());
	        record.setLang(ti.readString());
	      }
	      if ( ti.available() >0) {
	    	  record.setCode(ti.readString());
	      }
	        return record;
	    }

	
}
