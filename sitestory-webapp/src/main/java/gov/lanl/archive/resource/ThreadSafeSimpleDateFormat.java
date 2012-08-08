package gov.lanl.archive.resource;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class ThreadSafeSimpleDateFormat {

	 private DateFormat df;

	 public ThreadSafeSimpleDateFormat(String format) {
	     this.df = new SimpleDateFormat(format);
	 }

	 public synchronized String format(Date date) {
	     return df.format(date);
	 }

	 public synchronized Date parse(String string) throws ParseException {
	     return df.parse(string);
	 }

	public synchronized void setTimeZone(TimeZone tzo) {
		// TODO Auto-generated method stub
		df.setTimeZone(tzo);
	}
	}