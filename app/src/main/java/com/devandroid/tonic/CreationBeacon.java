package com.devandroid.tonic;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.altbeacon.beacon.BeaconParser;

import java.util.UUID;

//Créé un beacon qui continue d'émettre tant que l'application n'a pas été kill
public class CreationBeacon extends Activity {

    MyBeacon mybeacon = new MyBeacon();

    public void createBeacon(android.content.Context context, String id1, String id2, String id3){
        mybeacon.setBeacon(context, id1, id2, id3);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation_beacon);

        //Créé un beacon ayant une id1 random
        final String id1 = UUID.randomUUID().toString();
        final String id2="2";
        final String id3="3";

        createBeacon(getApplicationContext(), id1, id2, id3);
    }
}
