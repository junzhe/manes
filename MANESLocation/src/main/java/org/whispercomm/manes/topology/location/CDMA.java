package org.whispercomm.manes.topology.location;

public class CDMA {
	String mcc;
	int sid;
	int nid;
	int bid;
	boolean asPrev;

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	public int getSid() {
		return sid;
	}

	public void setSid(int sid) {
		this.sid = sid;
	}

	public int getNid() {
		return nid;
	}

	public void setNid(int nid) {
		this.nid = nid;
	}

	public int getBid() {
		return bid;
	}

	public void setBid(int bid) {
		this.bid = bid;
	}

	public void setAsPrev(boolean asPrev) {
		this.asPrev = asPrev;
	}

	public boolean getAsPrev() {
		return this.asPrev;
	}

	@Override
	public String toString() {
		return PojoToString.toString(this);
	}

	public boolean isDataTheSame(CDMA theOther) {
		if (theOther == null)
			return false;
		if (theOther.getBid() == this.bid
				&& theOther.getMcc().compareTo(this.mcc) == 0
				&& theOther.getNid() == this.nid
				&& theOther.getSid() == this.sid)
			return true;
		else
			return false;
	}
}
