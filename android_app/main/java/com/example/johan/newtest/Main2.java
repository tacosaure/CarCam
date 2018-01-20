package com.example.johan.newtest;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;


public class Main2 extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {
    String ipadress;
    Intent intent;
    Socket client;
    int port;
    String msg;

    //variable entrée/sortie de données
    OutputStream outtoserver;
    OutputStreamWriter osw;
    BufferedWriter bw;
    String you="";      //inutile
    BufferedReader br;
    InputStream is;
    InputStreamReader isr;

    //toolbar
    Toolbar mToolbar;

    // Boutons controle de la voiture et prise de photo
    Button button_avancer;
    Button button_reculer;
    Button button_left;
    Button button_right;
    Button snap;

    // gestion de la camera
    Button button_x_plus;
    Button button_x_moins;
    Button button_y_plus;
    Button button_y_moins;
    Button button_home;

    // barre de gestion de la vitesse
    SeekBar speed;
    TextView speed_text;
    int progress_value_speed;
    int progress_value_opacity=255;

    // Flux video : motion
    WebView mWebView; // flux video recupere à partir d'une adresse http
    WebView conf_motion; // configuration de motion via http / non visible

    // adresse http : gestion motion
    String strg_snap;
    String piAddr = "http://"+ipadress+":8081/";
    String strg_pause_motion_detect;
    String strg_restart_motion;
            //"https://archive.org/download/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4";

    // pour le bouton poussoir en chaine
    Handler mHandler;

    // historique des messages
    ListView mainListView ;
    ArrayAdapter<String> listAdapter ;


    // Lors de la création de l'activité
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);    // mettre le design
        intent = getIntent();

        // récupération des données envoyées depuis l'activité précédente
        ipadress = intent.getStringExtra("IP_AD");

        // Vérification du bon format du port
        if(intent.getStringExtra("PO_AD").isEmpty())
            port = 8080;
        else
            port = Integer.parseInt(intent.getStringExtra("PO_AD"));
        if ((port > 65535)||(port<0))
            port = 8080;

        // Lier les variables aux objets du XML via l'ID
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        //Boutons rajoutés
        button_avancer = (Button) findViewById(R.id.button_avancer);
        button_reculer = (Button) findViewById(R.id.button_reculer);
        button_left = (Button) findViewById(R.id.button_left);
        button_right = (Button) findViewById(R.id.button_right);
        snap = (Button) findViewById(R.id.snap);
        button_x_plus = (Button) findViewById(R.id.button_x_plus);
        button_x_moins = (Button) findViewById(R.id.button_x_moins);
        button_y_plus = (Button) findViewById(R.id.button_y_plus);
        button_y_moins = (Button) findViewById(R.id.button_y_moins);
        button_home = (Button) findViewById(R.id.button_home);

        //Historique des messages
        mainListView = (ListView) findViewById( R.id.history );
        String[] planets = new String[] {};
        ArrayList<String> planetList = new ArrayList<String>();
        planetList.addAll( Arrays.asList(planets) );

        // Create ArrayAdapter using the planet list.
        listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, planetList);


        speed = (SeekBar) findViewById(R.id.seekBar_speed);
        speed_text = (TextView) findViewById(R.id.speed_text);
        speed_text.setText("Speed : "+speed.getProgress()+"%");
        // valeur par défault de la vitesse
        speed.setProgress(50);

        // lien http configuration motion
        strg_snap = "http://popo:pipi@"+ipadress+":8080/0/action/snapshot";
        strg_pause_motion_detect = "http://popo:pipi@"+ipadress+":8080/0/detection/pause";
        strg_restart_motion = "http://popo:pipi@"+ipadress+":8080/0/action/restart";
        piAddr = "http://"+ipadress+":8081/";

        conf_motion = (WebView) findViewById(R.id.conf_motion);

        mWebView = (WebView) findViewById(R.id.activity_main_webview);
        // autoriser le zoom sur la page web en cachant les boutons
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.loadUrl(piAddr);

        setSupportActionBar(mToolbar);

        button_avancer.setOnTouchListener(this);
        button_reculer.setOnTouchListener(this);
        button_right.setOnTouchListener(this);
        button_left.setOnTouchListener(this);
        button_x_moins.setOnTouchListener(this);
        button_x_plus.setOnTouchListener(this);
        button_y_plus.setOnTouchListener(this);
        button_y_moins.setOnTouchListener(this);

        button_home.setOnClickListener(this);
       /* button_x_moins.setOnClickListener(this);
        button_x_plus.setOnClickListener(this);
        button_y_plus.setOnClickListener(this);
        button_y_moins.setOnClickListener(this);*/
        snap.setOnClickListener(this);

        //gestion de la vitesse
        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            //lorsqu'on est en train de changer la vitesse
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress_value_speed=progress;
                speed_text.setText(progress+"%");
                sendMessage("speed "+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            //Lorsqu'on arrête de toucher à la barre de vitesse
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                speed_text.setText("Speed : "+progress_value_speed+"%");
            }
        });

        // Thread pour le socket
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client=new Socket();
                    SocketAddress server = new InetSocketAddress(ipadress,port);
                    client.connect(server);
                    //Effectuer une action sur le thread principal
                    Main2.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //Do your UI operations like dialog opening or Toast here
                            // affiche un message
                            if(client.isConnected()) Toast.makeText(Main2.this, "Connected", Toast.LENGTH_SHORT).show();


                        }
                    });
                    //Envoi d'un message vers les serveur
                    outtoserver=client.getOutputStream();
                    osw=new OutputStreamWriter(outtoserver);
                    bw=new BufferedWriter(osw);
                    bw.write("Hello from android");
                    bw.flush();

                    sendMessage("speed 50");

                    //Reception de messages avec la boucle d'écoute
                    is = client.getInputStream();
                    isr=new InputStreamReader(is);
                    br = new BufferedReader(isr);
                    try{
                        for(;;){

                            final String message =  br.readLine();
                            if(message.length()!=0){
                               /* if(message.substring(0,6).equals("plaque")) {
                                    Main2.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            //Do your UI operations like dialog opening or Toast here
                                            Toast toast = Toast.makeText(Main2.this, message, Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    });
                                }*/
                                if (message.equals("plate recognition has failed")) {
                                    Main2.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            //Do your UI operations like dialog opening or Toast here
                                            Toast toast = Toast.makeText(Main2.this, message, Toast.LENGTH_SHORT);
                                            toast.getView().setBackgroundColor(Color.RED);
                                            toast.getView().setPadding(10, 10, 10, 10);
                                            toast.show();
                                        }
                                    });
                                }

                                // substring pour récupérer une partie d'un string
                                else if(message.substring(0,4).equals("True")) {
                                    Main2.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            //Do your UI operations like dialog opening or Toast here
                                            Toast toast = Toast.makeText(Main2.this, message, Toast.LENGTH_SHORT);

                                            // changement de la couleur du background du message affiché
                                            toast.getView().setBackgroundResource(android.R.color.holo_green_light);

                                            // marge intérieure de la cellule du message affiché
                                            toast.getView().setPadding(10, 10, 10, 10);

                                            // afficher le message
                                            toast.show();
                                        }
                                    });
                                }
                                else if (message.substring(0,5).equals("False")) {
                                    Main2.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            //Do your UI operations like dialog opening or Toast here

                                            Toast toast = Toast.makeText(Main2.this, message, Toast.LENGTH_SHORT);
                                            toast.getView().setBackgroundResource(android.R.color.holo_red_light);
                                            toast.getView().setPadding(10, 10, 10, 10);
                                            toast.show();
                                        }
                                    });
                                }

                                else {
                                    Main2.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            //Do your UI operations like dialog opening or Toast here
                                            Toast.makeText(Main2.this, message, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                Main2.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                    listAdapter.add(message);
                                    mainListView.setAdapter(listAdapter);
                                    mainListView.setSelection(listAdapter.getCount() - 1);
                                    }
                                });

                            }
                        }
                    }
                    // gestion des exceptions/erreurs
                    catch(IOException e)
                    {
                        e.printStackTrace();

                    }
                    catch(Exception e)
                    {

                        e.printStackTrace();
                        finish();
                    }


                }
                catch (UnknownHostException e2) {
                    e2.printStackTrace();
                    finish();

                } catch (IOException e1) {
                    finish();
                    e1.printStackTrace();
                    Log.d("Time out", "Time");
                }
            }
            // Lancement du thread
        }).start();
    }

    // MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.my_menu2, menu);
        return true;
    }

    // Item du menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){

            case R.id.action_refresh :
                mWebView.reload();
                Toast.makeText(Main2.this,
                        "Video refreshed",
                        Toast.LENGTH_SHORT)
                        .show();
                return true;

            /*case R.id.action_calibrage :
                Toast.makeText(Main2.this,
                        "Calibrage en cours",
                        Toast.LENGTH_SHORT)
                        .show();
                sendMessage("calibrage");
                return true;*/

            case R.id.action_stop_detection :
                Toast.makeText(Main2.this,
                        "Detection mouvement : pause",
                        Toast.LENGTH_SHORT)
                        .show();
                conf_motion.loadUrl(strg_pause_motion_detect);
                return true;

            case R.id.action_restart_motion :
                Toast.makeText(Main2.this,
                        "Restart motion",
                        Toast.LENGTH_SHORT)
                        .show();
                conf_motion.loadUrl(strg_restart_motion);
                return true;

            case R.id.action_opacity :
                ShowDialog();
                return true;

            case R.id.action_check_immatriculation:
                ShowDialogImmatriculation();
                return true;

            case R.id.action_check_id:
                ShowDialogId();
                return true;

            case R.id.action_check_nom:
                ShowDialogName();
                return true;

            case R.id.action_check_full_name:
                ShowDialogFullname();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // boite de dialogue : recherche manuelle dans la database par numero de plaque d'immatriculation
    public void ShowDialogImmatriculation()
    {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final EditText text_imma = new EditText(this);
        popDialog.setIcon(android.R.drawable.ic_menu_preferences);
        popDialog.setTitle("Please Enter The Car Registration");
        popDialog.setView(text_imma);
        popDialog.setPositiveButton("Check",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sendMessage("check immatriculation "+text_imma.getText().toString());
                        dialog.dismiss();
                    }
                });
        popDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        popDialog.create();
        popDialog.show();
    }

    // boite de dialogue : recherche manuelle dans la database par ID
    public void ShowDialogId()
    {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final EditText text = new EditText(this);
        popDialog.setIcon(android.R.drawable.ic_menu_preferences);
        popDialog.setTitle("Please Enter an ID");
        popDialog.setView(text);
        popDialog.setPositiveButton("Check",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sendMessage("check id "+text.getText().toString());
                        dialog.dismiss();
                    }
                });
        popDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        popDialog.create();
        popDialog.show();
    }

    // boite de dialogue : recherche manuelle dans la database par nom
    public void ShowDialogName()
    {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final EditText text = new EditText(this);
        popDialog.setIcon(android.R.drawable.ic_menu_preferences);
        popDialog.setTitle("Please Enter a Lastname");
        popDialog.setView(text);
        popDialog.setPositiveButton("Check",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sendMessage("check nom "+text.getText().toString());
                        dialog.dismiss();
                    }
                });
        popDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        popDialog.create();
        popDialog.show();
    }

    // boite de dialogue : recherche manuelle dans la database par nom et prénom
    public void ShowDialogFullname()
    {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final EditText text_nom = new EditText(this);
        final EditText text_prenom = new EditText(this);
        text_nom.setHint("Nom");
        text_prenom.setHint("Prenom");

        // design intern à la boite de dialogue
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(text_prenom);
        layout.addView(text_nom);



        popDialog.setIcon(android.R.drawable.ic_menu_preferences);
        popDialog.setTitle("Please Enter a Fullname");
        popDialog.setView(text_prenom);
        popDialog.setView(text_nom);
        popDialog.setView(layout);
        popDialog.setPositiveButton("Check",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sendMessage("check nom "+text_nom.getText().toString() + " prenom " + text_prenom.getText().toString());
                        dialog.dismiss();
                    }
                });
        popDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        popDialog.create();
        popDialog.show();
    }


    // boite de dialogue : gestion de l'opacité des boutons
    public void ShowDialog(){
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        SeekBar seek = new SeekBar(this);
        seek.setMax(255);
        seek.setProgress(progress_value_opacity);
        seek.setKeyProgressIncrement(1);

        popDialog.setIcon(android.R.drawable.ic_menu_preferences);
        popDialog.setTitle("Please Select Your Opacity ");
        popDialog.setView(seek);


        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){

                progress_value_opacity=progress;
                button_avancer.getBackground().setAlpha(progress);
                button_reculer.getBackground().setAlpha(progress);
                button_left.getBackground().setAlpha(progress);
                button_right.getBackground().setAlpha(progress);
                button_home.getBackground().setAlpha(progress);
                button_x_plus.getBackground().setAlpha(progress);
                button_y_plus.getBackground().setAlpha(progress);
                button_y_moins.getBackground().setAlpha(progress);
                button_x_moins.getBackground().setAlpha(progress);
                snap.getBackground().setAlpha(progress);
           }



            public void onStartTrackingTouch(SeekBar arg0) {
                //do something

            }



            public void onStopTrackingTouch(SeekBar seekBar) {
            //do something


            }
        });


        // Button OK
        popDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        popDialog.create();
        popDialog.show();
    }

    // boite de dialogue : analyse de la plaque d'immatriculation via la photo
    public void ShowDialogSnapshot()
    {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        popDialog.setIcon(android.R.drawable.ic_menu_preferences);
        popDialog.setTitle("Please Enter a Lastname");
        popDialog.setPositiveButton("Check",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sendMessage("check snapshot");
                        dialog.dismiss();
                    }
                });
        popDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        popDialog.create();
        popDialog.show();
    }

    // bouton simple : prendre une photo
    @Override
        public void onClick (View view){
            if(view==button_home)
            {
                msg = "xy_home";
            }
            /*else if(view == button_x_plus)
            {
                msg = "x+";
            }
            else if(view == button_x_moins)
            {
                msg = "x-";
            }
            else if(view == button_y_plus)
            {
                msg = "y+";
            }
            else if(view == button_y_moins)
            {
                msg = "y-";
            }*/
            else if(view == snap)
            {
                msg = "";
                conf_motion.loadUrl(strg_snap);
                Toast.makeText(Main2.this, "snapshot", Toast.LENGTH_SHORT).show();
                ShowDialogSnapshot();
            }
            /*else
                msg = message.getText().toString();*/
            sendMessage(msg);
        }




    @Override
    public boolean onLongClick(View view){
       /* if(view==button_on)
        {
            msg = "long onLED";
        }
        else
            msg = "";
        sendMessage(msg);*/
        return true;
    }




    //Bouton poussoir et bouton envoi en continu
    // renvoie true si ça consomme le bouton ( non couplé à une autre actionListener ) renvoie false si cela ne consomme pas le bouton ( couplé à une autre actionListener )
    @Override public boolean onTouch(View view, MotionEvent event) {

        // Envoie de strings en continue
        /*if (view == button_test) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mHandler != null) return true;
                    mHandler = new Handler();
                    mHandler.postDelayed(mAction, 250);
                    break;
                case MotionEvent.ACTION_UP:
                    if (mHandler == null) return true;
                    mHandler.removeCallbacks(mAction);
                    mHandler = null;
                    break;
            }
            return false;
        }*/

        // Bouton poussoir : gestion de la voiture
        if (view == button_avancer)
        {
            switch (event.getAction()) {
                //bouton poussoir relache
                case MotionEvent.ACTION_UP:
                    msg = "stop";
                    sendMessage(msg);
                    break;
                //bouton poussoir appuye
                case MotionEvent.ACTION_DOWN:
                    msg = "avancer";
                    sendMessage(msg);
                    break;
            }
            return true;
        }
        else if (view == button_reculer)
        {
            switch (event.getAction()) {
                //bouton poussoir relache
                case MotionEvent.ACTION_UP:
                    msg = "stop";
                    sendMessage(msg);
                    break;
                //bouton poussoir appuye
                case MotionEvent.ACTION_DOWN:
                    msg = "reculer";
                    sendMessage(msg);
                    break;
            }
            return true;
        }
        else if (view == button_right)
        {
            switch (event.getAction()) {
                //bouton poussoir relache
                case MotionEvent.ACTION_UP:
                    msg = "stop_turning";
                    sendMessage(msg);
                    break;
                //bouton poussoir appuye
                case MotionEvent.ACTION_DOWN:
                    msg = "right";
                    sendMessage(msg);
                    break;
            }
            return true;
        }
        else if (view == button_left)
        {
            switch (event.getAction()) {
                //bouton poussoir relache
                case MotionEvent.ACTION_UP:
                    msg = "stop_turning";
                    sendMessage(msg);
                    break;
                //bouton poussoir appuye
                case MotionEvent.ACTION_DOWN:
                    msg = "left";
                    sendMessage(msg);
                    break;
            }
            return true;
        }

        //Bouton envoi en continu : gestion de la caméra
        else if (view == button_x_plus)
        {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mHandler != null) return true;
                    mHandler = new Handler();
                    mHandler.postDelayed(mAction_x_plus, 25);
                    break;
                case MotionEvent.ACTION_UP:
                    if (mHandler == null) return true;
                    mHandler.removeCallbacks(mAction_x_plus);
                    mHandler = null;
                    break;
            }
            return true;
        }
        else if (view == button_x_moins)
        {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mHandler != null) return true;
                    mHandler = new Handler();
                    mHandler.postDelayed(mAction_x_moins, 25);
                    break;
                case MotionEvent.ACTION_UP:
                    if (mHandler == null) return true;
                    mHandler.removeCallbacks(mAction_x_moins);
                    mHandler = null;
                    break;
            }
            return true;
        }
        else if (view == button_y_plus)
        {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mHandler != null) return true;
                    mHandler = new Handler();
                    mHandler.postDelayed(mAction_y_plus, 25);
                    break;
                case MotionEvent.ACTION_UP:
                    if (mHandler == null) return true;
                    mHandler.removeCallbacks(mAction_y_plus);
                    mHandler = null;
                    break;
            }
            return true;
        }
        else if (view == button_y_moins)
        {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mHandler != null) return true;
                    mHandler = new Handler();
                    mHandler.postDelayed(mAction_y_moins, 25);
                    break;
                case MotionEvent.ACTION_UP:
                    if (mHandler == null) return true;
                    mHandler.removeCallbacks(mAction_y_moins);
                    mHandler = null;
                    break;
            }
            return true;
        }

        return false;
    }

    // Envoie des strings en continue
    Runnable mAction_x_plus = new Runnable() {
        @Override public void run() {
            msg = "x+";
            sendMessage(msg);
            mHandler.postDelayed(this, 250);
        }
    };
    Runnable mAction_x_moins = new Runnable() {
        @Override public void run() {
            msg = "x-";
            sendMessage(msg);
            mHandler.postDelayed(this, 250);
        }
    };
    Runnable mAction_y_plus = new Runnable() {
        @Override public void run() {
            msg = "y+";
            sendMessage(msg);
            mHandler.postDelayed(this, 250);
        }
    };
    Runnable mAction_y_moins = new Runnable() {
        @Override public void run() {
            msg = "y-";
            sendMessage(msg);
            mHandler.postDelayed(this, 250);
        }
    };


    // Quand on appuie sur le bouton back du telephone (et non l appli)
    public void onBackPressed () {
        //moveTaskToBack (true);
        msg = "quit";
        sendMessageFinish(msg);
    }

    // envoi de messages
    public void sendMessage(String msg)
    {
        try {
            outtoserver = client.getOutputStream();
            //write in output stream
            osw = new OutputStreamWriter(outtoserver);
            //now write in output buffer
            bw = new BufferedWriter(osw);
            bw.write(you + msg);
            bw.flush();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    //envoi de message puis ferme l'activité en cours
    public void sendMessageFinish(String msg)
    {
        try {
            outtoserver = client.getOutputStream();
            //write in output stream
            osw = new OutputStreamWriter(outtoserver);
            //now write in output buffer
            bw = new BufferedWriter(osw);
            bw.write(you + msg);
            bw.flush();
            finish();
        } catch (IOException e) {

            e.printStackTrace();
            // ferme l'activité en cours
            finish();
        }
    }
}