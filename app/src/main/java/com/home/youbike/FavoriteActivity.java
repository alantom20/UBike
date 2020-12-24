package com.home.youbike;

import android.annotation.SuppressLint;
import android.content.Intent;

import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class FavoriteActivity extends AppCompatActivity {

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
    private List<UBike> newList = new ArrayList<>();
    private List<UBike> uBikesNewTaipei;
    private int listIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        findViews();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setupDownCounterTimer();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BikeAdapter(newList , FavoriteActivity.this);
        recyclerView.setAdapter(adapter);
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
                Intent intent = new Intent(FavoriteActivity.this,MapsActivity.class);
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
        toolbar = findViewById(R.id.toolbar_favorite);
        recyclerView = findViewById(R.id.recycler_favorite);
        timerText = findViewById(R.id.timer_text_favorite);
        mapButton = findViewById(R.id.button_map_favorite);
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
                latch.countDown();
                Log.d(TAG, "onResponse2: " + uBikesNewTaipei.size());

            }
        });
        OkHttpClient client1 = new OkHttpClient();
        client1.newCall(requestTaipei).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response)  throws IOException {
                String json = null;

                json = response.body().string();

                Log.d(TAG, "onResponse: " + json);
                JSONObject object = parseJSON(json);
                parseJSONObject(object);
                Log.d(TAG, "onResponse: " + latitude + "/" +longitude);
                try {
                    latch.await();                //取得完下一筆資料再往下執行
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "onResponse1:" + uBikes.size());
                if(flag){
                    uBikes.addAll(uBikesNewTaipei);
                }else{
                    for (UBike uBike : uBikesNewTaipei) {
                        uBikes.set(listIndex,uBike);
                        listIndex++;
                    }
                }
                Log.d(TAG, "onResponse1: " + uBikes.size());

                 //設定距離
                for (UBike uBike : uBikes) {
                    float distance = distanceBetween(Double.parseDouble(uBike.getLat()),Double.parseDouble(uBike.getLng()),latitude,longitude);
                    uBike.setDistance(distance);
                }

                List<Bike> results = BikeDatabase.getInstance(FavoriteActivity.this).bikeDao().getAll();  //取得資料庫list
                for(UBike uBike: uBikes){
                    for (Bike result : results) {
                        if(uBike.getSno().equals(result.sno)){  //如果資料庫有這筆資料star就設為true
                            uBike.setStar(true);
                        }
                    }
                }


                int i = 0;
                for (UBike uBike : uBikes) {
                    if(flag){                      //第一次執行
                        if(uBike.isStar()){         //將star為true的資料加入新的list
                            newList.add(uBike);
                    }
                    }else {
                        if(uBike.isStar()){  //第二次執行
                            newList.set(i,uBike);
                            i++;
                        }
                    }

                }
                Collections.sort(newList, new bikeSort()); //依距離做排序

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(flag) {
                            adapter.notifyDataSetChanged();
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
        getMenuInflater().inflate(R.menu.manu_favorite,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case  R.id.map_item_favorite:{
                Intent intent = new Intent(this,MapsActivity.class);
                startActivity(intent);
            }
            case  android.R.id.home :{
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    class bikeSort implements Comparator<UBike> {


        @Override
        public int compare(UBike a, UBike b) {
            return Float.compare(a.getDistance(), b.getDistance());
        }
    }
}