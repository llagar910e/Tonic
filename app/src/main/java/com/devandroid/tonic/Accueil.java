package com.devandroid.tonic;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Accueil extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);
    }

    public void tapButtonJouer(View view){
        Intent intent1 = new Intent(this, Jouer.class);
        startActivity(intent1);
    }

    public void tapButtonBeacon(View view){
        Intent intent2 = new Intent(this, CreationBeacon.class);
        startActivity(intent2);
    }
}
