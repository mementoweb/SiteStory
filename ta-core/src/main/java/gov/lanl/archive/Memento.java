package gov.lanl.archive;

import java.util.Date;



/*
@author Lyudmila Balakireva
*/

public class Memento {
private String id; 
private Date accessdate;
private Date firstdate = null;
private Date lastdate = null;
private Date nextdate = null;
private Date prevdate = null;
private String digest;
private String method;
private String mimetype;
private String lang="";
private String compress="";
private long length;
private long  req_headers_length;
private long  res_headers_length;
private String ip = null;
private String requrl;
private String url; //canonical
private String reqheaders;
String resheaders;
String code="";
int counter;
//Date lastmodified;
// responces or structures for dates in between  dates in between
Memento next ;
Memento prev ;
Memento first ;
Memento last ;
int statuscode;
String type;
int otype;
String dupid;

public Memento () {
	
}


public String getId() {
    return this.id;
}
public void setId(String id) {
	this.id = id;
}
public String getCode() {
    return this.code;
}
public void setCode(String code) {
	this.code = code;
}

public String getDupId() {
    return this.dupid;
}
public void setDupId(String dupid) {
	this.dupid = dupid;
}

//revisit by digest
public String getType() {
    return this.type;
}
public void setType(String type) {
	this.type = type;
}

//revisit by digest/
//public int getOtype() {
  //  return this.otype;
//}
//public void setOtype(int type) {
	//this.otype = type;
//}


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

public String getIp() {
    return this.ip;
}
public void setIp(String ip) {
	this.ip = ip;
}

public String getUrl() {
    return this.url;
}
public void setUrl(String url) {
	this.url = url;
}

public String getReqUrl() {
    return this.requrl;
}
public void setReqUrl(String url) {
	this.requrl = url;
}

public String getReqheaders() {
    return this.reqheaders;
}

public void setReqheaders(String reqheaders) {
	this.reqheaders = reqheaders;
}
public String getResheaders() {
    return this.resheaders;
}
public void setResheaders(String resheaders) {
	this.resheaders = resheaders;
}

public long getReqheaderslength() {
    return this.req_headers_length;
}
public void setReqheaderslength(long length) {
	this.req_headers_length = length;
}
public long getResheaderslength() {
    return this.res_headers_length;
}
public void setResheaderslength(long length) {
	this.res_headers_length = length;
}

public long getLength() {
    return this.length;
}
public void setLength(long length) {
	this.length = length;
}
public int getStatusCode() {
    return this.statuscode;
}
public void setStatuscode(int statuscode) {
	this.statuscode = statuscode;
}
public Date getAccessdate() {
    return this.accessdate;
}
public void setAccessdate(Date accessdate) {
	this.accessdate = accessdate;
}
//public Date getLastmodified() {
//    return this.lastmodified;
//}
//public void setLastmodified(Date lastmodified) {
	//this.lastmodified = lastmodified;
//}
//check this may be not used;
public Date getFirstdate() {
    return this.firstdate;
}
public void setFirstdate(Date firstdate) {
	this.firstdate = firstdate;
}

public Date getLastdate() {
    return this.lastdate;
}
public void setLastdate(Date lastdate) {
	this.lastdate = lastdate;
}
public Date getNextdate() {
    return this.nextdate;
}
public void setNextdate(Date nextdate) {
	this.nextdate = nextdate;
}
public Date getPrevdate() {
    return this.prevdate;
}
public void setPrevdate(Date prevdate) {
	this.prevdate = prevdate;
}



public int getCounter() {
    return this.counter;
}
public void setCounter(int counter) {
	this.counter = counter;
}

public Memento getNextMemento() {
    return this.next;
}
public void setNextMemento(Memento next) {
	this.next = next;
}
public Memento getPrevMemento() {
    return this.prev;
}
public void setPrevMemento(Memento prev) {
	this.prev = prev;
}

public Memento getLastMemento() {
    return this.last;
}
public void setLastMemento(Memento last) {
	this.last = last;
}

public Memento getFirstMemento() {
    return this.first;
}
public void setFirstMemento(Memento first) {
	this.first = first;
}




}
