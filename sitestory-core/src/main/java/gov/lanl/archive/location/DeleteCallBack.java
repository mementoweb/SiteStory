package gov.lanl.archive.location;

import java.io.InputStream;

import gov.lanl.archive.CallBack;

public class DeleteCallBack implements CallBack {
	PairWriter wr = new PairWriter();
	PairReader r = new PairReader();
	public void methodToCallBack(String uuid) {
		//fixed delete problem dirty way.
				if (uuid.indexOf("|")>0) {
					String olduuid = uuid.substring(0,uuid.indexOf("|"));
					System.out.println("olduuid" +olduuid);
					String newuuid = uuid.substring(uuid.indexOf("|") +1);
					System.out.println("newuuid" +newuuid);
					InputStream is = r.read(olduuid,"body");
					if (is != null) {
						System.out.println("writing:"+ newuuid +newuuid +".body");
					 wr.write(newuuid, is, "body");
					}
				} else {
					System.out.println("uuid" +uuid);
		         wr.delete(uuid, "body");
		        //wr.delete(uuid, "res");
		         //wr.delete(uuid, "req");
		         //wr.delete(uuid, "ip");
				}
		System.out.println("I've been called back");
		}
	
}
