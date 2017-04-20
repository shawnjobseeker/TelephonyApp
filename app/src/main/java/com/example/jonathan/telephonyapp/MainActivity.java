package com.example.jonathan.telephonyapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import com.example.jonathan.telephonyapp.TelephonyService.TelephonyBinder;

public class MainActivity extends AppCompatActivity implements BaseActivityInterface {

    private TelephonyService service;
    private boolean isBound;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            TelephonyBinder binder = (TelephonyBinder) iBinder;
            service = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };
    private BroadcastReceiver serviceBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // received broadcasts run in background!
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCellLocation();
                    updateServiceState();
                    updateSignalStrength();
                }
            });

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceBroadCastReceiver, new IntentFilter("phone-state"));
        Intent bindIntent = new Intent(this, TelephonyService.class);
        bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceBroadCastReceiver);
        super.onStop();
    }


    public void updateCellLocation() {
        TextView lacCid = (TextView) findViewById(R.id.lac_cid);
        final MockCellLocation location = service.getCellLocation();
        if (location.isGsm()) {
            lacCid.setText(getString(R.string.local_area_code, location.getLac()) + "\n" + getString(R.string.cell_id, location.getCid()));
        }
        else {
            lacCid.setText(getString(R.string.base_location, location.getBaseLocation()));
            lacCid.setAutoLinkMask(Linkify.WEB_URLS);
            lacCid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // navigate to base location
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + location.getBaseLocationLatLng());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            });
        }

    }


    public void updateServiceState() {
        int[] constOrder = new int[]{R.string.in_service, R.string.no_service, R.string.emergency_only, R.string.turned_off};
        TextView serviceState = (TextView)findViewById(R.id.service_state);
        TextView roaming = (TextView)findViewById(R.id.roaming_indicator);
        TextView operator = (TextView)findViewById(R.id.operator_name);
        TextView selection = (TextView)findViewById(R.id.network_selection);
        selection.setText(getString(R.string.network_selection, service.isManualSelection() ? "manually" : "automatically"));
        serviceState.setText(getString(constOrder[service.getServiceState()]));
        switch (service.getServiceState()) {
            case ServiceState.STATE_IN_SERVICE:
                serviceState.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));
                if (service.isRoaming())
                    roaming.setText(getString(R.string.roaming));
                operator.setText(service.getOperatorName());
                break;
            case ServiceState.STATE_EMERGENCY_ONLY:
                case ServiceState.STATE_OUT_OF_SERVICE:
                    case ServiceState.STATE_POWER_OFF:
                        serviceState.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                        break;
            default: break;
        }
    }


    public void updateSignalStrength() {
        MockSignalStrength strength = service.getStrength();
        int[] signalDesc = {R.string.very_poor, R.string.poor, R.string.good, R.string.strong, R.string.very_strong};
        TextView signal = (TextView)findViewById(R.id.signal);
        TextView signalType = (TextView)findViewById(R.id.signal_type);
        signalType.setText(strength.toString());
        if (strength.hasNoSignal()) {
            signal.setVisibility(View.INVISIBLE);
            return;
        }
        int level = strength.getSignalLevel();
        String drawableStrength = "ic_signal_cellular_" + level + "_bar_black_24dp";
        int drawableId = getResources().getIdentifier(drawableStrength, "drawable", getPackageName());
        signal.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, drawableId), null, null, null);
        signal.setText(getString(signalDesc[level]));
        if (level < 2)
            signal.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        else
            signal.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));
    }

}
