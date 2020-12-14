package com.home.youbike;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private static final int REQUEST_CODE_LOCATION = 100;
    private GoogleMap mMap;
    private String TAG = MapsActivity.class.getSimpleName();
    private List<UBike> uBikes = new ArrayList<UBike>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        OkHttpClient client = new OkHttpClient();
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
                JSONObject object = parseJSON(json.toString());
                parseJSONObject(object);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(MapsActivity.this::onMapReady);
                    }
                });


            }
        });



    }

    private void parseJSONObject(JSONObject object) {
        try {
            JSONObject object1 = object.getJSONObject("retVal");
            for(int i = 1 ; i<=9; i++){
                if(object1.has("000" + String.valueOf(i)) && !object1.isNull("000" +String.valueOf(i))) {
                    JSONObject object2 = object1.getJSONObject("000" + String.valueOf(i));
                    UBike uBike = new UBike();
                    uBike.sno = object2.getString("sno");
                    uBike.sna = object2.getString("sna");
                    uBike.tot = object2.getString("tot");
                    uBike.sbi = object2.getString("sbi");
                    uBike.sarea = object2.getString("sarea");
                    uBike.mday = object2.getString("mday");
                    uBike.lat = object2.getString("lat");
                    uBike.lng = object2.getString("lng");
                    uBike.ar = object2.getString("ar");
                    uBike.sareaen = object2.getString("sareaen");
                    uBike.snaen = object2.getString("snaen");
                    uBike.aren = object2.getString("aren");
                    uBike.bemp = object2.getString("bemp");
                    uBike.act = object2.getString("act");
                    uBikes.add(uBike);
                    Log.d(TAG, "onResponse: " + object2.getString("sna"));
                }
            }
            for(int i = 10 ; i<=99 ; i++){
                if(object1.has("00" + String.valueOf(i)) && !object1.isNull("00" +String.valueOf(i))) {
                    JSONObject object2 = object1.getJSONObject("00" + String.valueOf(i));
                    UBike uBike = new UBike();
                    uBike.sno = object2.getString("sno");
                    uBike.sna = object2.getString("sna");
                    uBike.tot = object2.getString("tot");
                    uBike.sbi = object2.getString("sbi");
                    uBike.sarea = object2.getString("sarea");
                    uBike.mday = object2.getString("mday");
                    uBike.lat = object2.getString("lat");
                    uBike.lng = object2.getString("lng");
                    uBike.ar = object2.getString("ar");
                    uBike.sareaen = object2.getString("sareaen");
                    uBike.snaen = object2.getString("snaen");
                    uBike.aren = object2.getString("aren");
                    uBike.bemp = object2.getString("bemp");
                    uBike.act = object2.getString("act");
                    uBikes.add(uBike);
                    Log.d(TAG, "onResponse: " + object2.getString("sna"));
                }
            }
            for(int i = 100 ;i<=404;i++){
                if(object1.has("0" + String.valueOf(i)) && !object1.isNull("0" +String.valueOf(i))) {
                    JSONObject object2 = object1.getJSONObject("0" + String.valueOf(i));
                    UBike uBike = new UBike();
                    uBike.sno = object2.getString("sno");
                    uBike.sna = object2.getString("sna");
                    uBike.tot = object2.getString("tot");
                    uBike.sbi = object2.getString("sbi");
                    uBike.sarea = object2.getString("sarea");
                    uBike.mday = object2.getString("mday");
                    uBike.lat = object2.getString("lat");
                    uBike.lng = object2.getString("lng");
                    uBike.ar = object2.getString("ar");
                    uBike.sareaen = object2.getString("sareaen");
                    uBike.snaen = object2.getString("snaen");
                    uBike.aren = object2.getString("aren");
                    uBike.bemp = object2.getString("bemp");
                    uBike.act = object2.getString("act");
                    uBikes.add(uBike);
                    Log.d(TAG, "onResponse: " + object2.getString("sna"));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (UBike uBike : uBikes) {
            Log.d(TAG, "parseJSONObject: " + uBike.mday);
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

        setupMap();
    }

    private void setupMap() {

        for (UBike uBike : uBikes) {
            LatLng latLng = new LatLng(Double.valueOf(uBike.lat), Double.valueOf(uBike.lng));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
            mMap.addMarker(new MarkerOptions().position(latLng).title(uBike.ar));
        }




        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_LOCATION);
            return;
        }
        setupLocation();

    }

    @SuppressLint("MissingPermission")
    private void setupLocation() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    Location location = task.getResult();
                    Log.d(TAG, "onComplete: " + location.getLatitude() + "," + location.getLongitude());

                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_LOCATION &&
        grantResults[0] == PackageManager.PERMISSION_GRANTED){
            setupLocation();

        }
    }
}