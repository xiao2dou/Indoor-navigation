package com.example.jin.hellofengmap;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.fengmap.android.FMDevice;
import com.fengmap.android.FMMapSDK;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapView;
import com.fengmap.android.map.FMViewMode;
import com.fengmap.android.map.animator.FMLinearInterpolator;
import com.fengmap.android.map.event.OnFMCompassListener;
import com.fengmap.android.map.event.OnFMSwitchGroupListener;
import com.fengmap.android.widget.FM3DControllerButton;
import com.fengmap.android.widget.FMFloorControllerComponent;
import com.fengmap.android.widget.FMZoomComponent;

public class MainActivity extends AppCompatActivity implements OnFMCompassListener,OnFMSwitchGroupListener {

    FMMap mFMMap;
    FMMapView mapView;

    FM3DControllerButton m3DTextButton;
    private FMFloorControllerComponent mFloorComponent;
    private boolean isAnimateEnd = true;
    private FMZoomComponent mZoomComponent;

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

        openMapByPath();
    }

    /**
     * 加载地图数据
     */
    private void openMapByPath() {
        mapView = (FMMapView) findViewById(R.id.mapview);
        mFMMap = mapView.getFMMap();       //获取地图操作对象

        String bid = "10380";             //地图id
        mFMMap.openMapById(bid, true);          //打开地图

        onMapInitSuccess();
    }

    /**
     * 加载地图成功
     */
    private void onMapInitSuccess(){
        //显示2/3维地图控制
        init3DControllerComponent();
        //设置指北针点击事件
        mFMMap.setOnFMCompassListener(this);
        //显示指南针
        mFMMap.showCompass();
        //楼层切换
        if (mFloorComponent == null) {
            initFloorControllerComponent();
        }
        //缩放控制
        if (mZoomComponent == null) {
            initZoomComponent();
        }
    }

    /**
     * 初始化缩放控件
     */
    private void initZoomComponent() {
        mZoomComponent = new FMZoomComponent(MainActivity.this);
        mZoomComponent.measure(0, 0);
        int width = mZoomComponent.getMeasuredWidth();
        int height = mZoomComponent.getMeasuredHeight();
        //缩放控件位置
        int offsetX = FMDevice.getDeviceWidth() - width - 10;
        int offsetY = FMDevice.getDeviceHeight() - 400 - height;
        mapView.addComponent(mZoomComponent, offsetX, offsetY);

        mZoomComponent.setOnFMZoomComponentListener(new FMZoomComponent.OnFMZoomComponentListener() {
            @Override
            public void onZoomIn(View view) {
                //地图放大
                mFMMap.zoomIn();
            }

            @Override
            public void onZoomOut(View view) {
                //地图缩小
                mFMMap.zoomOut();
            }
        });
    }

    /**
     * 楼层切换控件初始化
     */
    private void initFloorControllerComponent() {
        // 楼层切换
        mFloorComponent = new FMFloorControllerComponent(this);
        mFloorComponent.setMaxItemCount(4);
        //楼层切换事件监听
        mFloorComponent.setOnFMFloorControllerComponentListener(new FMFloorControllerComponent.OnFMFloorControllerComponentListener() {
            @Override
            public void onSwitchFloorMode(View view, FMFloorControllerComponent.FMFloorMode currentMode) {
                if (currentMode == FMFloorControllerComponent.FMFloorMode.SINGLE) {
                    setSingleDisplay();
                } else {
                    setMultiDisplay();
                }
            }

            @Override
            public boolean onItemSelected(int groupId, String floorName) {
                if (isAnimateEnd) {
                    switchFloor(groupId);
                    return true;
                }
                return false;
            }
        });
        //设置为单层模式
        mFloorComponent.setFloorMode(FMFloorControllerComponent.FMFloorMode.SINGLE);
        int groupId = 1;
        mFloorComponent.setFloorDataFromFMMapInfo(mFMMap.getFMMapInfo(), groupId);

        int offsetX = (int) (FMDevice.getDeviceDensity() * 5);
        int offsetY = (int) (FMDevice.getDeviceDensity() * 130);
        mapView.addComponent(mFloorComponent, offsetX, offsetY);
    }

    /**
     * 切换楼层
     *
     * @param groupId 楼层id
     */
    void switchFloor(int groupId) {
        mFMMap.setFocusByGroupIdAnimated(groupId, new FMLinearInterpolator(), this);
    }

    /**
     * 单层显示
     */
    void setSingleDisplay() {
        int[] gids = {mFMMap.getFocusGroupId()};       //获取当前地图焦点层id
        mFMMap.setMultiDisplay(gids, 0, this);
    }

    /**
     * 多层显示
     */
    void setMultiDisplay() {
        int[] gids = mFMMap.getMapGroupIds();    //获取地图所有的group
        FMFloorControllerComponent.FloorData fd = mFloorComponent.getFloorData(mFloorComponent.getSelectedPosition());
        int focus = 0;
        for (int i = 0; i < gids.length; i++) {
            if (gids[i] == fd.getGroupId()) {
                focus = i;
                break;
            }
        }
        mFMMap.setMultiDisplay(gids, focus, this);
    }

    /**
     * 组切换开始之前。
     */
    @Override
    public void beforeGroupChanged() {
        isAnimateEnd = false;
    }

    /**
     * 组切换结束之后。
     */
    @Override
    public void afterGroupChanged() {
        isAnimateEnd = true;
    }

    /**
     * 加载2d/3d切换控件
     */
    private void init3DControllerComponent() {
        m3DTextButton = new FM3DControllerButton(this);
        //设置初始状态为3D(true),设置为false为2D模式
        m3DTextButton.initState(true);
        m3DTextButton.measure(0, 0);
        int width = m3DTextButton.getMeasuredWidth();
        //设置3D控件位置
        mapView.addComponent(m3DTextButton, FMDevice.getDeviceWidth() - 10 - width, 50);
        //2、3D点击监听
        m3DTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FM3DControllerButton button = (FM3DControllerButton) v;
                if (button.isSelected()) {
                    button.setSelected(false);
                    mFMMap.setFMViewMode(FMViewMode.FMVIEW_MODE_2D);
                } else {
                    button.setSelected(true);
                    mFMMap.setFMViewMode(FMViewMode.FMVIEW_MODE_3D);
                }
            }
        });
    }

    /**
     * 地图销毁调用
     */
    @Override
    public void onBackPressed() {
        if (mFMMap != null) {
            mFMMap.onDestroy();
        }
        super.onBackPressed();
    }

    /**
     * 菜单点击监听
     * @param item
     * @return
     */
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

    /**
     * 指南针点击事件回调
     */
    @Override
    public void onCompassClick() {
        //恢复为初始状态
        mFMMap.resetCompassToNorth();
    }
}
