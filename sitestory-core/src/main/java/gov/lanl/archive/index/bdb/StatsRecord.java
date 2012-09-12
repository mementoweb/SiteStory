package gov.lanl.archive.index.bdb;

import java.util.Date;

public class StatsRecord {

	
	long num_files=0;
	long num_bytes=0;
	String ip="";
	String domain="";
	String start="";
	public String getDomain() {
	    return this.domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	//public String getIP() {
	  //  return this.ip;
	//}
	//public void setIP(String ip) {
		//this.ip = ip;
	//}
	
	public long getNumFiles() {
	    return this.num_files;
	}
	public   void setNumFiles(long num_files) {
	   this.num_files=num_files;
	}
	
	public long getNumBytes() {
	    return this.num_bytes;
	}
	public   void setNumBytes(long num_bytes) {
	   this.num_bytes=num_bytes;
	}
	public void setStart(String start){
		 this.start=start;
	}
	public String getStart(){
		 return this.start;
	}


}
