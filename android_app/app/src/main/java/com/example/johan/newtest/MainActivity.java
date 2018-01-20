package com.example.johan.newtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText ip;
    EditText port;
    String ipadress;
    String portadr;

    Intent intent;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;
    Button connect;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ip=(EditText)findViewById(R.id.ipadress);
        port=(EditText)findViewById(R.id.port);
        connect=(Button)findViewById(R.id.connect);
        mToolbar = (Toolbar) findViewById(R.id.toolbar) ;
        setSupportActionBar(mToolbar);
        load();
        connect.setOnClickListener(this);
    }
/*
   // en cas d'ajout d'un menu


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_setting) {
            Toast.makeText(MainActivity.this,
                    "You have clicked on setting action menu",
                    Toast.LENGTH_SHORT)
                    .show();
        }
        if(item.getItemId() == R.id.action_about_us){
            Toast.makeText(MainActivity.this,
                    "You have clicked on about us action menu",
                    Toast.LENGTH_SHORT)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }*/


// Lors du clic sur le bouton
    @Override
    public void onClick(View view) {
        //récupération des valeurs des champs
        ipadress=ip.getText().toString();
        portadr=port.getText().toString();

        //création d'une nouvelle activié
        intent= new Intent(getApplicationContext(),Main2.class);

        //envoi des données vers la nouvelle activié
        intent.putExtra("IP_AD",ipadress);
        intent.putExtra("PO_AD", portadr);

        //ouverture de la nouvelle activité
        startActivity(intent);
    }
    public void save(){
        sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("IP_AD",ipadress);
        editor.putString("PO_AD", portadr);
        editor.commit();

    }

    // initialisation des variables au chargement de l'activité
    public void load(){
        sharedPreferences = getSharedPreferences("data",Context.MODE_PRIVATE);
        ipadress= sharedPreferences.getString("IP_AD","192");
        portadr= sharedPreferences.getString("IP_PO","8080");

        if(ipadress == "192" ){

        }
        else
        {
            ip.setText(ipadress);

        }

        if(portadr == "8080" ){

        }
        else
        {
            port.setText(portadr);

        }

    }

}
