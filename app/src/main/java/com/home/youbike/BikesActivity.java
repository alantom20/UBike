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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BikesActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION = 50;
    private String TAG = BikesActivity.class.getSimpleName();
    private OkHttpClient client = new OkHttpClient();
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bikes);
        findViews();
        setSupportActionBar(toolbar);
        setupDownCounterTimer();
        //check internet
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network

        }
        else{
            final AlertDialog.Builder builderSingle = new AlertDialog.Builder(BikesActivity.this);
            builderSingle.setIcon(R.drawable.error);
            builderSingle.setTitle("無法連結到網路");

            builderSingle.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builderSingle.setCancelable(false);
            builderSingle.show();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_LOCATION);
            return;
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        responseTask = new TimerTask(){
            @Override
            public void run() {
                getMyLocation();
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
                getMyLocation();
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
                Log.d(TAG, "onResponse: " + latitude + "/" +longitude);

                for (UBike uBike : uBikes) {
                    float distance = distanceBetween(Double.parseDouble(uBike.getLat()),Double.parseDouble(uBike.getLng()),latitude,longitude);
                    uBike.setDistance(distance);
                }
                List<Bike> results = BikeDatabase.getInstance(BikesActivity.this).bikeDao().getAll();
                for(UBike uBike: uBikes){
                    for (Bike result : results) {
                        if(uBike.getSno().equals(result.sno)){
                            uBike.setStar(true);
                        }
                    }
                }
                Collections.sort(uBikes, new bikeSort());


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
            int listIndex = 0;
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
                    if(flag){
                        uBikes.add(uBike);
                    }else {
                        uBikes.set(listIndex,uBike);
                        listIndex++;
                    }
                }
            }





        } catch (JSONException e) {
            e.printStackTrace();
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
        float result = f.setScale(1,BigDecimal.ROUND_HALF_UP).floatValue();
        Log.d(TAG, "distanceBetween: " + result);
        return result;
    }




    @SuppressLint("MissingPermission")
    public void getMyLocation() {

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient( this);
        client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {

            @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if(task.isSuccessful()){
                        Location location = task.getResult();
                        Log.d(TAG, "onComplete: " + location.getLatitude() + "," + location.getLongitude()); //使用者位置
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();

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

    class bikeSort implements Comparator<UBike> {

        @Override
        public int compare(UBike a, UBike b) {
            return Float.compare(a.getDistance(), b.getDistance());
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_LOCATION &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED){
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            responseTask = new TimerTask(){
                @Override
                public void run() {
                    getMyLocation();
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
    }


}