package gov.lanl.archive.resource;

import gov.lanl.archive.ArchiveConfig;
import gov.lanl.archive.Index;



import java.net.URI;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

/*
@author Lyudmila Balakireva
*/

public class MementoCommons {
	   static ThreadSafeSimpleDateFormat  httpformatter;
	   static ThreadSafeSimpleDateFormat formatterout;
	   ThreadSafeSimpleDateFormat dtformatter;
	  // private static Index idx;
	   static final List mementoresourcesupportedformats = new ArrayList();
	   static final List  dtsupportedformatsv = new ArrayList();
	//   static List dtsupportedformats = new ArrayList();
	   
	   static  URI baseUri;
	   static {
	       
	        TimeZone tz = TimeZone.getTimeZone("UTC");
	        httpformatter = new ThreadSafeSimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");	     
	        TimeZone tzo = TimeZone.getTimeZone("GMT");
	        httpformatter.setTimeZone(tzo);
	        formatterout = new ThreadSafeSimpleDateFormat("yyyyMMddHHmmss");
	        formatterout.setTimeZone(tz);
	        ThreadSafeSimpleDateFormat dtformatter = new ThreadSafeSimpleDateFormat("E, dd MMM yyyy"); 
	      //  idx = ArchiveConfig.getMetadataIndex();
	        mementoresourcesupportedformats.add(formatterout);
	     
	        dtsupportedformatsv.add(new ThreadSafeSimpleDateFormat("E, dd MMM yyyy HH:mm:ss z"));
			dtsupportedformatsv.add( new ThreadSafeSimpleDateFormat("E, dd MMM yyyy z"));
			dtsupportedformatsv.add( new ThreadSafeSimpleDateFormat("E, dd MMM yyyy"));
			// MyServletContextListener cl= MyServletContextListener.getInstance();
			//    cl.setAttribute("idx", idx);
	    }
		  
	   public	MementoCommons( URI baseUri )
	   {
	       this.baseUri = baseUri;
	   }
	   
	   
   	public static  String  composeLink(Date date,String id,String type) {
		 String str = ", <"+ baseUri.toString() +"memento/"+formatterout.format(date)+"/"+id+">;rel=\""+type+"\"; datetime=\"" +httpformatter.format(date)+ "\""; 
		 return str;
	}
	public static  String  composeMemUrl(Date date,String id,String type) {
		 String str = baseUri.toString() +"memento/"+formatterout.format(date)+"/"+id; 
		 return str;
	}
	
	public String composeLinkHeader(Date memento, Date l,Date f,String id) {
		StringBuffer sb = new StringBuffer();
		String mem = composeLink(memento,id,"memento");
		
		String mfl = null;
		if ( (memento.equals(f)) && memento.equals(l)) {
			mfl = composeLink(memento,id,"memento first last");
		}
		else if (memento.equals(f)){
			
			mfl = composeLink(memento,id,"memento first");
			mfl = mfl + composeLink(l,id,"memento last");
			
		
		}	
		else if (memento.equals(l)) {
			mfl = composeLink(memento,id,"memento last");
			mfl = mfl + composeLink(f,id,"memento first");
			
		}	
		else  {
		
			mfl = mem ;
			mfl = mfl +composeLink(l,id,"memento last");
			mfl = mfl + composeLink(f,id,"memento first");
			
		
		}
		
		
		return mfl;
	}
	
	
	
	
   	public  Date checkMementoDateValidity(String httpdate){
   		System.out.println("mementoformat");
   	 Date d=checkDateValidity( httpdate ,mementoresourcesupportedformats );
   	 return d;
   	}
   	
   	
	public  Date checkDtDateValidity(String httpdate){
		System.out.println("dtformat");
	   	 Date d=checkDateValidity( httpdate ,  dtsupportedformatsv );
	   	 return d;
	   	}
	   	
	public  static Date checkDateValidity(String httpdate , List list) {
		System.out.println("validity check"+httpdate);
		Date d= null;
		    Iterator it  = list.iterator();
		    int count=0;
		     while (it.hasNext()) {
		    	 ThreadSafeSimpleDateFormat formatter =  (ThreadSafeSimpleDateFormat) it.next();
			 try {
				      TimeZone tzo = TimeZone.getTimeZone("GMT");
				      formatter.setTimeZone(tzo);
				      count = count+1;
		              d = formatter.parse(httpdate);
		              System.out.println("format found" +count);
		             break;
		             }

		           catch (Exception e) {
		        	  System.out.println("attempt to parse"+ count );
		           // TODO Auto-generated catch block                                                                                                                                            
		          // e.printStackTrace();
		          } 
			 
		     }
		     
		     
		     return d;
	    }
	
	
}
