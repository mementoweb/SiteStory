package gov.lanl.archive.unload;

import gov.lanl.archive.Memento;

public interface UnloadCallBack {

	void methodToCallBack(Memento m,boolean stop);
}
