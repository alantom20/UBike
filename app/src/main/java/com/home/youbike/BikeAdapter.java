package com.home.youbike;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.math.BigDecimal;
import java.util.List;

public class BikeAdapter extends RecyclerView.Adapter<BikeAdapter.BikeHolder>{
    private static final String TAG = BikeAdapter.class.getSimpleName();
    Context context;
    List<UBike> uBikes;
    public BikeAdapter(List<UBike> uBikes,Context context) {
        this.context = context;
        this.uBikes = uBikes;

    }


    @NonNull
    @Override
    public BikeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_bike,parent,false);
        return  new BikeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BikeHolder holder, int position) {
        UBike uBike = uBikes.get(position);
       // holder.timeText.setText(uBike.getMday());
        holder.loveImage.setImageResource(R.drawable.ic_love_empty);
        holder.loveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               holder.loveImage.setImageResource(R.drawable.ic_love);
            }
        });
        holder.titleText.setText(uBike.getSna());
        if(uBike.getDistance()>=1000){
            BigDecimal f = new BigDecimal(uBike.getDistance()/1000);
            String result = f.setScale(2,BigDecimal.ROUND_HALF_UP).toString();
            holder.distanceText.setText("約"+String.valueOf(result) + "公里");
        }else {
            holder.distanceText.setText("約"+String.valueOf(uBike.getDistance()) + "公尺");
        }
        holder.lendText.setText("可借車輛:" + uBike.getSbi());
        holder.parkingText.setText("可停空位:"+uBike.getBemp());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,MapsActivity.class);
                double lat = Double.parseDouble(uBike.getLat());
                double lng = Double.parseDouble(uBike.getLng());
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("title",uBike.getSna());
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return uBikes.size();
    }

    public class BikeHolder extends RecyclerView.ViewHolder{
        ImageView loveImage;
        TextView titleText;
        TextView distanceText;
        TextView lendText;
        TextView parkingText;
        TextView timeText;
        public BikeHolder(@NonNull View itemView) {
            super(itemView);
            loveImage = itemView.findViewById(R.id.image_love);
            titleText = itemView.findViewById(R.id.text_title);
            distanceText = itemView.findViewById(R.id.text_distance);
            lendText = itemView.findViewById(R.id.text_lend);
            parkingText = itemView.findViewById(R.id.text_parking);
            //timeText =  itemView.findViewById(R.id.time);

        }
    }

}
