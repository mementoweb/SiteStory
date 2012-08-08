package gov.lanl.archive.index.bdb;

import java.util.Date;

public class ResourceRecord {

	String id=""; 
	
	String digest="";
	String type="1"; // 1 first observation,0 revisit; 
	String mimetype="";
	long length = 0;
	long reshlength = 0;
	long reqhlength = 0;
	//String ip="";
	String url; //need original uri 
	String date;
	//long lastmodified=0;
	String dupid="";
	String lang="";
	String compress="";
	String code="";
	
	public String getId() {
	    return this.id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
	    return this.type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDupId() {
	    return this.dupid;
	}
	public void setDupId(String dupid) {
		this.dupid = dupid;
	}
	
	public String getDigest() {
	    return this.digest;
	}
	public void setDigest(String digest) {
		this.digest = digest;
	}
	public String getMimetype() {
	    return this.mimetype;
	}
	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public String getLang() {
	    return this.lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	public String getCompress() {
	    return this.compress;
	}
	public void setCompress(String compress) {
		this.compress = compress;
	}
	public String getCode() {
	    return this.code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	//public String getIp() {
	  //  return this.ip;
	//}
	//public void setIp(String ip) {
		//this.ip = ip;
	//}
	
	public long getLength() {
	    return this.length;
	}
	public void setLength(long length) {
		this.length = length;
	}
	
	public long getResLength() {
	    return this.reshlength;
	}
	public void setResLength(long length) {
		this.reshlength = length;
	}
	public long getReqLength() {
	    return this.reqhlength;
	}
	public void setReqLength(long length) {
		this.reqhlength = length;
	}
	
	//public long getLastmodified() {
	  //  return this.lastmodified;
	//}
	//public void setLastmodified(long lastmodified) {
		//this.lastmodified = lastmodified;
	//}

	public String getUrl() {
	    return this.url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getDate() {
	    return this.date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
}
