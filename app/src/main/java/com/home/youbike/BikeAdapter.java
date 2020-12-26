package com.home.youbike;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BikeAdapter extends RecyclerView.Adapter<BikeAdapter.BikeHolder>{
    private static final String TAG = BikeAdapter.class.getSimpleName();
    Context context;
    List<UBike> uBikes = new ArrayList<>();
    public BikeAdapter(List<UBike> uBikes,Context context) {
        this.context = context;
        if(uBikes != null){
            this.uBikes = uBikes;
        }

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
        if(uBike.isStar()){
            holder.loveImage.setImageResource(R.drawable.ic_love);
        }else {
            holder.loveImage.setImageResource(R.drawable.ic_love_empty);
        }

        holder.loveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uBike.setStar(!uBike.isStar());
                if(uBike.isStar()){
                    holder.loveImage.setImageResource(R.drawable.ic_love);
                    Bike bike = new Bike(uBike.getSno(),true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            BikeDatabase.getInstance(context).bikeDao().insert(bike);
                        }
                    }).start();

                }else {
                    holder.loveImage.setImageResource(R.drawable.ic_love_empty);
                    Log.d(TAG, "onClick: " + context.getClass().getSimpleName());
                    if((context.getClass().getSimpleName()).equals("FavoriteActivity")){
                        notifyItemRemoved(holder.getAdapterPosition());
                        notifyItemRangeChanged(position, uBikes.size());
                        uBikes.remove(position);
                    }
                  //  notifyDataSetChanged();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Bike result = BikeDatabase.getInstance(context).bikeDao().findByBike(uBike.getSno());
                            BikeDatabase.getInstance(context).bikeDao().delete(result);
                        }
                    }).start();
                }
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
                intent.putExtra("distance",uBike.getDistance());
                context.startActivity(intent);
            }
        });
       // holder.timeText.setText(uBike.getMday());


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
//            timeText = itemView.findViewById(R.id.time_text);


        }
    }


}
