package com.devandroid.tonic;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;


public class Jouer extends Activity implements SensorEventListener {

    SensorManager sensorManager;
    Sensor magnetic, accelerometer;

    float[] acceleromterVector = new float[3];
    float[] magneticVector = new float[3];
    float[] resultMatrix = new float[9];
    float[] values = new float[3];

    float azimuth, pitch, roll;

    MediaPlayer mediaPlayer;

    String note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jouer);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener((SensorEventListener) this, magnetic, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mediaPlayer = MediaPlayer.create(this, R.raw.do3);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // Mettre à jour la valeur de l'accéléromètre et du champ magnétique
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acceleromterVector = sensorEvent.values;
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticVector = sensorEvent.values;
        }

        // Demander au sensorManager la matrice de Rotation (resultMatrix)
        SensorManager.getRotationMatrix(resultMatrix, null, acceleromterVector, magneticVector);

        // Demander au SensorManager le vecteur d'orientation associé (values)
        SensorManager.getOrientation(resultMatrix, values);
        // l'azimuth
        azimuth = (float) Math.toDegrees(values[0]);
        // le pitch
        pitch = (float) Math.toDegrees(values[1]);
        // le roll
        roll = (float) Math.toDegrees(values[2]);

        Log.v("azimuth", Float.toString(azimuth));

        if (azimuth >= -170 && azimuth <= -139){
            if(note != "do"){
                mediaPlayer.release();
                mediaPlayer = MediaPlayer.create(this, R.raw.do3);
                mediaPlayer.start();
            }
            note="do";
        }

        if (azimuth >= -118 && azimuth <= -87){
            if(note != "re"){
                mediaPlayer.release();
                mediaPlayer = MediaPlayer.create(this, R.raw.re3);
                mediaPlayer.start();
            }

            note="re";
        }

        if (azimuth >= -66 && azimuth <= -35){
            if(note != "mi"){
                mediaPlayer.release();
                mediaPlayer = MediaPlayer.create(this, R.raw.mi3);
                mediaPlayer.start();
            }

            note="mi";
        }

        if (azimuth >= -14 && azimuth <= 17){
            if(note != "fa"){
                mediaPlayer.release();
                mediaPlayer = MediaPlayer.create(this, R.raw.fa3);
                mediaPlayer.start();
            }
            note="fa";
        }

        if (azimuth >= 38 && azimuth <= 69){
            if(note != "sol"){
                mediaPlayer.release();
                mediaPlayer = MediaPlayer.create(this, R.raw.sol3);
                mediaPlayer.start();
            }
            note="sol";
        }

        if (azimuth >= 110 && azimuth <= 121){
            if(note != "la"){
                mediaPlayer.release();
                mediaPlayer = MediaPlayer.create(this, R.raw.la3);
                mediaPlayer.start();
            }
            note="la";
        }

        if (azimuth >= 141 && azimuth <= 170){
            if(note != "si"){
                mediaPlayer.release();
                mediaPlayer = MediaPlayer.create(this, R.raw.si3);
                mediaPlayer.start();
            }
            note="si";
        }

    }


    // Stopper les mesures
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, magnetic);
        sensorManager.unregisterListener(this, accelerometer);
    }


    // Reprendre les mesures
    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener((SensorEventListener) this, magnetic, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
