package gov.lanl.director;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class InitServletContextListener implements ServletContextListener {
	ServletContext context;
	private static  InitServletContextListener _instance;
	// AppConfig config;
	public void contextDestroyed(ServletContextEvent contextEvent) {
		// ArchiveConfig.shutdown();
		 System.out.println("Context Destroyed");		
         context = null;
	}
	 
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		
		this.context = sce.getServletContext();	
		 _instance = this;
		 
		String path = context.getRealPath(context.getContextPath());
		
		System.out.println("listener init:"+path);
	      String initparam = context.getInitParameter("configpath");
	      System.out.println("initparam:" +initparam);
	   	// config.processConfig(path+"/classes/timegates.xml");
	   
		// TODO Auto-generated method stub
		context.setAttribute("path", path);
	}
	
	public static InitServletContextListener getInstance() {
	     return _instance;
	 }
	
	public void setAttribute(String key,Object value) {
		context.setAttribute(key,value);
	}
	public String getAttribute(String key) {
		String value =(String) context.getAttribute(key);
		return value;
	}
}
