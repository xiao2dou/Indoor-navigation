package com.example.jin.hellofengmap;

import android.content.Intent;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.fengmap.android.FMMapSDK;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapView;

public class MainActivity extends AppCompatActivity {

    FMMap mFMMap;
    FMMapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FMMapSDK.init(getApplication());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        mapView = (FMMapView) findViewById(R.id.mapview);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                Intent intent=new Intent(MainActivity.this,MySettingActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 地图手势控制
     * @param bol
     */
    public void mapController(boolean bol){
        //地图旋转
        mapView.getFMMapGestureEnableController().setEnableMapRotate(bol);
        //地图缩放
        mapView.getFMMapGestureEnableController().setEnableMapScale(bol);
        //地图平移
        mapView.getFMMapGestureEnableController().setEnableMapDrag(bol);
        //地图倾斜
        mapView.getFMMapGestureEnableController().setEnableMapTilt(bol);
        //地图惯性滑动
        mapView.getFMMapGestureEnableController().setEnableMapSwipe(bol);
    }
}
