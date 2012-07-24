package gov.lanl.archive.index.bdb;

import java.util.Date;

public class HeadersRecord {

	String id=""; 
	String req_headers="";
	String res_headers="";
	String ip="";
	
	public String getId() {
	    return this.id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getIP() {
	    return this.ip;
	}
	public void setIP(String ip) {
		this.ip = ip;
	}
	
	public String getReqHeaders() {
	    return this.req_headers;
	}
	public   void setReqHeaders(String headers) {
	   this.req_headers=headers;
	}
	
	public String getResHeaders() {
	    return this.res_headers;
	}
	public   void setResHeaders(String headers) {
	   this.res_headers=headers;
	}
	
	


}
