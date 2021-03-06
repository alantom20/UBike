package com.home.youbike;


import org.json.JSONException;
import org.json.JSONObject;

public class UBike {
    private String sno; //站點代號
    private String sna; //場站中文名稱
    private String tot; //場站總停車格
    private String sbi; //場站目前車輛數量
    private String sarea; //場站區域
    private String mday; //資料更新時間
    private String lat; //緯度
    private String lng; //經度
    private String ar; //地點
    private String sareaen; //場站區域英文
    private String snaen; //場站名稱英文
    private String aren; //地址英文
    private String bemp; //空位數量
    private String act; //全站禁用狀態
    private float distance = 0.0f; //距離
    private boolean star = false;  //我的最愛






    public UBike() {
    }

    public UBike(String sno, String sna, String tot,
                 String sbi, String sarea, String mday,
                 String lat, String lng, String ar,
                 String sareaen, String snaen, String aren,
                 String bemp, String act)
    {
        this.sno = sno;
        this.sna = sna;
        this.tot = tot;
        this.sbi = sbi;
        this.sarea = sarea;
        this.mday = mday;
        this.lat = lat;
        this.lng = lng;
        this.ar = ar;
        this.sareaen = sareaen;
        this.snaen = snaen;
        this.aren = aren;
        this.bemp = bemp;
        this.act = act;
    }

    public UBike(JSONObject object2) {
        try {
            sno = object2.getString("sno");
            sna = object2.getString("sna");
            tot = object2.getString("tot");
            sbi = object2.getString("sbi");
            sarea = object2.getString("sarea");
            mday = object2.getString("mday");
            lat = object2.getString("lat");
            lng = object2.getString("lng");
            ar = object2.getString("ar");
           sareaen = object2.getString("sareaen");
           snaen = object2.getString("snaen");
            aren = object2.getString("aren");
            bemp = object2.getString("bemp");
            act = object2.getString("act");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public boolean isStar() {
        return star;
    }

    public void setStar(boolean star) {
        this.star = star;
    }


    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    public String getSna() {
        return sna;
    }

    public void setSna(String sna) {
        this.sna = sna;
    }

    public String getTot() {
        return tot;
    }

    public void setTot(String tot) {
        this.tot = tot;
    }

    public String getSbi() {
        return sbi;
    }

    public void setSbi(String sbi) {
        this.sbi = sbi;
    }

    public String getSarea() {
        return sarea;
    }

    public void setSarea(String sarea) {
        this.sarea = sarea;
    }

    public String getMday() {
        return mday;
    }

    public void setMday(String mday) {
        this.mday = mday;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getAr() {
        return ar;
    }

    public void setAr(String ar) {
        this.ar = ar;
    }

    public String getSareaen() {
        return sareaen;
    }

    public void setSareaen(String sareaen) {
        this.sareaen = sareaen;
    }

    public String getSnaen() {
        return snaen;
    }

    public void setSnaen(String snaen) {
        this.snaen = snaen;
    }

    public String getAren() {
        return aren;
    }

    public void setAren(String aren) {
        this.aren = aren;
    }

    public String getBemp() {
        return bemp;
    }

    public void setBemp(String bemp) {
        this.bemp = bemp;
    }

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }
}






