package com.example.jonathan.telephonyapp;

import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;

/**
 * Created by Jonathan on 19/04/2017.
 */

public interface BaseActivityInterface {
    void updateCellLocation(CellLocation location);
    void updateSignalStrength(SignalStrength strength);
    void updateServiceState(ServiceState state);
}
