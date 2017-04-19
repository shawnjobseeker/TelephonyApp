package com.example.jonathan.telephonyapp;

import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;

/**
 * Created by Jonathan on 19/04/2017.
 */

public class MyPhoneStateListener extends PhoneStateListener {

    private BaseActivityInterface activity;
    public MyPhoneStateListener(BaseActivityInterface activity) {
        this.activity = activity;
    }
    @Override
    public void onServiceStateChanged(ServiceState serviceState) {
       activity.updateServiceState(serviceState);
    }

    @Override
    public void onCellLocationChanged(CellLocation location) {
        activity.updateCellLocation(location);
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        activity.updateSignalStrength(signalStrength);
    }
}
