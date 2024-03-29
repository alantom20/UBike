package com.home.youbike;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BikesActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION = 50;
    private String TAG = BikesActivity.class.getSimpleName();
    private List<UBike> uBikes = new ArrayList<>();
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private BikeAdapter adapter;
    private Timer timer = new Timer();
    private boolean flag = true;
    private double longitude;
    private double latitude;
    private TimerTask responseTask;
    private TextView timerText;
    private CountDownTimer count;
    private Button mapButton;
    private Button favoriteButton;
    private List<UBike> uBikesNewTaipei = new ArrayList<>();
    private int listIndex;
    private List<UBike> updateList;
    private AdView bikeAd;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bikes);
        //check internet
        checkInternet();


        //check location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_CODE_LOCATION);
        }else{
            findViews();
        }

    }


    private void checkInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED &&
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED) {
            final AlertDialog.Builder builderSingle = new AlertDialog.Builder(BikesActivity.this);
            builderSingle.setIcon(R.drawable.error)
                    .setTitle("無法連結到網路")
                    .setCancelable(false)
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }).show();

        }
    }



    public void setupDownCounterTimer(){
        count = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText("  "+String.valueOf(millisUntilFinished/1000) +  "秒後自動更新");
            }

            @Override
            public void onFinish() {
                timerText.setText("清單更新中......");
            }
        };

    }


    private void findViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycler_bike);
        timerText = findViewById(R.id.timer_text);
        mapButton = findViewById(R.id.button_map);
        favoriteButton = findViewById(R.id.button_favorite);
        bikeAd = findViewById(R.id.bike_adView);

        startLocationUpdates();
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        bikeAd.loadAd(adRequest);
        setSupportActionBar(toolbar);
        setupDownCounterTimer();
        //recycler
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        responseTask = new TimerTask(){
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


        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BikesActivity.this,MapsActivity.class);
                startActivity(intent);
            }
        });
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BikesActivity.this,FavoriteActivity.class);
                startActivity(intent);
            }
        });


    }



    @Override
    protected void onRestart() {
        super.onRestart();

        startLocationUpdates();
        getJSON();
        checkInternet();

    }



    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }
    private void stopLocationUpdates() {
        client.removeLocationUpdates(locationCallback);
    }


    private void getJSON() {
        CountDownLatch latch = new CountDownLatch(1);  //指定等待多少執行緒

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
                Log.d(TAG, "onResponse2: " + updateList.get(0).getMday());

            }
        });
        OkHttpClient client1 = new OkHttpClient();
        client1.newCall(requestTaipei).enqueue(new Callback() {
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
                Log.d(TAG, "onResponse1: " + json);
                //取得第二筆資料
                JSONObject object = parseJSON(json);
                parseJSONObject(object);
                Log.d(TAG, "onResponse1: " + latitude + "/" +longitude);
                try {
                    latch.await();               //取得完前一筆資料再往下執行
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                //加上第二筆資料
                if(flag){
                    uBikes.addAll(updateList);
                }else{
                    for (UBike uBike : updateList) {
                        uBikes.set(listIndex,uBike);
                        listIndex++;
                    }
                }
//                Log.d(TAG, "onResponse: " + uBikes.get(673).getSna() + "/"+uBikes.get(673).getMday());
                Log.d(TAG, "onResponse total:" + uBikes.size());



                //addDistance
                for (UBike uBike : uBikes) {
                    float distance = distanceBetween(Double.parseDouble(uBike.getLat()),Double.parseDouble(uBike.getLng()),latitude,longitude);
                    uBike.setDistance(distance);
                }
                //依距離排序
                Collections.sort(uBikes, new bikeSort());

                //getDatabaseList
                List<Bike> results = BikeDatabase.getInstance(BikesActivity.this).bikeDao().getAll();


                for(UBike uBike: uBikes){
                    for (Bike result : results) {
                        if(uBike.getSno().equals(result.sno)){  //資料庫裡如果有這筆資料就設為true
                            uBike.setStar(true);
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(flag) {
                            adapter = new BikeAdapter(uBikes , BikesActivity.this);
                            recyclerView.setAdapter(adapter);
                            flag = false;
                        }else {
                            adapter.notifyDataSetChanged();

                        }

                    }
                });

            }
        });
    }




    private  void parseJSONObject(JSONObject object) {

        try {
            JSONObject object1 = object.getJSONObject("retVal");
            listIndex = 0;
            for (int i = 0; i <= 404; i++) {
                int n = 0;
                n = 4 - (String.valueOf(i)).length(); //取得位數
                String final_string = String.valueOf(i);
                for (int j = 0; j < n; j++) {                    //根據位數前面加多少0
                    final_string = "0" + final_string;
                }
                if (object1.has(final_string) && !object1.isNull(final_string)) { //判斷是否有這物件
                    JSONObject object2 = object1.getJSONObject(final_string);
                    UBike uBike = new UBike(object2);
                    if(uBike.getAct().equals("1")){
                        if(flag){
                            uBikes.add(uBike);
                        }else {
                            uBikes.set(listIndex,uBike);
                            listIndex++;
                        }
                    }

                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
            parseJSONObject(object);

        }
        //test data
        Log.d(TAG, "parseJSONObject: " + uBikes.size());



    }


    private JSONObject parseJSON(String json) {
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException err) {
            Log.d("Error", err.toString());
        }
        return jsonObject;

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




    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        client = LocationServices.getFusedLocationProviderClient( this);


        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2*5000);

        locationCallback=new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // if (location != null) {


                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.d(TAG, "onComplete1: " + location.getLatitude() + "," + location.getLongitude()); //使用者位置

                    //    }
                }
            }
        };
        client.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());


        client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if(task.isSuccessful()){
                        Location location = task.getResult();
                        if(location!=null){
                            Log.d(TAG, "onComplete: " + location.getLatitude() + "," + location.getLongitude()); //使用者位置
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();
                        }

                    }
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bike,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case  R.id.item_map:{
                Intent intent = new Intent(this,MapsActivity.class);
                startActivity(intent);
            }
           /* case  android.R.id.home :{
                finish();
            }*/
        }
        return super.onOptionsItemSelected(item);
    }



    class bikeSort implements Comparator<UBike> {      //依距離做排序

        @Override
        public int compare(UBike a, UBike b) {
            return Float.compare(a.getDistance(), b.getDistance());
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0){
            if(requestCode == REQUEST_CODE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
            grantResults[1] == PackageManager.PERMISSION_GRANTED ){
                findViews();
            }else{
                finish();
        }

        }
    }




}