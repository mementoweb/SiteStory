package gov.lanl.archive;

import gov.lanl.archive.CallBack;
import gov.lanl.archive.unload.UnloadCallBack;

import java.util.Date;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Vector;
/*
@author Lyudmila Balakireva
*/

public interface Index {
		
		  boolean add( Memento m);
		/*  boolean remove( Memento m ); */
		/*  boolean contains(String url,String digest ); */
		  Memento get (String url,  Date accessdatetime);
		   Date getRecent(String url);
	   /*   Memento get (String location); */
		   public  List getMementos(String url);
	       public void close();
	       public  List getUntil(String date,String startkey); 
	       public  void processUnload (String date,UnloadCallBack ucallback); 
	       public void delete(String url,String date,CallBack callback);
	       //public NavigableMap getFeed(String domain);
}
