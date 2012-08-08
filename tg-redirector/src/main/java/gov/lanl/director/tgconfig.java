package gov.lanl.director;

import java.util.Date;
import java.util.List;

public class tgconfig {
	private String url; //canonical
	private Date sdate;
	private Date edate;
	private List exp;
	
	
	public Date getStartdate() {
	    return this.sdate;
	}
	public void setStartdate(Date sdate) {
		this.sdate = sdate;
	}
	public Date getEnddate() {
	    return this.edate;
	}
	public void setEnddate(Date edate) {
		this.edate = edate;
	}
	
	public String getUrl() {
	    return this.url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
	public boolean checkTimeGate(Date date,String id) {
		boolean status = false;
		System.out.println("from tgconfig");
		System.out.println("endate:"+edate);
		System.out.println("startdate:"+sdate);
		
		//omit regexpressions for now
		if (date.after(sdate)&&date.before(edate)) {
		return true;
		}
		else if (date.equals(sdate)){
			return true;
		}
		else if (date.equals(edate)){
			return true;
		}
		else {
			return false;
		}
		
	}
	
	
}
