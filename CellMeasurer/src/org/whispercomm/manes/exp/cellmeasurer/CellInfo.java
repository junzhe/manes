package org.whispercomm.manes.exp.cellmeasurer;

import org.json.JSONObject;

/**
 * The class used to store 3G data.
 *
 * @author Junzhe Zhang
 *
 */
public abstract class CellInfo {

    public String mcc;
    public String mcc_server;
    public boolean isPrev;

    public CellInfo() {
        isPrev = true;
    }

    public void setIsPrev(boolean isPrev) {
        this.isPrev = isPrev;
    }

    public boolean getIsPrev() {
        return isPrev;
    }

    synchronized public void updateVersion() {
        mcc_server = mcc;
    }

    synchronized public void updateIsPrev() {
        if ((mcc_server == null) || (!mcc_server.equals(mcc))) {
            isPrev = false;
        } else {
            isPrev = true;
        }
    }

    abstract public JSONObject prepareJSON();
}
