package com.example.jonathan.telephonyapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

/**
 * Created by Jonathan on 19/04/2017.
 */

public class TelephonyService extends Service {

    private final IBinder binder = new TelephonyBinder();
    private int serviceState;
    private String operatorName;
    private boolean isManualSelection;
    private boolean isRoaming;
    private MockCellLocation location;
    private MockSignalStrength strength;

    class TelephonyBinder extends Binder {
        TelephonyService getService() {
            return TelephonyService.this;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        TelephonyManager   tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        tManager.listen(new ServicePhoneStateListener(),  PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_SERVICE_STATE );
        return binder;
    }

    private void sendUpdate() {
        Intent intent = new Intent("phone-state");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // here, the public methods for Activity to retrieve values from Service
    public int getServiceState() {
        return serviceState;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public boolean isManualSelection() {
        return isManualSelection;
    }

    public boolean isRoaming() {
        return isRoaming;
    }
    public MockCellLocation getCellLocation() {
        return location;
    }
    public MockSignalStrength getStrength() {
        return strength;
    }

    private  class ServicePhoneStateListener extends PhoneStateListener {

        private boolean serviceStateChanged, cellLocationChanged, signalStrengthChanged;
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            if (!serviceStateChanged) {
                TelephonyService.this.serviceState = serviceState.getState();
                TelephonyService.this.operatorName = serviceState.getOperatorAlphaLong() + " (" + serviceState.getOperatorNumeric() + ")";
                TelephonyService.this.isManualSelection = serviceState.getIsManualSelection();
                TelephonyService.this.isRoaming = serviceState.getRoaming();
            }
            serviceStateChanged = true;
            sendUpdate();
        }

        @Override
        public void onCellLocationChanged(CellLocation location) {
            if (!cellLocationChanged) {
                if (location instanceof GsmCellLocation) {
                    GsmCellLocation gsm = (GsmCellLocation) location;
                    TelephonyService.this.location = new MockCellLocation(gsm.getCid(), gsm.getLac());
                } else if (location instanceof CdmaCellLocation) {
                    CdmaCellLocation cdma = (CdmaCellLocation) location;
                    TelephonyService.this.location = new MockCellLocation(cdma.getBaseStationId(), cdma.getBaseStationLatitude(), cdma.getBaseStationLongitude());
                }
            }
           cellLocationChanged = true;
            sendUpdate();
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            // strength.getLevel() exists but requires API >= 23, therefore retrieved Level manually in MockSignalStrength
            if (!signalStrengthChanged) {
                if (signalStrength.isGsm())
                    TelephonyService.this.strength = new MockSignalStrength(signalStrength.getGsmSignalStrength());
                else
                    TelephonyService.this.strength = new MockSignalStrength(signalStrength.getCdmaDbm(), signalStrength.getCdmaEcio());
            }
            signalStrengthChanged = true;
            sendUpdate();
        }
        private void sendUpdate() {
            if (serviceStateChanged && cellLocationChanged && signalStrengthChanged) {
                serviceStateChanged = false;
                cellLocationChanged = false;
                signalStrengthChanged = false;

            }
            TelephonyService.this.sendUpdate();
        }
    }
}
