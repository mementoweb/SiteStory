package gov.lanl.archive.resource;

import gov.lanl.archive.ArchiveConfig;
import gov.lanl.archive.Index;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class MyServletContextListener implements ServletContextListener {
	ServletContext context;
	private static  MyServletContextListener _instance;
	 
	public void contextDestroyed(ServletContextEvent contextEvent) {
	
		 Index idx=(Index) context.getAttribute("idx");
		 if (idx!=null) {
			   idx.close();
			   }
		// ArchiveConfig.shutdown();
		 System.out.println("Context Destroyed");
		
         context = null;
	}
	 
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		
		this.context = sce.getServletContext();
	
		
		 _instance = this;
		System.out.println("listener init:");
		// TODO Auto-generated method stub
		//context.setAttribute("TEST", "TEST_VALUE");
	}
	
	public static MyServletContextListener getInstance() {
	     return _instance;
	 }
	
	public void setAttribute(String key,Object value) {
		context.setAttribute(key,value);
	}
}
