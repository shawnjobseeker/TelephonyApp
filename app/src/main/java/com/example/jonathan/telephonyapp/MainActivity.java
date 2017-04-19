package com.example.jonathan.telephonyapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements BaseActivityInterface {

    private TelephonyManager tManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        tManager.listen(new MyPhoneStateListener(this), PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_SERVICE_STATE );
    }

    @Override
    public void updateCellLocation(CellLocation location) {
        TextView lacCid = (TextView) findViewById(R.id.lac_cid);
        if (location instanceof GsmCellLocation) {
            GsmCellLocation gsm = (GsmCellLocation)location;
            lacCid.setText(getString(R.string.local_area_code, gsm.getLac()) + "\n" + getString(R.string.cell_id, gsm.getCid()));
        }
        else {
            final CdmaCellLocation cdma = (CdmaCellLocation)location;
            lacCid.setText(getString(R.string.base_location, cdma.getBaseStationId()));
            lacCid.setAutoLinkMask(Linkify.WEB_URLS);
            lacCid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // navigate to base location
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + cdma.getBaseStationLatitude() + "," + cdma.getBaseStationLongitude());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            });
        }

    }

    @Override
    public void updateServiceState(ServiceState state) {
        int[] constOrder = new int[]{R.string.in_service, R.string.no_service, R.string.emergency_only, R.string.turned_off};
        TextView serviceState = (TextView)findViewById(R.id.service_state);
        TextView roaming = (TextView)findViewById(R.id.roaming_indicator);
        TextView operator = (TextView)findViewById(R.id.operator_name);
        TextView selection = (TextView)findViewById(R.id.network_selection);
        selection.setText(getString(R.string.network_selection, state.getIsManualSelection() ? "manually" : "automatically"));
        serviceState.setText(getString(constOrder[state.getState()]));
        switch (state.getState()) {
            case ServiceState.STATE_IN_SERVICE:
                serviceState.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));
                if (state.getRoaming())
                    roaming.setText(getString(R.string.roaming));
                operator.setText(state.getOperatorAlphaLong() + "(" + state.getOperatorNumeric() + ")");
            case ServiceState.STATE_EMERGENCY_ONLY:
                case ServiceState.STATE_OUT_OF_SERVICE:
                    case ServiceState.STATE_POWER_OFF:
                        serviceState.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                        break;
            default: break;
        }
    }

    @Override
    public void updateSignalStrength(SignalStrength strength) {
        int[] signalDesc = {R.string.very_poor, R.string.poor, R.string.good, R.string.strong, R.string.very_strong};
        TextView signal = (TextView)findViewById(R.id.signal);
        TextView signalType = (TextView)findViewById(R.id.signal_type);
        String strengthVal = (strength.isGsm()) ? "GSM: " + strength.getGsmSignalStrength() : "CDMA\nDBM: " + strength.getCdmaDbm() + "\nEc/Io: " + strength.getCdmaEcio();
        signalType.setText(strengthVal);
        int level = getSignalStrengthLevel(strength);
        String drawableStrength = "ic_signal_cellular_" + level + "_bar_black_24dp"; // strength.getLevel() exists requires API >= 23!
        int drawableId = getResources().getIdentifier(drawableStrength, "drawable", getPackageName());
        signal.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, drawableId), null, null, null);
        signal.setText(getString(signalDesc[level]));
        if (level < 2)
            signal.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
    }

    public int getSignalStrengthLevel(SignalStrength strength) {
        if (strength.isGsm()) {
            int gsm = strength.getGsmSignalStrength();
            return getLevel(gsm, new int[]{-100, -90, -80, -65});

        } else {
            int dbm = strength.getCdmaDbm();
            int ecio = strength.getCdmaEcio();
            int levelDbm = getLevel(dbm, new int[]{-100, -95, -85, -75});
            int levelEcio = getLevel(ecio, new int[]{-150, -130, -110, -90});
            return (levelDbm < levelEcio) ? levelDbm : levelEcio;
        }

    }
    public int getLevel(int arg, int[] intervals) {
        for (int i = 0; i < intervals.length; i++) {
            if (arg <= intervals[i]) {
                return i;
            }
        }
        return intervals.length;
    }
}
