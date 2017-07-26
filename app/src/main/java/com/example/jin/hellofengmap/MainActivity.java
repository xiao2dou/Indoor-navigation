package com.example.jin.hellofengmap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.fengmap.android.FMMapSDK;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapView;

public class MainActivity extends AppCompatActivity {

    FMMap mFMMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FMMapSDK.init(getApplication());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FMMapView mapView = (FMMapView) findViewById(R.id.mapview);
        mFMMap = mapView.getFMMap();       //获取地图操作对象

        String bid = "10380";             //地图id
        mFMMap.openMapById(bid, true);          //打开地图
    }

    @Override
    public void onBackPressed() {
        if (mFMMap != null) {
            mFMMap.onDestroy();
        }
        super.onBackPressed();
    }
}
