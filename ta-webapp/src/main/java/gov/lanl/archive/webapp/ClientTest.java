package gov.lanl.archive.webapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;

public class ClientTest {

    /**
     * @param args
     * @throws IOException 
     * @throws HttpException 
     */
    public static void main(String[] args) throws HttpException, IOException {
	// TODO Auto-generated method stub
	//test_socket();
	//test_get();
	test_put();
	
    }
    
    public static void test_put() throws HttpException, IOException {
	 HttpClient mClient = new HttpClient();
	 String bodyonly =   "<html><title>Hello World!</title><body><p><font size=\"14\">Hello World! Page created at  Sun, 30 Jul 2009 </font></p></body></html>"; 
	 int size = bodyonly.length();
	 String header ="Date: Mon, 30 Jul 2009 14:29:09 GMT\r\nServer: Apache\r\nContent-Length:"+size+ "\r\nConnection: close\r\nContent-Type: text/html; charset=UTF-8\r\n\r\n";
         String body=header+"\r\n\r\n"+header+ bodyonly;
	 
	 //String header ="HTTP/1.1 200 OK\r\nDate: Tue, 28 Jun 2009 16:31:39 GMT\r\nServer: Apache\r\nContent-Location: index.html.en\r\nVary: negotiate,accept-language,accept-charset\r\nTCN: choice\r\nLast-Modified: Fri, 10 Oct 2008 04:46:12 GMT\r\nETag: \"f0b0-5b0-458ded424ad00\"\r\nAccept-Ranges: bytes\r\nContent-Length: 1456\r\nContent-Type: text/html\r\nContent-Language: en\r\n\r\n";
		// String body=header +"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n<title>Test Page for Apache Installation5</title>\n</head>\n<!-- Background white, links blue (unvisited), navy (visited), red\n(active) -->\n<body bgcolor=\"#FFFFFF\" text=\"#000000\" link=\"#0000FF\"\nvlink=\"#000080\" alink=\"#FF0000\">\n<p>  If you can see this, it means that the installation of the <a\nhref=\"http://www.apache.org/foundation/preFAQ.html\">Apache  web\nserver</a> software on this system was successful. You may now add\ncontent to this directory and replace this page!</p>\n\n<hr width=\"50%\" size=\"8\" />\n<h2 align=\"center\">Seeing this instead of the website you\nexpected??</h2>\n\n<p>This page is here because the site administrator has changed the\nconfiguration of this web server. da! Please <strong>contact the person\nresponsible for maintaining this server with questions.</strong>\nThe Apache Software Foundation, which wrote the web server software\nthis site administrator is using, has nothing to do with\nmaintaining this site and cannot help resolve configuration\nissues.</p>\n\n<hr width=\"50%\" size=\"8\" />\n<p>The Apache <a href=\"manual/\">documentation</a> has been included\nwith this distribution.</p>\n\n<p>You are free to use the image below on an Apache-powered web\nserver. Thanks for using Apache!!!!!!!!????</p>\n\n<div align=\"center\"><img src=\"apache_pb.gif\" alt=\"\" /></div>\n</body>\n</html>\n\n";
		     //  PutMethod mPut = new PutMethod("http://memento.lanl.gov/tomcat/ta/http://wayback.lanl.gov/hello");
		       PutMethod mPut = new PutMethod("http://localhost:9999/put/http://test.com/hello");
		       // mPut.setRequestHeader("Content-Type","text/html;charset=UTF-8");
		       //mPut.setRequestHeader("Date","Tue, 29 Jun 2009 16:31:39 GMT");
			      
		       //mPut.setRequestBody( test );
		       mPut.setRequestBody(body);
		    
		       mClient.executeMethod( mPut);
		       //mClient.executeMethod( mPut);
		     Header[] headers = mPut.getResponseHeaders();
		     for ( int i = 0; i < headers.length; ++i){
			      System.out.println( headers[i]);
			      }
		  //  System.out.println (headers.toString());
	          String  mReturnMsg = mPut.getResponseBodyAsString();
	          System.out.println(mReturnMsg);
	          mPut.releaseConnection();
    }
    
    public static void test_get() throws HttpException, IOException { 
	
	 HttpClient mClient = new HttpClient();
	// GetMethod  get = new GetMethod("http://localhost:8080/ta/*/http://ttt.lanl.gov/123456");
	 GetMethod  get = new GetMethod("http://memento.lanl.gov/tomcat/ta/*/http://wayback.lanl.gov/hello");
	 get.setRequestHeader("X-Accept-Datetime","Tue, 26 Jul 2009 16:31:39 GMT");
	 mClient.executeMethod(get);
	  Header[] headers = get.getResponseHeaders();
	  for ( int i = 0; i < headers.length; ++i){
	      System.out.println( headers[i]);
	      }
	  
	 String  mReturnMsg = get.getResponseBodyAsString();
          System.out.println(mReturnMsg);
          get.releaseConnection();
    }
    
    public static void test_socket() throws IOException {
	 try {
	        // Construct data
	     
	        // Create a socket to the host
	        String hostname = "memento.lanl.gov";
	        int port = 80;
	        InetAddress addr = InetAddress.getByName(hostname);
	        Socket socket = new Socket(addr, port);
	    
	        // Send header
	        String path = "/servlet/SomeServlet";
	        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
	        wr.write("PUT /tomcat/ta/3 HTTP/1.1\r\n");
	        	wr.write("Host: memento.lanl.gov\r\n");
	        	String data ="Date:Tue, 21 Jul 2009 21:29:39 GMT\r\n\r\n<title>Hello World! </title> > Hello World!   It 's   Tue, 21 Jul 2009 21:29:39 GMT";
	        	wr.write("Content-Length: "+data.length()+"\r\n");
	        	wr.write("\r\n"); 
	        	
	        
	        	
	        	wr.write(data);
	        	//wr.write("PUT /ta/4 HTTP/1.1\r\nHost: localhost\r\nContent-Length:1456\r\n\r\nLast-Modified:Fri, 10 Oct 2008 04:46:12 GMT\r\nETag:\"f0b0-5b0-458ded424ad00\"\r\nAccept-Ranges:bytes\r\nContent-Length:1456\r\nContent-Location:index.html.en\r\nVary:negotiate, accept-language, accept-charset\r\nTCN:choice\r\n\r\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n<title>Test Page for Apache Installation</title>\n</head>\n<!-- Background white, links blue (unvisited), navy (visited), red\n(active) -->\n<body bgcolor=\"#FFFFFF\" text=\"#000000\" link=\"#0000FF"\nvlink="#000080" alink="#FF0000">\n<p>If you can see this, it means that the installation of the <a\nhref="http://www.apache.org/foundation/preFAQ.html">Apache web\nserver</a> software on this system was successful. You may now add\ncontent to this directory and replace this page.</p>\n\n<hr width="50%" size="8" />\n<h2 align="center">Seeing this instead of the website you\nexpected?</h2>\n\n<p>This page is here because the site administrator has changed the\nconfiguration of this web server. Please <strong>contact the person\nresponsible for maintaining this server with questions.</strong>\nThe Apache Software Foundation, which wrote the web server software\nthis site administrator is using, has nothing to do with\nmaintaining this site and cannot help resolve configuration\nissues.</p>\n\n<hr width="50%" size="8" />\n<p>The Apache <a href="manual/">documentation</a> has been included\nwith this distribution.</p>\n\n<p>You are free to use the image below on an Apache-powered web\nserver. Thanks for using Apache!</p>\n\n<div align="center"><img src="apache_pb.gif" alt="" /></div>\n</body>\n</html>\n\n");
	       
	    
	        wr.flush();
	    
	        // Get response
	        BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        String line;
	        while ((line = rd.readLine()) != null) {
	            System.out.println(line);
	            // Process line...
	        }
	        socket.close();
	        wr.close();
	        rd.close();
	    } catch (Exception e) {
	    }
	
	
	
	
	

	    

    }
    
}
