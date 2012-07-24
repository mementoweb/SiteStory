package gov.lanl.archive;

import java.util.Date;

/*
@author Lyudmila Balakireva
*/

public class MementoFacts {
private final String id; 
private final String accessdate;
private final String digest;
	
private final String mimetype;
private final	long length;
private final	String ip;
private final	String url;
	
private final	String lastmodified;
private final	String dupid;
	
	MementoFacts next ;
	MementoFacts prev ;
	MementoFacts first ;
	MementoFacts last ;
	
	//int statuscode; //do i use it 
    
	
	public class MementoBuilder {
		private final String url;
		private final String accessdate;
		
		private String id =""; 
		private String digest="";
		private long length = 0;
		private  String mimetype ="";
		private String ip ="";
		private String lastmodified="0";
		private String dupid ="";
		private MementoFacts next = null;
		private MementoFacts prev = null;
		private MementoFacts first = null;
		private MementoFacts last = null;
		
		
		public MementoBuilder (String url,Date accessdate) {
			this.url = url;
			this.accessdate = Long.toString(accessdate.getTime());
			
		}
		
		public MementoBuilder (String url,String accessdate) {
			this.url = url;
			this.accessdate = accessdate;
			
		}
		public MementoBuilder id(String val) {
			{ id = val;return this; }
		}
		
		public MementoBuilder digest(String val) {
			{digest =val;return this; }
		}
		
		public MementoBuilder dupid(String val) {
			{dupid=val;return this; }
		}
		public MementoBuilder lastmodified (String val) {
			{lastmodified=val;return this; }
		}
		
				
		public MementoBuilder lastmodifiedasDate (Date val) {
			{lastmodified = Long.toString(val.getTime());
			return this; }
		}
				
		public MementoBuilder mimetype (String val) {
			{mimetype=val;return this; }
		}
		public MementoBuilder length (long val) {
			{length=val;return this; }
		}
		public MementoBuilder ip (String val) {
			{ip = val;return this; }
		}
		
		public MementoBuilder next (MementoFacts val) {
			{ next = val;return this; }
		}
		public MementoBuilder prev (MementoFacts val) {
			{ prev = val;return this; }
		}
		public MementoBuilder last (MementoFacts val) {
			{ last = val;return this; }
		}
		
		public MementoBuilder first (MementoFacts val) {
			{ first = val;return this; }
		}
		
	    public MementoFacts build() {
				return new MementoFacts(this);
		}
		
	}
	
	private MementoFacts (MementoBuilder builder) {
		 url= builder.url;
		 accessdate = builder.accessdate;
		 id = builder.id;
		 digest = builder.digest;
		 ip = builder.ip;
		 length = builder.length;
		 mimetype = builder.mimetype;
		 lastmodified = builder.lastmodified;
		 dupid=builder.dupid;
		 last=builder.last;
		 first=builder.first;
		 next=builder.next;
		 prev=builder.prev;
	}
	
	public String getLastmodified() {
	    return this.lastmodified;
	}	
	public Date getLastmodifiedasDate() {
		return  new Date(Long.parseLong(this.lastmodified));
	}
	public String getAccessdate() {
	    return this.accessdate;
	}
	public Date getAccessdateasDate() {
	    return  new Date(Long.parseLong(this.lastmodified));
	}
	public long getLength() {
	    return this.length;
	}
	public String getIp() {
	    return this.ip;
	}
	public String getUrl() {
	    return this.url;
	}
	public String getMimetype() {
	    return this.mimetype;
	}
	public String getId() {
	    return this.id;
	}
	
    public String getDigest() {
	    return this.digest;
	}
    public String getDupId() {
        return this.dupid;
    }
    public MementoFacts getFirstMemento() {
        return this.first;
    }
    public MementoFacts getLastMemento() {
        return this.last;
    }
    public MementoFacts getPrevMemento() {
        return this.prev;
    }
    public MementoFacts getNextMemento() {
        return this.next;
    }
}
