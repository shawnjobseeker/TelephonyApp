package com.example.jonathan.telephonyapp;

/**
 * Created by Jonathan on 20/04/2017.
 */
// class for holding signal strength values
public class MockSignalStrength {
    private String signalType;
    private int signalGsm, signalDbm, signalEcio;
    public MockSignalStrength(int signalGsm) {
        signalType = "GSM";
        this.signalGsm = signalGsm;
    }
    public MockSignalStrength(int signalDbm, int signalEcio) {
        signalType = "CDMA";
        this.signalDbm = signalDbm;
        this.signalEcio = signalEcio;
    }
    public boolean hasNoSignal() {
        return (signalGsm == 0 || signalGsm == 99);
    }

    @Override
    public String toString() {
        if (signalType.equals("GSM"))
            return signalType + ": " + signalGsm;
        else
            return signalType + "\nDBM: " + signalDbm + ", Ec/Io: " + signalEcio;
    }

    public int getSignalLevel() {
        if (signalType.equals("GSM")) {
            if (signalGsm == 99)
                return 0;
            else
                return getLevel(signalGsm, new int[]{0, 8, 16, 24});
        }
        else {
            int levelDbm = getLevel(signalDbm, new int[]{-100, -95, -85, -75});
            int levelEcio = getLevel(signalEcio, new int[]{-150, -130, -110, -90});
            return (levelDbm < levelEcio) ? levelDbm : levelEcio;
        }
    }
    private int getLevel(int arg, int[] intervals) {
        for (int i = 0; i < intervals.length; i++) {
            if (arg <= intervals[i]) {
                return i;
            }
        }
        return intervals.length;
    }
}
