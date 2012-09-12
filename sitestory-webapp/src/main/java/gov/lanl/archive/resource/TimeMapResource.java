package gov.lanl.archive.resource;

import gov.lanl.archive.ArchiveConfig;
import gov.lanl.archive.Index;
import gov.lanl.archive.Memento;


import java.net.URI;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

//import org.dspace.foresite.Aggregation;
//import org.dspace.foresite.ResourceMap;
//import org.dspace.foresite.OREFactory;
//import org.dspace.foresite.Agent;
//import org.dspace.foresite.AggregatedResource;
//import org.dspace.foresite.ORESerialiser;
//import org.dspace.foresite.ORESerialiserFactory;
//import org.dspace.foresite.ResourceMapDocument;
//import org.dspace.foresite.Triple;
//import org.dspace.foresite.jena.TripleJena;
//import org.dspace.foresite.Predicate;

/*
@author Lyudmila Balakireva
*/

@Path("/timemap/*")

public class TimeMapResource {
	protected final URI baseUri;
	private static Index idx;
	MementoCommons mc;
	public	TimeMapResource( @Context UriInfo uriInfo )
    {
        this.baseUri = uriInfo.getBaseUri();
         mc = new MementoCommons(baseUri);
         idx = ArchiveConfig.getMetadataIndex();
         MyServletContextListener cl= MyServletContextListener.getInstance();
		    cl.setAttribute("idx", idx);
         System.out.println("init");
    }
  
	
	
	@GET
	@Path("link/{id:.*}")
		@Produces("application/link-format" )
	public  Response getMyLinks(@PathParam("id") String idp, @Context UriInfo ui) throws ParseException {
		 URI ur = ui.getRequestUri(); 
		 System.out.println("request url:"+ur.toString());
		 URI baseurl = ui.getBaseUri();
		// System.out.println("baseurl"+baseurl.toString());
		 String id = ur.toString().replace(baseurl.toString()+"timemap/link/", "");
		// System.out.println("get into get:"+id);
		 
		System.out.println("id"+id);
		 List mset = idx.getMementos(id);
		 System.out.println("Size:"+mset.size());
		 Iterator it =  mset.iterator();
		 if (mset.size()==0) {
			 return Response.status(404).build(); 
		 }
		    StringBuffer sb = new StringBuffer("<"+id+">;rel=\"original\"\n");
		    sb.append(" , <"+baseUri.toString() +"timegate/" + id+">;rel=\"timegate\" ");
		   // sb.append (" , <"+baseUri.toString() +"timebundle/" + id+">;rel=\"timebundle\"");
		    sb.append (" , <"+baseUri.toString() +"timemap/link/" + id+">;rel=\"self\"; type=\"application/link-format\"");
		                int count = 0;
		                while (it.hasNext()) {
		                	Memento m = (Memento) it.next();
		                	//m.getAccessdate();
		                	String mlink="";
		                	if (count==0) {
		                	 mlink = mc.composeLink(	m.getAccessdate(), id, "first memento");
		                	 count=1;
		                	}
		                	else {
		                		mlink = mc.composeLink(	m.getAccessdate(), id, "memento");	
		                		count=count+1;
		                	}
		                	sb.append( mlink +"\n");
		                }
		                
		                if (count>0) {
		                int m_index = sb.lastIndexOf("memento"); 
		                System.out.println(m_index);
		                sb.insert(m_index,"last ");
		                }
		                
		                
		                ResponseBuilder r = Response.ok(sb.toString());
		
		             	String timemap = " <"+baseUri.toString() +"timemap/link/" + id+">;anchor=\""+id+"\" ;rel=\"timemap\"; type=\"application/link-format\"";
                        r.header("Link", timemap );
                        
                      return  r.build();       
		 		//return sb.toString();
	}
	
/*	
	@GET
	@Path("rdf/{id:.*}")
	@Produces("application/rdf+xml" )
	
	public  Response getRDF(@PathParam("id") String idp, @Context UriInfo ui) throws Exception {
		 URI ur = ui.getRequestUri(); 
		 System.out.println("request url:"+ur.toString());
		 URI baseurl = ui.getBaseUri();
		 System.out.println("baseurl"+baseurl.toString());
		 String id = ur.toString().replaceFirst(baseurl.toString()+"timemap/rdf/", "");
		 List<Memento> mset = idx.getMementos(id);
		 Date now = new Date();
		 Calendar cal = Calendar.getInstance();
		
			Aggregation agg = OREFactory.createAggregation(new URI( baseUri + "timebundle/" + id));
			            agg.addTitle("Memento Time Bundle for " + id);
			            agg.addType(new URI("http://www.mementoweb.org/terms/tb/TimeBundle"));
			
			 Predicate pr_type = new Predicate();
			          pr_type.setURI(new URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
		 	 Predicate pr_format = new Predicate();
			           pr_format.setURI(new URI("http://purl.org/dc/elements/1.1/format"));
		
			 ResourceMap rem = agg.createResourceMap(new URI(baseUri + "timemap/rdf"  + "/" + id));
			             rem.setCreated(now);
			             rem.setModified(now);
			             rem.createTriple(pr_type, new URI("http://www.mementoweb.org/terms/tb/TimeMap"));
		
			             Agent creator = OREFactory.createAgent();
			                   creator.addName("Foresite Toolkit (Java)");
			             rem.addCreator(creator);
	
			
			AggregatedResource ar_o = agg.createAggregatedResource(new URI(id));
			                   ar_o.createTriple(pr_type, new URI("http://www.mementoweb.org/terms/tb/OriginalResource"));
			                   
			//include timegate into aggregation
			AggregatedResource ar_tg = agg.createAggregatedResource(new URI(baseUri+ "timegate/" + id));
			                   ar_tg.createTriple(pr_format, new URI(id));
			                   ar_tg.createTriple(pr_type, new URI("http://www.mementoweb.org/terms/tb/TimeGate"));
			                   
		
			   Predicate prstart= new Predicate();
			             prstart.setURI(new URI("http://www.mementoweb.org/terms/tb/start"));
			   Predicate prend= new Predicate();
			             prend.setURI(new URI("http://www.mementoweb.org/terms/tb/end"));
			
			 for(Memento r:mset){
				
				 
				    AggregatedResource   ar = agg.createAggregatedResource(new URI(mc.composeMemUrl(r.getAccessdate(),id,"memento")));
				       if (r.getMimetype()!=null){                
				        ar.createTriple(pr_format, r.getMimetype());
				       }
				                         Predicate pr = new Predicate();
				                         pr. setURI(new URI("http://www.mementoweb.org/terms/tb/mementoFor"));
				 	                     ar.createTriple(pr, new URI(id));
					                     ar.createTriple(pr_type, new URI("http://www.mementoweb.org/terms/tb/Memento"));
					
					 Triple triple =  new TripleJena ();
					        triple.initialise(new URI(mc.composeMemUrl(r.getAccessdate(),id,"memento")));
		          
		             Predicate pred = new Predicate();
		                       pred.setURI(new URI("http://www.mementoweb.org/terms/tb/validOver"));
		       
		             String blanc ="urn:uuid:" +  UUID.randomUUID ().toString();
		             triple.relate(pred, new URI(blanc ));
		             
		             Triple tr =  new TripleJena ();
		                    tr.initialise(new URI(blanc));
		                    tr.relate(pr_type,new URI("http://www.mementoweb.org/terms/tb/Period"));
		    
		    			//period difined by [ [ interval [ date first digest recorded  and date of next digest recorded [ 
		             
		             Triple trd =  new TripleJena ();
		                    trd.initialise(new URI(blanc));
		                    cal.setTime(r.getAccessdate());
	                        trd.relate(prstart,cal);
	                     //   cal.setTime(r.getNextMemento().getAccessdate());
	       	               // trd.relate(prend,cal);
	       	    
	       	       
	                 ar.addTriple(triple);
	                 ar.addTriple(tr);
	                 ar.addTriple(trd);
		             
			 }
			
			 ORESerialiser serial = ORESerialiserFactory.getInstance("RDF/XML");
			 ResourceMapDocument doc = serial.serialise(rem);
			 String serialisation = doc.toString();
			 ResponseBuilder r = Response.ok(serialisation);
				
          	String timemap = " <"+baseUri.toString() +"timemap/rdf/" + id+">;anchor=\""+id+"\" ;rel=\"timemap\"; type=\"application/rdf+xml\"";
             r.header("Link", timemap );
             
           return  r.build();    
		//return serialisation;
	}
	*/
}
