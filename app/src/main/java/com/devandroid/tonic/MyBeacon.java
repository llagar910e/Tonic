package com.devandroid.tonic;
import android.app.Application;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.util.Arrays;

public class MyBeacon {

    BeaconParser beaconParser;
    BeaconTransmitter beaconTransmitter;

    void setBeacon(android.content.Context context, String id1, String id2, String id3){
        Beacon beacon = new Beacon.Builder()
                .setId1(id1)
                .setId2(id2)
                .setId3(id3)
                .setManufacturer(0x0118)
                .setTxPower(-59)
                .setDataFields(Arrays.asList(new Long[] {0l}))
                .build();

        beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        beaconTransmitter = new BeaconTransmitter(context, beaconParser);
        beaconTransmitter.startAdvertising(beacon);
    }

}
