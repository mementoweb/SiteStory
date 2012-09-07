package gov.lanl.director;



import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.DefaultExpressionEngine;

public class AppConfig {
	 //static MementoCommons mc;
	 static SortedMap map;
	// static SortedMap lmap;
	 static List list ;
	 static List timemapindex;
	 
	// protected final URI baseUri;
	 static final List  dtsupportedformatsv = new ArrayList();
	 
	 static {
		    dtsupportedformatsv.add(new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z"));
			//dtsupportedformatsv.add( new SimpleDateFormat("E, dd MMM yyyy z"));
			//dtsupportedformatsv.add( new SimpleDateFormat("E, dd MMM yyyy"));
	 }
	 
	 
	  public  SortedMap getmap() {
		  return this.map;
	  }
	 
	  
	  public List getList() {
		  return list;
	  }
	  public List getTMList() {
		  return timemapindex;
	  }
	 
	   
	public static void processConfig()  {
		 /*
		  <timegates>
         <timegate uri=" base uri of timegate ">
         <start> datetime </start>
         <end> datetime </end>
         <regex> URI regular expression </regex>
         <regex> URI regular expression </regex>
         <timemap  uri="http://www.theresourcedepot.com/000010/timemap/link/"/>
         </timegate>
         </timegates>
		  * 
		  */
		 try {
		 ClassLoader cl = AppConfig.class.getClassLoader();
		

		   //URL u = ClassLoader.getSystemResource("timegates.xml");
		 XMLConfiguration config = new XMLConfiguration();
		
		//InputStream in = .getClass().getClassLoader().getResourceAsStream("timegates.xml");
		
		InputStream in;

        if (cl != null) {
           in = cl.getResourceAsStream("timegates.xml");
        } else {
            in = ClassLoader.getSystemResourceAsStream("timegates.xml");
            
        }
       
        System.out.println("before load");
       
        config.load(in);
        
         StringWriter stringWriter = new StringWriter();
         config.save(stringWriter);
         System.out.println(stringWriter.toString());
          DefaultExpressionEngine engine = new DefaultExpressionEngine();
	                              engine.setAttributeEnd(null);
		                          engine.setAttributeStart(engine.getPropertyDelimiter());
		                         
		 config.setExpressionEngine(engine);  
        // config.setExpressionEngine(new XPathExpressionEngine());
		
		 System.out.println("after load");
		 Object prop = config.getProperty("timegate");
		 list = new ArrayList(); 
		 map = new TreeMap();
		 timemapindex = new ArrayList();
        
		List ltimegates = config.getList("timegate.uri");
		System.out.println("list size from tg-director config:"+ltimegates.size());
		 
        if (ltimegates.size()>0) {
       	 
       	 for (int i=0;i<ltimegates.size();i++) {
       		 String url  =  (String) config.getProperty("timegate("+i+").uri");
       		 System.out.println("url"+url);
       		 tgconfig tg = new tgconfig();
       		 tg.setUrl(url);
       		 String sdatestr = (String) config.getProperty("timegate("+i+").start");
       		 String edatestr = (String) config.getProperty("timegate("+i+").end");
       		 String timemap = (String) config.getProperty("timegate("+i+").timemap.uri");
       		 System.out.println("Start date from " + sdatestr);
       		     Date sdate = checkDtDateValidity(sdatestr);
       		     Date edate = checkDtDateValidity(edatestr);
       		 List regexvalues = config.getList("timegate("+i+").regex");
       		 
       		 map.put(edate,url);
       		 tg.setEnddate(edate);
       		 tg.setStartdate(sdate);
       		 list.add(i,tg);
       		 timemapindex.add (i,timemap);
       	 }
       	 
       	 
        }
		 }
		 catch (Exception e) {
	           // TODO Auto-generated catch block
	           e.printStackTrace();
    }

	 }
	
	public static  Date checkDtDateValidity(String httpdate){
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
		    	 SimpleDateFormat formatter =  (SimpleDateFormat) it.next();
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
