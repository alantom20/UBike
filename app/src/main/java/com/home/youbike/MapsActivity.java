package com.home.youbike;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import java.util.concurrent.CountDownLatch;

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
    private Timer timer = new Timer();
    private double lat = 25.033998502835516;
    private double lng = 121.56450245374178;
    private List<Marker> markers =  new ArrayList<>();;
    private TextView timerText;
    private CountDownTimer count;
    private Toolbar toolbar;
    private View infoView;
    private TextView titleText;
    private Button mapButton;
    private Button listButton;
    private String title;
    private LatLng myLatLng;
    private TextView distanceText;
    private float distance;
    private List<UBike> uBikesNewTaipei;
    private List<UBike> updateList;
    private AdView adView;
    private CountDownLatch latchLocation;
    private String bemp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        String sbi = getIntent().getStringExtra("sbi");
        bemp = getIntent().getStringExtra("bemp");
        lat = getIntent().getDoubleExtra("lat",0);
        lng = getIntent().getDoubleExtra("lng",0);
        title = getIntent().getStringExtra("title");
        distance = getIntent().getFloatExtra("distance",0);
        Log.d(TAG, "onCreate: " + lat + "/" + lng);
        findViews();
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this::onMapReady);
        setupDownCounterTimer();


        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this,BikesActivity.class);
                startActivity(intent);
            }
        });



    }


    @Override
    protected void onRestart() {
        super.onRestart();
        checkInternet();
    }

    private void checkInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED &&
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED) {
            final AlertDialog.Builder builderSingle = new AlertDialog.Builder(MapsActivity.this);
            builderSingle.setIcon(R.drawable.error)
                    .setTitle("無法連結到網路")
                    .setCancelable(false)
                    .setPositiveButton("確定", null)
                    .show();

        }
    }
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
        setupLocation(); //設定初始位置
        //setupMap();
        final TimerTask responseTask = new TimerTask(){
            @Override
            public void run() {
                latchLocation = new CountDownLatch(1);
                getMyLocation();
                try {
                    latchLocation.await();               //取得完前一筆資料再往下執行
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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

    private void findViews() {
        timerText = findViewById(R.id.text_count);
        toolbar = findViewById(R.id.toolbar_map);
        infoView = findViewById(R.id.layout_info);
        titleText = findViewById(R.id.text_title_map);
        mapButton = findViewById(R.id.button_googleMap);
        listButton = findViewById(R.id.button_list);
        distanceText = findViewById(R.id.map_distance);
        adView = findViewById(R.id.map_adView);
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




    private void getJSON() {
        CountDownLatch latch = new CountDownLatch(1);
        Request requestTaipei = new Request.Builder()
                .url("https://tcgbusfs.blob.core.windows.net/blobyoubike/YouBikeTP.json")
                .build();
        Request requestNewTaipei = new Request.Builder()
                .url("https://data.ntpc.gov.tw/api/datasets/71CD1490-A2DF-4198-BEF1-318479775E8A/json?page=0&size=10000")
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(requestNewTaipei).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String newTaipeiJson = response.body().string();
                Log.d(TAG, "onResponse2: " + newTaipeiJson);
                uBikesNewTaipei = new Gson().fromJson(newTaipeiJson,
                        new TypeToken<ArrayList<UBike>>(){}.getType());
                for (int i = 0; i < uBikesNewTaipei.size()-1; i++) {
                    String act = uBikesNewTaipei.get(i).getAct();
                    if(act.equals("0")){
                        uBikesNewTaipei.remove(i);
                    }
                }
                updateList = uBikesNewTaipei;

                latch.countDown();
                Log.d(TAG, "onResponse2: " + updateList.size());

            }
        });

        OkHttpClient client1 = new OkHttpClient();
        client1.newCall(requestTaipei).enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {

                try {
                    String taipeiJson = response.body().string();
                    Log.d(TAG, "onResponse: " + taipeiJson);
                    JSONObject object = parseJSON(taipeiJson);
                    parseJSONObject(object);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "onResponse: " + uBikes.size());
                uBikes.addAll(updateList);
                Log.d(TAG, "onResponse: " + uBikes.size());
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
                    if(uBike.getAct().equals("1")){
                        uBikes.add(uBike);
                    }
                    Log.d(TAG, "parseJSONObject: " + object2.getString("sna"));
                }
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }
        //test data

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


    private void setupMap() {
            mMap.clear();
            markers.clear();
        mMap.setInfoWindowAdapter(new InfoWindowAdapter(MapsActivity.this));
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

        mMap.setOnMarkerClickListener(this);

    }

    @SuppressLint("MissingPermission")
    private void setupLocation() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        if(lat!=0 & lng!=0){
            LatLng latLng = new LatLng(lat,lng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            setViews();

        }else{
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
            client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if(task.isSuccessful()){
                        Location location = task.getResult();
                        if(location !=null){
                            Log.d(TAG, "onComplete: " + location.getLatitude() + "," + location.getLongitude());
                            myLatLng = new LatLng(location.getLatitude(),  location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 16));
                        }


                    }
                }
            });
        }


    }

    private void setViews() {
        infoView.setVisibility(View.VISIBLE);
        titleText.setText(title);
        if (distance >= 1000) {
            BigDecimal f = new BigDecimal(distance / 1000);
            String result =f.setScale(2, BigDecimal.ROUND_HALF_UP).toString(); //取小數後兩位
            distanceText.setText(result + "公里");
        }else{
            distanceText.setText(String.valueOf(distance) + "公尺");
        }
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "http://maps.google.com/maps?&daddr=" + String.valueOf(lat)+ ","+ String.valueOf(lng) +  "&dirflg=w"; //googleMap設定值
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void getMyLocation() {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient( this);
        client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {

            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    Location location = task.getResult();
                    if(location!=null){
                        Log.d(TAG, "onComplete: " + location.getLatitude() + "," + location.getLongitude()); //使用者位置
                        myLatLng = new LatLng(location.getLatitude(),  location.getLongitude());

                    }
                    latchLocation.countDown();
                }
            }
        });
    }

    public float distanceBetween(double startLatitude,double startLongitude,double endLatitude, double endLongitude)
    {
        float[] results = new float[1];
        Location.distanceBetween(startLatitude,startLongitude,
                endLatitude, endLongitude, results);

        BigDecimal f = new BigDecimal(results[0]);
        float result = f.setScale(1,BigDecimal.ROUND_HALF_UP).floatValue(); //取小數後一位
        Log.d(TAG, "distanceBetween: " + result);
        return result;
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
            case  android.R.id.home :{
                finish();
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
        double marketLatitude = marker.getPosition().latitude;
        double marketLongitude = marker.getPosition().longitude;
        titleText.setText(title);
        if(myLatLng != null){
            float MarkDistance = distanceBetween(myLatLng.latitude, myLatLng.longitude, //取得距離
                    marketLatitude,  marketLongitude);
            Log.d(TAG, "onMarkerClick: " + MarkDistance);
            if (MarkDistance >= 1000) {
                BigDecimal f = new BigDecimal(MarkDistance / 1000);
                String result =f.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                distanceText.setText(result + "公里");
            }else{
                distanceText.setText(String.valueOf(MarkDistance) + "公尺");
            }
        }
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "http://maps.google.com/maps?&daddr=" + String.valueOf(marketLatitude)+ ","+ String.valueOf(marketLongitude) +  "&dirflg=w";
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }

            }
        });

        return false;
    }


}