package gov.lanl.archive.location;

import gov.lanl.archive.CallBack;

public class DeleteCallBack implements CallBack {
	PairWriter wr = new PairWriter();
	
	public void methodToCallBack(String uuid) {
		wr.delete(uuid, "body");
		wr.delete(uuid, "res");
		wr.delete(uuid, "req");
		wr.delete(uuid, "ip");
		System.out.println("I've been called back");
		}
}
