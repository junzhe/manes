package org.whispercomm.manes.topology.location;

public class GSM {

	boolean asPrev;

	public void setAsPrev(boolean asPrev) {
		this.asPrev = asPrev;
	}

	public boolean getAsPrev() {
		return this.asPrev;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	public int getMnc() {
		return mnc;
	}

	public void setMnc(int mnc) {
		this.mnc = mnc;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public int getLac() {
		return lac;
	}

	public void setLac(int lac) {
		this.lac = lac;
	}

	String mcc;
	int mnc;
	int cid;
	int lac;

	@Override
	public String toString() {
		return PojoToString.toString(this);
	}

	public boolean isDataTheSame(GSM theOther) {
		if (theOther == null)
			return false;
		if (theOther.getCid() == this.cid && theOther.getLac() == this.lac
				&& theOther.getMcc().compareTo(this.mcc) == 0
				&& theOther.getMnc() == this.mnc)
			return true;
		else
			return false;
	}
}
