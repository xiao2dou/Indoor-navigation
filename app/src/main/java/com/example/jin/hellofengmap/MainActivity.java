package com.example.jin.hellofengmap;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.jin.hellofengmap.location.Destination;
import com.example.jin.hellofengmap.location.Location;
import com.example.jin.hellofengmap.location.iBeacon;
import com.example.jin.hellofengmap.location.iBeaconClass;
import com.example.jin.hellofengmap.utils.SnackbarUtil;
import com.example.jin.hellofengmap.utils.ViewHelper;
import com.fengmap.android.FMDevice;
import com.fengmap.android.FMErrorMsg;
import com.fengmap.android.FMMapSDK;
import com.fengmap.android.analysis.navi.FMNaviAnalyser;
import com.fengmap.android.analysis.navi.FMNaviResult;
import com.fengmap.android.data.OnFMDownloadProgressListener;
import com.fengmap.android.exception.FMObjectException;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapUpgradeInfo;
import com.fengmap.android.map.FMMapView;
import com.fengmap.android.map.FMPickMapCoordResult;
import com.fengmap.android.map.FMViewMode;
import com.fengmap.android.map.animator.FMLinearInterpolator;
import com.fengmap.android.map.event.OnFMCompassListener;
import com.fengmap.android.map.event.OnFMMapClickListener;
import com.fengmap.android.map.event.OnFMMapInitListener;
import com.fengmap.android.map.event.OnFMNodeListener;
import com.fengmap.android.map.event.OnFMSwitchGroupListener;
import com.fengmap.android.map.geometry.FMMapCoord;
import com.fengmap.android.map.layer.FMFacilityLayer;
import com.fengmap.android.map.layer.FMImageLayer;
import com.fengmap.android.map.layer.FMLineLayer;
import com.fengmap.android.map.layer.FMLocationLayer;
import com.fengmap.android.map.layer.FMModelLayer;
import com.fengmap.android.map.marker.FMFacility;
import com.fengmap.android.map.marker.FMImageMarker;
import com.fengmap.android.map.marker.FMLineMarker;
import com.fengmap.android.map.marker.FMLocationMarker;
import com.fengmap.android.map.marker.FMModel;
import com.fengmap.android.map.marker.FMNode;
import com.fengmap.android.map.marker.FMSegment;
import com.fengmap.android.widget.FM3DControllerButton;
import com.fengmap.android.widget.FMFloorControllerComponent;
import com.fengmap.android.widget.FMZoomComponent;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements OnFMMapInitListener,
        OnFMCompassListener, OnFMSwitchGroupListener, OnFMMapClickListener {

    FMMap mFMMap;
    FMMapView mapView;
    FloatingActionButton btnMyLocation;//定位按钮

    Handler mHandler = new Handler();

    //地图控制按钮
    private FM3DControllerButton m3DTextButton;
    private FMFloorControllerComponent mFloorComponent;
    private boolean isAnimateEnd = true;
    private FMZoomComponent mZoomComponent;

    private FMFacilityLayer mFacilityLayer;//公共设施图层
    private FMModelLayer mModelLayer;//模型图层
    private int mGroupId = 1;//默认楼层
    private FMModel mClickedModel;

    private FMImageLayer mImageLayer;//点标注
    Snackbar snackbar;

    public static BluetoothAdapter mBluetoothAdapter;//蓝牙
    public boolean isScanning = false;
    public Timer timer;//定时器
    public List<iBeacon> mIBeaconList = new ArrayList<>();//所有扫描到的数据

    private FMLocationLayer mLocationLayer;
    private FMLocationMarker mLocationMarker;

    /**
     * 线图层
     */
    protected FMLineLayer mLineLayer;
    /**
     * 导航分析
     */
    protected FMNaviAnalyser mNaviAnalyser;
    /**
     * 起点坐标
     */
    protected FMMapCoord stCoord;
    /**
     * 起点楼层
     */
    protected int stGroupId;
    /**
     * 起点图层
     */
    protected FMImageLayer stImageLayer;
    /**
     * 终点坐标
     */
    protected FMMapCoord endCoord;
    /**
     * 终点楼层id
     */
    protected int endGroupId;
    /**
     * 终点图层
     */
    protected FMImageLayer endImageLayer;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //动态权限申请
        if (Build.VERSION.SDK_INT < 23) {
            // Android 6.0 之前无需运行时权限申请
        } else {

            //android 6.0及以上动态权限申请
            //判断是否有权限（蓝牙）
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                //判断是否需要 向用户解释，为什么要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                    Toast.makeText(this, "定位需要开启蓝牙", Toast.LENGTH_SHORT).show();
                }
            }

            // 先检测权限   目前SDK只需2个危险权限，读和写存储卡
            int p1 = MainActivity.this.checkSelfPermission(FMMapSDK.SDK_PERMISSIONS[0]);
            int p2 = MainActivity.this.checkSelfPermission(FMMapSDK.SDK_PERMISSIONS[1]);
            // 只要有任一权限没通过，则申请
            if (p1 != PackageManager.PERMISSION_GRANTED || p2 != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(FMMapSDK.SDK_PERMISSIONS,                //SDK所需权限数组
                        FMMapSDK.SDK_PERMISSION_RESULT_CODE);   //SDK权限申请处理结果返回码
            } else {
                // 已经拥有权限了
            }
        }


        //初始化SDK
        FMMapSDK.init(getApplication());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 使用此检查来确定设备是否支持BLE。 然后，您可以选择性地禁用BLE相关功能。
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "您的设备不支持BLE", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 初始化蓝牙适配器。
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //检查该设备是否支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "你的设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //开启蓝牙
        mBluetoothAdapter.enable();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        //定位
        btnMyLocation = (FloatingActionButton) findViewById(R.id.btn_my_location);
        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location.isProblem = false;//默认无问题
                beginScanLocation();
                Log.d("button", "onClick: location");
            }
        });

        openMapByPath();
    }

    /**
     * 加载地图数据
     */
    private void openMapByPath() {
        mapView = (FMMapView) findViewById(R.id.mapview);
        mFMMap = mapView.getFMMap();       //获取地图操作对象

        String bid = "10347";             //地图id
        //监听地图的加载状况
        mFMMap.setOnFMMapInitListener(this);
        //打开地图
        //第一个参数为地图ID，将会自动在缓存里面找地图文件，第二个参数为是否自动在线更新地图
        mFMMap.openMapById(bid, true);
        //地图点击事件
        mFMMap.setOnFMMapClickListener(this);
    }


    /**
     * 地图加载成功回调事件
     *
     * @param path 地图所在sdcard路径
     */
    @Override
    public void onMapInitSuccess(String path) {
        // 加载地图主题
        // 若本地存在对应ID的主题，将从本地读取，若不存在则在线加载。由于网络问题，无特殊要求，建议使用加载离线主题。
        mFMMap.loadThemeById("2001");
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

        int groupId = mFMMap.getFocusGroupId();
        //清空标记
        mImageLayer.removeAll();
        //公共设施图层
        mFacilityLayer = mFMMap.getFMLayerProxy().getFMFacilityLayer(groupId);
        mFacilityLayer.setOnFMNodeListener(mOnFacilityClickListener);
        mFMMap.addLayer(mFacilityLayer);

        //模型图层
        mModelLayer = mFMMap.getFMLayerProxy().getFMModelLayer(groupId);
        mModelLayer.setOnFMNodeListener(mOnModelCLickListener);
        mFMMap.addLayer(mModelLayer);

        //图片图层
        mImageLayer = mFMMap.getFMLayerProxy().createFMImageLayer(mFMMap.getFocusGroupId());
        mFMMap.addLayer(mImageLayer);

        //获取定位图层
        mLocationLayer = mFMMap.getFMLayerProxy().getFMLocationLayer();
        mFMMap.addLayer(mLocationLayer);

        //线图层
        mLineLayer = mFMMap.getFMLayerProxy().getFMLineLayer();
        mFMMap.addLayer(mLineLayer);

        //导航分析
        try {
            mNaviAnalyser = FMNaviAnalyser.getFMNaviAnalyserById("10347");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (FMObjectException e) {
            e.printStackTrace();
        }

    }

    /**
     * 地图加载失败回调事件
     *
     * @param path      地图所在sdcard路径
     * @param errorCode 失败加载错误码，可以通过{@link FMErrorMsg#getErrorMsg(int)}获取加载地图失败详情
     */
    @Override
    public void onMapInitFailure(String path, int errorCode) {
        //TODO 可以提示用户地图加载失败原因，进行地图加载失败处理
        Toast.makeText(this, "加载地图失败，请检查网络设置", Toast.LENGTH_SHORT).show();
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

        //updateFloorButton();
    }

    /**
     * 切换楼层
     *
     * @param groupId 楼层id
     */
    void switchFloor(int groupId) {
        mFMMap.setFocusByGroupIdAnimated(groupId, new FMLinearInterpolator(), this);

        //切换图层
        mGroupId = groupId;
        //mImageLayer.removeAll();
        //公共设施图层
        mFacilityLayer = mFMMap.getFMLayerProxy().getFMFacilityLayer(groupId);
        mFacilityLayer.setOnFMNodeListener(mOnFacilityClickListener);
        mFMMap.addLayer(mFacilityLayer);

        //模型图层
        mModelLayer = mFMMap.getFMLayerProxy().getFMModelLayer(groupId);
        mModelLayer.setOnFMNodeListener(mOnModelCLickListener);
        mFMMap.addLayer(mModelLayer);

        //图片图层
        mImageLayer = mFMMap.getFMLayerProxy().createFMImageLayer(mFMMap.getFocusGroupId());
        mFMMap.addLayer(mImageLayer);

        //获取定位图层
        mLocationLayer = mFMMap.getFMLayerProxy().getFMLocationLayer();
        mFMMap.addLayer(mLocationLayer);

        //线图层
        mLineLayer = mFMMap.getFMLayerProxy().getFMLineLayer();
        mFMMap.addLayer(mLineLayer);

        //导航分析
        try {
            mNaviAnalyser = FMNaviAnalyser.getFMNaviAnalyserById("10347");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (FMObjectException e) {
            e.printStackTrace();
        }

        //清空标记
        if (groupId == Destination.groupId) {
            showMarket(Destination.myDestination);
        }

        //清除定位点标注
        if (groupId != Location.groupId) {
            clearLocationMarker();
        } else {
            updateLocationMarker();
        }

        Log.d("切换楼层成功", "switchFloor: ");
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
     * 地图手势控制
     *
     * @param bol
     */
    public void mapController(boolean bol) {
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

    /**
     * 处理权限申请结果
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 申请权限被拒绝，则退出程序。
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                grantResults[1] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "拒绝了必要权限，无法使用该程序！", Toast.LENGTH_SHORT).show();
            this.finish();
        } else if (requestCode == FMMapSDK.SDK_PERMISSION_RESULT_CODE) {
            // SDK所需权限被允许
        }
    }


    /**
     * 当{@link FMMap#openMapById(String, boolean)}设置openMapById(String, false)时地图不自动更新会
     * 回调此事件，可以调用{@link FMMap#upgrade(FMMapUpgradeInfo, OnFMDownloadProgressListener)}进行
     * 地图下载更新
     *
     * @param upgradeInfo 地图版本更新详情,地图版本号{@link FMMapUpgradeInfo#getVersion()},<br/>
     *                    地图id{@link FMMapUpgradeInfo#getMapId()}
     * @return 如果调用了{@link FMMap#upgrade(FMMapUpgradeInfo, OnFMDownloadProgressListener)}地图下载更新，
     * 返回值return true,因为{@link FMMap#upgrade(FMMapUpgradeInfo, OnFMDownloadProgressListener)}
     * 会自动下载更新地图，更新完成后会加载地图;否则return false。
     */
    @Override
    public boolean onUpgrade(FMMapUpgradeInfo upgradeInfo) {
        boolean isUpgrade = upgradeInfo.isNeedUpgrade();
        if (isUpgrade) { // 有新版本更新
            // 调用更新接口，返回true，SDK内部会去加载新地图并显示
            mFMMap.upgrade(upgradeInfo, new OnFMDownloadProgressListener() {
                @Override
                public void onCompleted(String mapPath) {
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {

                }

                @Override
                public void onFailure(String mapPath, int errorCode) {

                }
            });

            return true;
        } else {  // 无更新，返回false则SDK内部会去加载本地地图并显示
            return false;
        }
    }


        /**
     * 模型点击事件
     */
    private OnFMNodeListener mOnModelCLickListener = new OnFMNodeListener() {
        @Override
        public boolean onClick(FMNode node) {
            if (mClickedModel != null) {
                mClickedModel.setSelected(false);
            }
            FMModel model = (FMModel) node;
            mClickedModel = model;

            model.setSelected(true);
            mFMMap.updateMap();
            final FMMapCoord centerMapCoord = model.getCenterMapCoord();

            showMarket(centerMapCoord);

            //String content = getString(R.string.event_click_content, "模型:"+mClickedModel.getName(), mGroupId, centerMapCoord.x, centerMapCoord.y);
            //建立SnackBar提示用户点击的地图信息
            CoordinatorLayout mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
            String name = mClickedModel.getName();
            Log.d("Test", "onClick: " + name + "len" + name.length());
            if (name.length() == 0) {
                name = "未命名区域";
            }
            Log.d("Test", "onClick: " + name + "len" + name.length());
            snackbar = Snackbar.make(mCoordinatorLayout, name, Snackbar.LENGTH_INDEFINITE);
            SnackbarUtil.setBackgroundColor(snackbar, SnackbarUtil.blue);
            SnackbarUtil.SnackbarAddView(snackbar, R.layout.snackbar, 0);
            View view = snackbar.getView();
            Button go_there = (Button) view.findViewById(R.id.go);
            //go_there.setBackgroundColor(Color.WHITE);

            //"去这里"按钮的点击事件
            go_there.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goThere(centerMapCoord);
                }
            });
            snackbar.show();
            //Toast.makeText(MainActivity.this,content,Toast.LENGTH_SHORT).show();
            //ViewHelper.setViewText(MainActivity.this, R.id.map_result, content);
            return true;
        }

        @Override
        public boolean onLongPress(FMNode node) {
            return false;
        }
    };
    /**
     * 公共设施点击事件
     */
    private OnFMNodeListener mOnFacilityClickListener = new OnFMNodeListener() {
        @Override
        public boolean onClick(FMNode node) {
            FMFacility facility = (FMFacility) node;
            final FMMapCoord centerMapCoord = facility.getPosition();

            showMarket(centerMapCoord);

            //String content = getString(R.string.event_click_content, "公共设施", mGroupId, centerMapCoord.x, centerMapCoord.y);
            //Toast.makeText(MainActivity.this,content,Toast.LENGTH_SHORT).show();

            //LinearLayout linearLayout=(LinearLayout)findViewById(R.id.activity_main_layout);
            CoordinatorLayout mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
            //建立SnackBar提示用户点击模型的信息
            snackbar = Snackbar.make(mCoordinatorLayout, "公共设施", Snackbar.LENGTH_INDEFINITE);
            SnackbarUtil.setBackgroundColor(snackbar, SnackbarUtil.blue);
            SnackbarUtil.SnackbarAddView(snackbar, R.layout.snackbar, 0);
            View view = snackbar.getView();
            Button go_there = (Button) view.findViewById(R.id.go);

            //"去这里"按钮的点击事件
            go_there.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goThere(centerMapCoord);
                }
            });
            snackbar.show();
            //ViewHelper.setViewText(MainActivity.this, R.id.map_result, content);
            return true;
        }


        @Override
        public boolean onLongPress(FMNode node) {
            return false;
        }
    };

    void goThere(FMMapCoord centerMapCoord) {
        Toast.makeText(MainActivity.this, "gogogo", Toast.LENGTH_SHORT).show();
        Destination.myDestination = centerMapCoord;
        Destination.groupId = mGroupId;
        endGroupId=Destination.groupId;
        endCoord=Destination.myDestination;
        createEndImageMarker();
        //开始分析导航
        analyzeNavigation();
        // 画完置空
        stCoord = null;
        endCoord = null;
    }

    /**
     * 地图点击事件
     *
     * @param x
     * @param y
     */
    @Override
    public void onMapClick(float x, float y) {
        FMPickMapCoordResult mapCoordResult = mFMMap.pickMapCoord(x, y);

        double pX = x;
        double pY = y;
        if (mapCoordResult != null) {
            FMMapCoord mapCoord = mapCoordResult.getMapCoord();
            pX = mapCoord.x;
            pY = mapCoord.y;
        }
        if (snackbar != null) {
            snackbar.dismiss();
        }

        String content = getString(R.string.event_click_content, "地图", mGroupId, pX, pY);
        Toast.makeText(MainActivity.this,content,Toast.LENGTH_SHORT).show();
        //ViewHelper.setViewText(MainActivity.this, R.id.map_result, content);
    }

    /**
     * 显示标记
     *
     * @param centerMapCoord 标记位置
     * @author jin
     */
    public void showMarket(FMMapCoord centerMapCoord) {
        if (centerMapCoord != null && mImageLayer == null) {
            //添加图片标注
            FMImageMarker imageMarker = ViewHelper.buildImageMarker(getResources(),
                    centerMapCoord, R.drawable.ic_marker_blue);
            mImageLayer.addMarker(imageMarker);
        } else if (centerMapCoord != null && mImageLayer != null) {
            //移除现有标记
            mImageLayer.removeAll();
            //添加图片标注
            FMImageMarker imageMarker = ViewHelper.buildImageMarker(getResources(),
                    centerMapCoord, R.drawable.ic_marker_blue);
            mImageLayer.addMarker(imageMarker);
        }
    }

    /**
     * 菜单点击监听
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(MainActivity.this, MySettingActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 开始定位
     */
    public void beginScanLocation() {
        scanLeDevice(true, 2000);
        timer = new Timer(true);
        timer.schedule(task, 1000); //延时0ms后执行
    }

    /**
     * 开始导航
     */
    public void beginScanNavigate() {
        scanLeDevice(true, 0);
        timer = new Timer(true);
        timer.schedule(task, 4000, 3000); //延时0ms后执行，1000ms执行一次
    }

    public void stopScan() {
        scanLeDevice(false, 0);
        timer.cancel();
    }

    /**
     * 扫描
     * 新开线程
     *
     * @param enable
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevice(final boolean enable, long time) {
        if (enable && time == 0)//enable =true就是说要开始扫描
        {
            mBluetoothAdapter.startLeScan(mLeScanCallback);//这句就是开始扫描了
            isScanning = true;
        } else if (enable && time > 0) {
            // Stops scanning after a pre-defined scan period.
            // 下边的代码是为了在 SCAN_PERIOD （以毫秒位单位）后，停止扫描的
            // 如果需要不停的扫描，可以注释掉
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    // 这个是重置menu，将 menu中的停止按钮->扫描按钮
                    invalidateOptionsMenu();
                }
            }, time);
            mBluetoothAdapter.startLeScan(mLeScanCallback);//这句就是开始扫描了
            isScanning = true;
        } else if (!enable) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);//这句就是停止扫描
            isScanning = false;
        }
        // 这个是重置menu，将 menu中的扫描按钮->停止按钮
        invalidateOptionsMenu();
    }

    /**
     * Device scan callback.
     * 回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            final iBeacon ibeacon = iBeaconClass.fromScanData(device, rssi, scanRecord);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ibeacon != null && ibeacon.getMinor() != 0) {//扫描到有效信息
                        Log.d("Scan iBeacon", "run: " + ibeacon.getBluetoothAddress());
                        mIBeaconList.add(ibeacon);
                    }
                }
            });
        }
    };

    //定时执行
    TimerTask task = new TimerTask() {
        public void run() {
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

    //异步消息处理机制
    //定义处理消息的对象
    public Handler handler = new Handler() {
        /**
         * 处理消息
         * @param msg
         */
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    break;
                case 1://1s到，处理、发送数据
                    if (Location.isProblem == false) {
                        //Location.ProcessData(MainActivity.this, mIBeaconList);
                        locationMarker();
                    }
                    mIBeaconList.clear();
                    break;
                case 2:
                    locationMarker();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 添加/更新定位点标注
     *
     * @return
     * @author jin
     * DATA:2017/7/31
     */
    private boolean locationMarker() {

        String TAG = "LocationMarker";

        Location.groupId = 1;
        Location.myLocation.x = 1.296164E7;
        Location.myLocation.y = 4861845.0;

        if (Location.groupId != 0 && Location.myLocation.x != 0 && Location.myLocation.y != 0) {

            Log.d(TAG, "locationMarker: " + Location.groupId);
            //切换地图
            switchFloor(Location.groupId);
            //更新楼层控制组件
            updateFloorButton(Location.groupId);
            //刷新定位点
            updateLocationMarker();

            clear();

            stCoord = Location.myLocation;
            stGroupId = Location.groupId;
            createStartImageMarker();

        }
        return true;
    }

    /**
     * 修改楼层控件
     */
    void updateFloorButton(int groupId) {
        String TAG = "切换楼层控件";
        Log.d(TAG, "handleMessage: GroupId" + Location.groupId);
        FMFloorControllerComponent.FloorData[] mFloorDatas = mFloorComponent.getFloorDatas();
        for (int i = 0; i < mFloorDatas.length; i++) {
            if (mFloorDatas[i].getGroupId() == groupId) {
                Log.d(TAG, "changeFloorButton: ====");
                if (i != mFloorComponent.getSelectedPosition()) {
                    mFloorComponent.setSelected(i);
                    invokeFloorComponentNotify(mFloorComponent);
                }
                break;
            }
        }
    }

    void invokeFloorComponentNotify(FMFloorControllerComponent floorComponent) {
        Class clazz = FMFloorControllerComponent.class;
        try {
            Method method = clazz.getDeclaredMethod("updateData");
            method.setAccessible(true);
            method.invoke(floorComponent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 更新定位点位置
     */
    private void updateLocationMarker() {
        //FMMapCoord CENTER_COORD = new FMMapCoord(1.296164E7, 4861845.0);
        String TAG = "定位点";
        Log.d(TAG, "updateLocationMarker: begin");
        if (mLocationMarker == null) {
            Log.d(TAG, "updateLocationMarker: add");
            int groupId = mFMMap.getFocusGroupId();
            Log.d(TAG, "updateLocationMarker: groupId=" + groupId);
            mLocationMarker = new FMLocationMarker(groupId, Location.myLocation);
            //设置定位点图片
            mLocationMarker.setActiveImageFromAssets("active.png");
            //设置定位图片宽高
            mLocationMarker.setMarkerWidth(90);
            mLocationMarker.setMarkerHeight(90);
            mLocationLayer.addMarker(mLocationMarker);
        } else {
            //更新定位点位置和方向
            Log.d(TAG, "updateLocationMarker: update");
            float angle = 0;
            mLocationMarker.updateAngleAndPosition(angle, Location.myLocation);
        }
    }

    /**
     * 清除定位标注
     */
    private void clearLocationMarker() {
        String TAG = "定位点";
        if (mLocationLayer != null) {
            Log.d(TAG, "clearLocationMarker: clear");
            mLocationLayer.removeAll();

            mLocationMarker = null;
        }
    }


    /**
     * 开始分析导航
     */
    private void analyzeNavigation() {
//        stGroupId=Location.groupId;
//        stCoord=Location.myLocation;
//        endGroupId=Destination.groupId;
//        endCoord=Destination.myDestination;
        int type = mNaviAnalyser.analyzeNavi(stGroupId, stCoord, endGroupId, endCoord,
                FMNaviAnalyser.FMNaviModule.MODULE_SHORTEST);
        if (type == FMNaviAnalyser.FMRouteCalcuResult.ROUTE_SUCCESS) {
            addLineMarker();
        }
    }

    /**
     * 清理所有的线与图层
     */
    protected void clear() {
        clearLineLayer();
        clearStartImageLayer();
        clearEndImageLayer();
    }


    /**
     * 清除线图层
     */
    protected void clearLineLayer() {
        if (mLineLayer != null) {
            mLineLayer.removeAll();
        }
    }

    /**
     * 清除起点图层
     */
    protected void clearStartImageLayer() {
        if (stImageLayer != null) {
            stImageLayer.removeAll();
            mFMMap.removeLayer(stImageLayer); // 移除图层
            stImageLayer = null;
        }
    }

    /**
     * 清除终点图层
     */
    protected void clearEndImageLayer() {
        if (endImageLayer != null) {
            endImageLayer.removeAll();
            mFMMap.removeLayer(endImageLayer); // 移除图层

            endImageLayer = null;
        }
    }


    /**
     *  添加线标注
     */
    protected void addLineMarker() {
        ArrayList<FMNaviResult> results = mNaviAnalyser.getNaviResults();
        // 填充导航数据
        ArrayList<FMSegment> segments = new ArrayList<>();
        for (FMNaviResult r : results) {
            int groupId = r.getGroupId();
            FMSegment s = new FMSegment(groupId, r.getPointList());
            segments.add(s);
        }
        //添加LineMarker
        FMLineMarker lineMarker = new FMLineMarker(segments);
        lineMarker.setLineWidth(3f);
        mLineLayer.addMarker(lineMarker);
    }

    /**
     * 创建起点图标
     */
    protected void createStartImageMarker() {
        clearStartImageLayer();
        // 添加起点图层
        stImageLayer = new FMImageLayer(mFMMap, stGroupId);
        mFMMap.addLayer(stImageLayer);
//        // 标注物样式
//        FMImageMarker imageMarker = ViewHelper.buildImageMarker(getResources(), stCoord, R.drawable.start);
//        stImageLayer.addMarker(imageMarker);
    }

    /**
     * 创建终点图层
     */
    protected void createEndImageMarker() {
        clearEndImageLayer();
        // 添加起点图层
        endImageLayer = new FMImageLayer(mFMMap, endGroupId);
        mFMMap.addLayer(endImageLayer);
        mImageLayer.removeAll();
        // 标注物样式
        FMImageMarker imageMarker = ViewHelper.buildImageMarker(getResources(), endCoord, R.drawable.end);
        endImageLayer.addMarker(imageMarker);
    }
}
