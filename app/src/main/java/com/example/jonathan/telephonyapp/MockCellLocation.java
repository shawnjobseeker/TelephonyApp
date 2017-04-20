package com.example.jonathan.telephonyapp;

/**
 * Created by Jonathan on 20/04/2017.
 */
// class for holding cell location values
public class MockCellLocation {
    private boolean isGsm;
    private int lac, cid, baseLocation;
    private String baseLocationLatLng;

    public MockCellLocation(int lac, int cid) {
        this.isGsm = true;
        this.lac = lac;
        this.cid = cid;
    }
    public MockCellLocation(int baseLocation, double lat, double lng) {
        this.isGsm = false;
        this.baseLocation = baseLocation;
        this.baseLocationLatLng = lat + "," + lng;
    }

    public boolean isGsm() {
        return isGsm;
    }

    public int getLac() {
        if (isGsm)
        return lac;
        else
            return 0;
    }

    public int getCid() {
        if (isGsm)
            return cid;
        else
            return 0;
    }

    public int getBaseLocation() {
        if (!isGsm)
        return baseLocation;
        else
            return 0;
    }

    public String getBaseLocationLatLng() {
        if (!isGsm)
        return baseLocationLatLng;
        else
            return null;
    }
}
