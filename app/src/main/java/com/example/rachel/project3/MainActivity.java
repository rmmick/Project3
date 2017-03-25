package com.example.rachel.project3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SharedPreferences pref;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    ConnectivityCheck myCheck;

    private static final String TAG = "ParseJSON";
    private static final int SPACES_TO_INDENT_FOR_EACH_LEVEL_OF_NESTING = 2;

    private String MYURL = "http://www.tetonsoftware.com/pets/pets.json";
    private ImageView iv;
    private JSONArray jsonArray;
    private ArrayList<Pet> pets = new ArrayList<>();
    private Spinner spinner;

    DownloadTask myTask;
    DownloadImageTask imageTask;

    private int numberentries = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        spinner = (Spinner) findViewById(R.id.spinner);
        iv = (ImageView) findViewById(R.id.imageView);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("");
        myCheck = new ConnectivityCheck(this);

        if(doNetworkCheck()){
            doTask();
        }

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        listener = new SharedPreferences.OnSharedPreferenceChangeListener(){
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("PREF_LIST")){
                    MYURL = pref.getString("PREF_LIST", "Nothing Found");
                    pets.clear();
                    spinner.setAdapter(null);

                    if(doNetworkCheck()){
                        doTask();
                    }
                }

            }
        };

        pref.registerOnSharedPreferenceChangeListener(listener);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                getImage(spinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void getImage(String s) {
        String url = "";
        for(int i = 0; i < numberentries; i++){
            if(s.equals(pets.get(i).getName())){
                url = pets.get(i).getURL();
            }
        }

        imageTask = new DownloadImageTask(this);
        imageTask.execute(url);
    }

    public void setImage(Bitmap b){
        iv.setImageBitmap(b);
    }

    public void doTask(){
        myTask = new DownloadTask(this);

        myTask.setnameValuePair("Name1","Value1");
        myTask.setnameValuePair("Name2","Value2");

        myTask.execute(MYURL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.p3_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;
            default:
                break;
        }
        return true;
    }

    public boolean doNetworkCheck() {
        String res = myCheck.isNetworkReachable()?"Connected":"No Network Connection";
        if(!(res.equals("Connected"))) {
            Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
        }
        return res.equals("Connected");
    }

    public void processJSON(String string) {
        if(string != null) {
            try {
                JSONObject jsonobject = new JSONObject(string);

                Log.d(TAG, jsonobject.toString(SPACES_TO_INDENT_FOR_EACH_LEVEL_OF_NESTING));

                jsonArray = jsonobject.getJSONArray("pets");
                numberentries = jsonArray.length();
                setJSONUI();
                Log.i(TAG, "Number of entries " + numberentries);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            error();
        }

    }

    /**
     */
    private void setJSONUI() {
        if (jsonArray == null) {
            return;
        }
        try {
            for(int j = 0; j < numberentries; j++) {
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                pets.add(new Pet(jsonObject.getString("name"), jsonObject.getString("file")));
            }

            ArrayList<String> s = new ArrayList<>();
            for(int a = 0; a < pets.size(); a++){
                s.add(pets.get(a).getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, s);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setEnabled(true);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void error() {
        spinner.setEnabled(false);
        Toast.makeText(MainActivity.this, "Error connecting to: " + MYURL + "\nServer returned 404", Toast.LENGTH_SHORT).show();
    }
}
