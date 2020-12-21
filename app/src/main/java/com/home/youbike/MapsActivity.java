package com.home.youbike;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends AppCompatActivity implements  GoogleMap.OnMarkerClickListener,OnMapReadyCallback {

    private static final int REQUEST_CODE_LOCATION = 100;
    private GoogleMap mMap;
    private String TAG = MapsActivity.class.getSimpleName();
    private List<UBike> uBikes = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();
    private Timer timer = new Timer();
    private double lat;
    private double lng;
    private List<Marker> markers =  new ArrayList<>();;
    private TextView timerText;
    private CountDownTimer count;
    private Toolbar toolbar;
    private View infoView;
    private TextView titleText;
    private Button mapButton;
    private Button listButton;
    private String title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        this.setTheme(R.style.Theme_Youbike);
        lat = getIntent().getDoubleExtra("lat",0);
        lng = getIntent().getDoubleExtra("lng",0);
        title = getIntent().getStringExtra("title");
        Log.d(TAG, "onCreate: " + lat + "/" + lng);

        findViews();
        setSupportActionBar(toolbar);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this::onMapReady);
        setupDownCounterTimer();
        final TimerTask responseTask = new TimerTask(){
            @Override
            public void run() {
                getJSON();


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        count.start();
                    }
                });
            }
        };
        timer.schedule(responseTask, 0, 60*1000);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this,BikesActivity.class);
                startActivity(intent);
            }
        });


    }

    private void findViews() {
        timerText = findViewById(R.id.text_count);
        toolbar = findViewById(R.id.toolbar_map);
        infoView = findViewById(R.id.layout_info);
        titleText = findViewById(R.id.text_title_map);
        mapButton = findViewById(R.id.button_googleMap);
        listButton = findViewById(R.id.button_list);
    }

    public void setupDownCounterTimer(){
        count = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText("  "+String.valueOf(millisUntilFinished / 1000) + "秒後自動更新");
            }

            @Override
            public void onFinish() {
                timerText.setText("資料更新中......");
            }
        };

    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
        count.cancel();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Timer timer = new Timer();
        TimerTask responseTask = new TimerTask(){
            @Override
            public void run() {
                getJSON();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        count.start();
                    }
                });


            }
        };

        timer.schedule(responseTask, 0, 60*1000);
    }


    private void getJSON() {
        Request request = new Request.Builder()
                .url("https://tcgbusfs.blob.core.windows.net/blobyoubike/YouBikeTP.json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {

                String json = null;
                try {
                    json = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "onResponse: " + json);
                JSONObject object = parseJSON(json);
                parseJSONObject(object);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupMap();

                    }
                });


            }
        });
    }



    private void parseJSONObject(JSONObject object) {
        uBikes.clear();
        try {
            JSONObject object1 = object.getJSONObject("retVal");
            for (int i=0;i<=404;i++){
                int n = 0;
                n = 4-(String.valueOf(i)).length(); //取得位數
                String final_string = String.valueOf(i);
                for(int j=0;j<n;j++){                    //根據位數前面加多少0
                    final_string = "0" + final_string;
                }
                if(object1.has(final_string) && !object1.isNull(final_string)){ //判斷是否有這物件
                    JSONObject object2 = object1.getJSONObject(final_string);
                    UBike uBike = new UBike(object2);
                    uBikes.add(uBike);
                    Log.d(TAG, "onResponse: " + object2.getString("sna"));
                }
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }
        //test data
        for (UBike uBike : uBikes) {
          //  Log.d(TAG, "parseJSONObject: " + uBike.getMday());
        }
        Log.d(TAG, "parseJSONObject: " + uBikes.size());


    }


    private JSONObject parseJSON(String json) {
        JSONObject jsonObject  = null;

        try {
             jsonObject = new JSONObject(json);
        }catch (JSONException err){
            Log.d("Error", err.toString());
        }
       return  jsonObject;

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_LOCATION);
            return;
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(25.033976,121.5623502), 16));
        setupLocation();
        setupMap();
    }

    private void setupMap() {
        mMap.clear();
        markers.clear();
        for (UBike uBike : uBikes) {
            LatLng latLng = new LatLng(Double.valueOf(uBike.getLat()), Double.valueOf(uBike.getLng()));
            Marker marker =  mMap.addMarker(new MarkerOptions().position(latLng).title(uBike.getSna())
                    .snippet( "可借車輛:"+ uBike.getSbi() + "\n" + "可停空位:" + uBike.getBemp()));
            markers.add(marker);
        }
        for (Marker marker : markers) {
            if(marker.getPosition().latitude == lat && marker.getPosition().longitude  == lng){
                marker.showInfoWindow();

            }
        }
        mMap.setInfoWindowAdapter(new InfoWindowAdapter(MapsActivity.this));
        mMap.setOnMarkerClickListener(this);

    }

    @SuppressLint("MissingPermission")
    private void setupLocation() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if(lat!=0 & lng!=0){
            LatLng latLng = new LatLng(lat,lng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            infoView.setVisibility(View.VISIBLE);
            titleText.setText("  "+title);

        }else{
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
            client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if(task.isSuccessful()){
                        Location location = task.getResult();
                        Log.d(TAG, "onComplete: " + location.getLatitude() + "," + location.getLongitude());
                        LatLng latLng = new LatLng(location.getLatitude(),  location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

                    }
                }
            });
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case  R.id.item_list:{
                Intent intent = new Intent(this,BikesActivity.class);
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_LOCATION &&
        grantResults[0] == PackageManager.PERMISSION_GRANTED){
            setupLocation();

        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
       infoView.setVisibility(View.VISIBLE);
       String title = marker.getTitle();
       titleText.setText("  "+title);

       return false;
    }

}