package com.devandroid.tonic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;

import Proxemic.ProxZone;


public class Jouer extends Activity implements SensorEventListener, BeaconConsumer {

    //Les valeurs du capteur d'orientation
    private float mAzimuth, mPitch, mRoll = 0f;

    //Indique si une note est en train d'être rejouée, permet d'éviter de rejouer tout au long de l'accélération
    private boolean mRejouer;

    //Permet de savoir si l'utilisateur/la boussole a fait un tour pour changer d'octave
    private int compteurRetourBoussole = 0;

    //La gravité naturelle utilisée pour calculer l'accélération
    final float naturalGravity = 0.98f;

    //Le lecteur permettant de jouer le son
    MediaPlayer mediaPlayer;

    //Le ton joué
    String note = "do";

    //l'octave de la gamme jouée
    int octave = 3;

    //Indique si la note en cours est une dieze ou non
    private boolean dieze = false;

    //L'ID de la ressource de la note jouée constituée de note[+dieze]+octave
    int noteID;

    //La valeur convertie de l'accélération verticale de l'appareil
    float accelerationVerticale;

    //Les valeurs brutes de l'accélération sur les 3 axes
    float[] gravity = new float[3];

    //Utilisé pour écouter les capteurs
    private SensorManager mSensorManager;

    //La boussole
    private ImageView boussole;

    // Enregistre l'angle de rotation de la boussole
    private float currentDegree = 0f;

    //Thread permettant d'animer la boussole en fonction de l'orientation
    Runnable rotationBoussole;

    //Permet d'écouter les beacons alentours
    private BeaconManager beaconManager;

    //Permet de définir les zones  proxémiques
    ProxZone proxzone;
    String zoneProxemique;

    //Le volume du lecteur média
    float volume = 0f;

    //Indique si l'erreur indiquant à l'utilisateur qu'il faut créer un beacon est affichée
    boolean afficheErreur;

    //Incrémenté lors de l'absence de beacon et remis à 0 si un beacon est détecté
    //Permet de détecter l'absence de beacon malgré les sauts de connexion
    int pasBeacon = 0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jouer);

        pasBeacon=0;

        //Instancie le beaconManager pour écouter les beacons alentours
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.bind(this);

        //Permet de définir les zones proxémiques
        proxzone = new ProxZone(0.5D, 1.0D, 1.5D, 2.0D);
        zoneProxemique = new String();

        //Instancie le sensorManager pour utiliser les capteurs
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Instancie une premièrenote de musique
        noteID = getApplicationContext().getResources().getIdentifier(note + octave, "raw", getApplicationContext().getPackageName());

        //Création du mediaPlayer permettant de jouer la musique
        mediaPlayer = MediaPlayer.create(getApplicationContext(), noteID);

        //Image représentant la boussole avec les notes
        boussole = (ImageView) findViewById(R.id.boussole);

        //Thread permettant d'animer la boussole en fonction de l'orientation
        rotationBoussole = new Runnable() {
            @Override
            public void run() {
                // create a rotation animation (reverse turn degree degrees)
                RotateAnimation ra = new RotateAnimation(
                        currentDegree,
                        -mAzimuth,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f);

                // how long the animation will take place
                ra.setDuration(210);

                // set the animation after the end of the reservation status
                ra.setFillAfter(true);

                // Start the animation
                boussole.startAnimation(ra);
                currentDegree = -mAzimuth;


            }
        };
    }

    //A la création du beacon listener
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllRangeNotifiers();
        Log.d("m1","entro");
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Log.v("Beacons", Integer.toString(beacons.size()));
                //Si un beacon est identifié
                if (beacons.size() > 0) {
                    pasBeacon=0;
                    Log.i("Monitoring", "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.");
                    ArrayList<String> arrBeacons = new ArrayList<String>();
                    //Calculer  les distances
                    for (Beacon b:beacons) {
                        String uuid = b.getId1 ().toString ();
                        //Distance
                        double distance1 = b.getDistance();
                        String distance = String.valueOf(Math.round(distance1 * 100.0) / 100.0);
                        arrBeacons.add (uuid+"*"+distance);
                    }

                    //Met à jour la zone proxémique en fonction de la distance du premier beacon trouvé puis change le volume du lecteur
                    zoneProxemique = proxzone.setDistanceofEntity(beacons.iterator().next().getDistance());
                    changerVolume(zoneProxemique);
                    Log.v("Zone", zoneProxemique + " : " + beacons.iterator().next().getDistance());
                }

                //Si aucun beacon trouvé, affiche une erreur et retourne à l'activité précédente
                else pasBeacon ++;

                if (pasBeacon == 3){
                    afficheErreur();
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }

    }

    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_FASTEST);

    }

    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            mAzimuth = Math.round(sensorEvent.values[0]);
            mPitch = Math.round(sensorEvent.values[1]);
            mRoll = Math.round(sensorEvent.values[2]);
        }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//gravity is calculated here
            gravity[1] = naturalGravity * gravity[1] + (1 - naturalGravity) * sensorEvent.values[1];
//acceleration retrieved from the event and the gravity is removed
            accelerationVerticale = Math.round((sensorEvent.values[1] - gravity[1]) * 100);
        }


        if (mPitch > -170 && mPitch < 170) {
            runOnUiThread(rotationBoussole);
            if (accelerationVerticale > 300) {
                rejouerNote();
            } else if (changerNote()) {
                jouerNote();
            }

        }

        Log.v("Azimuth", Float.toString(mAzimuth));
        Log.v("Pitch", Float.toString(mPitch));
        Log.v("Roll", Float.toString(mRoll));
        Log.v("Compteur", Integer.toString(compteurRetourBoussole));
    }

        //Affiche un message disant qu'il faut créer un beacon avant de jouer
        void afficheErreur(){
        afficheErreur = true;
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Attention");
        alertDialog.setMessage("Cette application a besoin d'un beacon pour fonctionner. Pour ce faire, lancez l'application sur un autre appareil et choisissez \" Créer un beacon\" puis réessayez.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(getApplicationContext(), Accueil.class);
                        startActivity(intent);
                    }
                });
        alertDialog.show();
    }

        //Change le volume du lecteur en fonction de la zone proxémique
        void changerVolume(String zoneProxemique){
            if (zoneProxemique=="intimiZone"){
                volume = 0.25f;
            }
            else if (zoneProxemique=="personalZone"){
                volume = 0.5f;
            }
            else if (zoneProxemique=="socialZone"){
                volume = 0.75f;
            }
            else if (zoneProxemique=="publicZone"){
                volume = 1.0f;
            }
            else volume=0f;
        }

        //Joue la note
        void jouerNote() {
            noteID = getApplicationContext().getResources().getIdentifier(note + octave, "raw", getApplicationContext().getPackageName());
            Log.v("mediaplayer", "Releasing");
                mediaPlayer.release();
            Log.v("mediaplayer", "Creating");
            mediaPlayer = MediaPlayer.create(getApplicationContext(), noteID);
            mediaPlayer.setVolume(volume,volume);
            Log.v("mediaplayer", "Starting");
            mediaPlayer.start();
        }

        //Rejoue la note
        void rejouerNote() {
            Log.v("rejouerNote", "ici");
            if (mRejouer == false) {
                mediaPlayer.release();
                mediaPlayer = MediaPlayer.create(getApplicationContext(), noteID);
                mediaPlayer.setVolume(volume,volume);
                mediaPlayer.start();
            }
            mRejouer=true;
        }

        //En fonction des capteurs change ou non la note et renvoie si elle a changé
        boolean changerNote() {

            Log.v("acceleration", "0 : " + accelerationVerticale);
            if (accelerationVerticale < 100  && mPitch > -30 && mPitch < 30) {
                Log.v("note", "Azimuth : " + mAzimuth + " Note : " + note + octave);
                mRejouer=false;
                if (compteurRetourBoussole < 30) compteurRetourBoussole ++;
                if(mRoll > 75 || mRoll < -75){
                    if(dieze==false && note != "mi" && note != "si"){
                        note = note+"d";
                        Log.v("dieze", note);
                        dieze = true;
                        return true;
                    }
                }
                else {

                    if (dieze==true){
                        note = note.substring(0, note.length()-1);
                        Log.v("dieze", note);
                        dieze=false;
                    }
                    if (mAzimuth >= 0 && mAzimuth <= 52) {
                        if (compteurRetourBoussole <= 20 && octave < 4 && note != "do") {
                            octave++;
                        }
                        if (note != "do") {
                            note = "do";
                            return true;
                        }
                        compteurRetourBoussole = 0;
                    }

                    if (mAzimuth >= 53 && mAzimuth <= 105) {
                        if (note != "re") {
                            note = "re";
                            return true;
                        }
                    }

                    if (mAzimuth >= 106 && mAzimuth <= 158) {
                        if (note != "mi") {
                            note = "mi";
                            return true;
                        }
                    }

                    if (mAzimuth >= 159 && mAzimuth <= 211) {
                        if (note != "fa") {
                            note = "fa";
                            return true;
                        }
                    }

                    if (mAzimuth >= 212 && mAzimuth <= 264) {
                        if (note != "sol") {
                            note = "sol";
                            return true;
                        }
                    }

                    if (mAzimuth >= 265 && mAzimuth <= 307) {
                        if (note != "la") {
                            note = "la";
                            return true;
                        }
                    }

                    if (mAzimuth >= 308 && mAzimuth <= 360) {
                        if (compteurRetourBoussole <= 20 && octave > 1 && note != "si") {
                            octave--;
                        }
                        if (note != "si") {
                            note = "si";
                            return true;
                        }
                        compteurRetourBoussole = 0;
                    }
                }
            }
            return false;
        }
}
