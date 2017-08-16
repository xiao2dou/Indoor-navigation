package com.example.jin.hellofengmap;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.jin.hellofengmap.location.Destination;
import com.example.jin.hellofengmap.location.FitRssi;
import com.example.jin.hellofengmap.location.Location;
import com.example.jin.hellofengmap.location.MapCoord;
import com.example.jin.hellofengmap.location.iBeacon;
import com.example.jin.hellofengmap.location.iBeaconClass;
import com.example.jin.hellofengmap.location.iBeacons;
import com.example.jin.hellofengmap.utils.ConvertUtils;
import com.example.jin.hellofengmap.utils.FMLocationAPI;
import com.example.jin.hellofengmap.utils.SnackbarUtil;
import com.example.jin.hellofengmap.utils.ViewHelper;
import com.example.jin.hellofengmap.widget.ImageViewCheckBox;
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
import com.fengmap.android.map.animator.FMValueAnimation;
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
import com.fengmap.android.utils.FMMath;
import com.fengmap.android.widget.FM3DControllerButton;
import com.fengmap.android.widget.FMFloorControllerComponent;
import com.fengmap.android.widget.FMSwitchFloorComponent;
import com.fengmap.android.widget.FMZoomComponent;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnFMMapInitListener,
        OnFMCompassListener, OnFMSwitchGroupListener, OnFMMapClickListener, Runnable,
        FMLocationAPI.OnFMLocationListener{

    /**
     * 个人信息里面的图片，昵称，电话，性别
     */
    public static Bitmap mainBitmap;
    public static String mainName = new String("昵称");
    public static String mainPhone = new String("电话");
    public static String mainGender = new String("性别");
    /**
     * 地图
     */
    FMMap mFMMap;
    /**
     * 地图容器
     */
    FMMapView mapView;
    /**
     * 定位按钮
     */
    FloatingActionButton btnMyLocation;
    /**
     * 定时扫描控制
     */
    Handler mHandler = new Handler();
    /**
     * 2/3维地图切换控制组件
     */
    private FM3DControllerButton m3DTextButton;
    /**
     * 楼层控制组件
     */
    private FMFloorControllerComponent mFloorComponent;
    /**
     * 楼层组件切换标记
     */
    private boolean isAnimateEnd = true;
    /**
     * 楼层切换控件
     */
    private FMSwitchFloorComponent mSwitchFloorComponent;
    /**
     * 地图缩放控制组件
     */
    private FMZoomComponent mZoomComponent;

    /**
     * 公共设施图层
     */
    private FMFacilityLayer mFacilityLayer;
    /**
     * 模型图层
     */
    private FMModelLayer mModelLayer;
    /**
     * 初始楼层
     */
    private int mGroupId = 1;
    /**
     * 点击的模型
     */
    private FMModel mClickedModel;
    /**
     * 图片图层
     */
    private FMImageLayer mImageLayer;
    /**
     * 底端消息窗
     */
    Snackbar snackbar;
    /**
     * 蓝牙适配器
     */
    public static BluetoothAdapter mBluetoothAdapter;
    /**
     * 是否正在进行扫描
     */
    public boolean isScanning = false;
    /**
     * 定时器
     */
    public Timer timer;
    /**
     * 所有扫描到的iBeacon的数据
     */
    public List<iBeacon> mIBeaconList = new ArrayList<>();
    /**
     * 定位图层
     */
    private FMLocationLayer mLocationLayer;
    /**
     * 定位标记
     */
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
     * 起点图层
     */
    protected FMImageLayer stImageLayer;
    /**
     * 终点图层
     */
    protected FMImageLayer endImageLayer;

    /**
     * 定位切换楼层
     */
    protected static final int WHAT_LOCATE_SWITCH_GROUP = 4;
    /**
     * 两个点相差最大距离20米
     */
    protected static final double MAX_BETWEEN_LENGTH = 20;
    /**
     * 进入地图显示级别
     */
    protected static final int MAP_NORMAL_LEVEL = 20;
    /**
     * 默认起点
     */
    protected MapCoord stCoord = new MapCoord(1, new FMMapCoord(12961647.576796599, 4861814.63807118));
    /**
     * 默认终点
     */
    protected MapCoord endCoord = new MapCoord(6, new FMMapCoord(12961699.79823795, 4861826.46384646));
    /**
     * 导航行走点集合
     */
    protected ArrayList<ArrayList<FMMapCoord>> mNaviPoints = new ArrayList<>();
    /**
     * 导航行走的楼层集合
     */
    protected ArrayList<Integer> mNaviGroupIds = new ArrayList<>();
    /**
     * 导航行走索引
     */
    protected int mCurrentIndex = 0;
    /**
     * 差值动画
     */
    protected FMLocationAPI mLocationAPI;

    private FMValueAnimation mMoveAnimation;

    /**
     * 行走显示详情
     */
    private static final int WHAT_WALKING_ROUTE_LINE = 3;
    /**
     * 约束过的定位标注
     */
    private FMLocationMarker mHandledMarker;
    /**
     * 上一次行走坐标
     */
    private FMMapCoord mLastMoveCoord;
    /**
     * 是否为第一人称
     */
    private boolean mIsFirstView = true;
    /**
     * 是否为跟随状态
     */
    private boolean mHasFollowed = true;
    /**
     * 总共距离
     */
    private double mTotalDistance;
    /**
     * 剩余距离
     */
    private volatile double mLeftDistance;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //动态权限申请
        if (Build.VERSION.SDK_INT < 23) {
            // Android 6.0 之前无需运行时权限申请
        } else {
            // 先检测权限   目前SDK只需2个危险权限，读和写存储卡
            int p1 = MainActivity.this.checkSelfPermission(FMMapSDK.SDK_PERMISSIONS[0]);
            int p2 = MainActivity.this.checkSelfPermission(FMMapSDK.SDK_PERMISSIONS[1]);

            int p3 = MainActivity.this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            // 只要有任一权限没通过，则申请
            if (p1 != PackageManager.PERMISSION_GRANTED || p2 != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(FMMapSDK.SDK_PERMISSIONS,                //SDK所需权限数组
                        FMMapSDK.SDK_PERMISSION_RESULT_CODE);   //SDK权限申请处理结果返回码
            } else if (p3 != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            } else {
                // 已经拥有权限了
            }
        }

        //初始化蜂鸟地图SDK
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

        // 检查该设备是否支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "你的设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 开启蓝牙
        mBluetoothAdapter.enable();

        // 初始化Toolbar控件
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 配置ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        // 点击定位Button
        btnMyLocation = (FloatingActionButton) findViewById(R.id.btn_my_location);
        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 默认无问题
                Location.isProblem = false;
                // 打开扫描器
                beginScanLocation();
            }
        });

        // 加载地图数据
        openMapByPath();
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
        switch (requestCode) {
            case FMMapSDK.SDK_PERMISSION_RESULT_CODE:
                // 申请权限被拒绝，则退出程序。
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                        grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝了必要权限，无法使用该程序！", Toast.LENGTH_SHORT).show();
                    this.finish();
                } else if (requestCode == FMMapSDK.SDK_PERMISSION_RESULT_CODE) {
                    // SDK所需权限被允许
                }
                break;
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //定位权限被允许
                } else {
                    Toast.makeText(this, "拒绝了必要权限，无法使用该程序！", Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 加载地图数据
     */
    private void openMapByPath() {
        mapView = (FMMapView) findViewById(R.id.mapview);
        mFMMap = mapView.getFMMap();//获取地图操作对象

        String bid = "10347";//地图id
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

        if (mSwitchFloorComponent == null) {
            //initSwitchFloorComponent();
            initFloorControllerComponent();
        }

        int groupId = mFMMap.getFocusGroupId();

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

        //差值动画
        mLocationAPI = new FMLocationAPI();
        mLocationAPI.setFMLocationListener(this);

        //路径规划
        //analyzeNavigation(stCoord, endCoord);
//        analyzeNavigation();
//        mTotalDistance = mNaviAnalyser.getSceneRouteLength();
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
    }

    /**
     * 切换楼层
     *
     * @param groupId 楼层id
     */
    void switchFloor(int groupId) {
        mFMMap.setFocusByGroupIdAnimated(groupId, new FMLinearInterpolator(), this);

        //切换楼层Id
        mGroupId = groupId;

        //清空标记
        mImageLayer.removeAll();

        /**
         * 切换各个图层
         */
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

        //清空标记
        mImageLayer.removeAll();

        //导航分析
        try {
            mNaviAnalyser = FMNaviAnalyser.getFMNaviAnalyserById("10347");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (FMObjectException e) {
            e.printStackTrace();
        }

        //修改定位点标注
        if (mGroupId != Location.mapCoord.getGroupId()) {
            //切换到非定位楼层，清除定位标注
            clearLocationMarker();
        } else {
            //切换到（回）定位楼层，更新定位标注
            updateLocationMarker();
        }

        Log.d("切换楼层成功", "switchFloor: ");
    }

    /**
     * 单层显示模式
     */
    void setSingleDisplay() {
        int[] gids = {mFMMap.getFocusGroupId()};       //获取当前地图焦点层id
        mFMMap.setMultiDisplay(gids, 0, this);
    }

    /**
     * 多层显示模式
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
        //isAnimateEnd = false;
    }

    /**
     * 组切换结束之后。
     */
    @Override
    public void afterGroupChanged() {
        isAnimateEnd = true;
        mHandler.sendEmptyMessage(WHAT_LOCATE_SWITCH_GROUP);
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

        if (mLocationAPI != null) {
            mLocationAPI.destroy();
        }
        mHandler.removeMessages(WHAT_LOCATE_SWITCH_GROUP);
        mHandler.removeMessages(WHAT_WALKING_ROUTE_LINE);

        //停止模拟轨迹动画
        if (mLocationAPI != null) {
            mLocationAPI.destroy();
        }
        //停止移动动画
        if (mMoveAnimation != null) {
            mMoveAnimation.stop();
        }
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
            final FMModel model = (FMModel) node;
            mClickedModel = model;

            model.setSelected(true);
            mFMMap.updateMap();
            final FMMapCoord centerMapCoord = model.getCenterMapCoord();

            //显示标记
            showMarket(centerMapCoord);

            //建立SnackBar提示用户点击的地图信息
            CoordinatorLayout mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
            String name = mClickedModel.getName();
            if (name.length() == 0) {
                name = "未命名区域";
            }

            //配置snackbar
            snackbar = Snackbar.make(mCoordinatorLayout, name, Snackbar.LENGTH_INDEFINITE);
            SnackbarUtil.setBackgroundColor(snackbar, SnackbarUtil.blue);
            SnackbarUtil.SnackbarAddView(snackbar, R.layout.snackbar, 0);
            View view = snackbar.getView();
            Button go_there = (Button) view.findViewById(R.id.go);

            //"去这里"按钮的点击事件
            go_there.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Destination.name = model.getName();
                    //去这里事件
                    goThere(centerMapCoord);
                }
            });

            //显示snackbar
            snackbar.show();
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

            //显示标记
            showMarket(centerMapCoord);

            //配置snackbar
            CoordinatorLayout mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
            snackbar = Snackbar.make(mCoordinatorLayout, "公共设施", Snackbar.LENGTH_INDEFINITE);
            SnackbarUtil.setBackgroundColor(snackbar, SnackbarUtil.blue);
            SnackbarUtil.SnackbarAddView(snackbar, R.layout.snackbar, 0);
            View view = snackbar.getView();
            Button go_there = (Button) view.findViewById(R.id.go);

            //"去这里"按钮的点击事件
            go_there.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Destination.name = "公共设施";
                    //去这里事件
                    goThere(centerMapCoord);
                }
            });
            //显示snackbar
            snackbar.show();
            return true;
        }


        @Override
        public boolean onLongPress(FMNode node) {
            return false;
        }
    };

    /**
     * 去这里事件
     * @param centerMapCoord
     */
    void goThere(FMMapCoord centerMapCoord) {
        if (Location.mapCoord.getGroupId() == 0) {
            //未进行定位
            Toast.makeText(MainActivity.this, "请先确定你的位置", Toast.LENGTH_SHORT).show();
            return;
        }

        //配置终点信息
        Destination.mapCoord =new MapCoord(mGroupId,centerMapCoord);

        //配置路线规划终点信息
        endCoord = Destination.mapCoord;

        //清除所有的线与图层
        clear();

        //添加定位标记
        locationMarker();

        //添加终点标记
        createEndImageMarker();

        //添加起点标记
        createStartImageMarker();

        //开始分析导航
        analyzeNavigation();
        mTotalDistance = mNaviAnalyser.getSceneRouteLength();

        Log.d("gohere", "goThere: "+Destination.mapCoord.getGroupId());

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

        //String content = getString(R.string.event_click_content, "地图", mGroupId, pX, pY);
        //Toast.makeText(MainActivity.this, content, Toast.LENGTH_SHORT).show();
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
        scanLeDevice(true, 2000);//扫描2s
        Thread thread = new Thread(MainActivity.this);//开启一个线程来延时
        thread.start();//启动线程
    }

    /**
     * 开始导航扫描
     */
    public void beginScanNavigate() {
        scanLeDevice(true, 0);
        timer = new Timer(true);
        timer.schedule(task, 4000, 3000); //延时4000ms后执行，1000ms执行一次
    }

    /**
     * 停止扫描
     */
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
        if (enable && time == 0){//enable =true就是说要开始扫描
            //持续扫描
            mBluetoothAdapter.startLeScan(mLeScanCallback);//这句就是开始扫描了
            isScanning = true;
        } else if (enable && time > 0) {
            // 在 time （以毫秒位单位）后，停止扫描的
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
            //停止扫描
            mBluetoothAdapter.stopLeScan(mLeScanCallback);//这句就是停止扫描
            isScanning = false;
        }
        // 这个是重置menu，将 menu中的扫描按钮->停止按钮
        invalidateOptionsMenu();
    }

    /**
     * Device scan callback.
     * 扫描回调
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
                        //将有效信息添加到List
                        mIBeaconList.add(ibeacon);
                    }
                }
            });
        }
    };

    /**
     * 定时执行
     */
    TimerTask task = new TimerTask() {
        public void run() {
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

    /**
     * 异步消息处理机制，定义处理消息的对象
     */
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
                        ProcessData(MainActivity.this, mIBeaconList);
                    }
                    //发送完数据后清空List
                    mIBeaconList.clear();
                    break;
                case 2:
                    //添加定位标记
                    locationMarker();
                    break;
                case WHAT_WALKING_ROUTE_LINE://3
                    updateWalkRouteLine((FMMapCoord) msg.obj);
                    break;
                case WHAT_LOCATE_SWITCH_GROUP://4
                    updateLocateGroupView();
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

//        Location.groupId = 1;
//        Location.myLocation.x = 1.296164E7;
//        Location.myLocation.y = 4861845.0;

        if (Location.mapCoord.getGroupId() != 0 && Location.mapCoord.getMapCoord().x != 0 &&
                Location.mapCoord.getMapCoord().y != 0) {

            Log.d(TAG, "locationMarker: " + Location.mapCoord.getGroupId());
            //清空现有
            clear();
            //切换地图
            switchFloor(Location.mapCoord.getGroupId());
            //更新楼层控制组件
            updateFloorButton(Location.mapCoord.getGroupId());
            //刷新定位点
            updateLocationMarker();

            stCoord = Location.mapCoord;
            //createStartImageMarker();
        }
        return true;
    }

    /**
     * 修改楼层控件
     */
    void updateFloorButton(int groupId) {
        String TAG = "切换楼层控件";
        Log.d(TAG, "handleMessage: GroupId" + groupId);
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

//        boolean visible = mLocationAPI.getGroupId() == mGroupId;
//        mHandledMarker.setVisible(visible);

        //FMMapCoord CENTER_COORD = new FMMapCoord(1.296164E7, 4861845.0);
        String TAG = "定位点";
        Log.d(TAG, "updateLocationMarker: begin");
        if (mLocationMarker == null) {
            Log.d(TAG, "updateLocationMarker: add");
            int groupId = mFMMap.getFocusGroupId();
            Log.d(TAG, "updateLocationMarker: groupId=" + groupId);
            mLocationMarker = new FMLocationMarker(groupId, Location.mapCoord.getMapCoord());
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
            mLocationMarker.updateAngleAndPosition(angle, Location.mapCoord.getMapCoord());
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

        int type = mNaviAnalyser.analyzeNavi(stCoord.getGroupId(), stCoord.getMapCoord(),
                endCoord.getGroupId(), endCoord.getMapCoord(),
                FMNaviAnalyser.FMNaviModule.MODULE_SHORTEST);

        if (type == FMNaviAnalyser.FMRouteCalcuResult.ROUTE_SUCCESS) {

            addLineMarker();

            fillWithPoints();

            //行走总距离
            double sceneRouteLength = mNaviAnalyser.getSceneRouteLength();
            setSceneRouteLength(sceneRouteLength);
        }
    }

    /**
     * 开始点击导航
     */
    public void startWalkingRouteLine() {

        //mLeftDistance = mTotalDistance;

        //行走索引初始为0
        mCurrentIndex = 0;
        setStartAnimationEnable(false);

        //缩放地图状态
        setZoomLevel();
        //开始进行模拟行走
        int groupId = getWillWalkingGroupId();
        setFocusGroupId(groupId);
    }

    /**
     * 切换楼层行走
     *
     * @param groupId 楼层id
     */
    protected void setFocusGroupId(int groupId) {
        if (groupId != mFMMap.getFocusGroupId()) {
            mFMMap.setFocusByGroupId(groupId, null);
            mHandler.sendEmptyMessage(WHAT_LOCATE_SWITCH_GROUP);
        }

        setupTargetLine(groupId);
    }
    /**
     * 开始模拟行走路线
     *
     * @param groupId 楼层id
     */
    protected void setupTargetLine(int groupId) {
        ArrayList<FMMapCoord> points = getWillWalkingPoints();
        mLocationAPI.setupTargetLine(points, groupId);
        mLocationAPI.start();
    }
    /**
     * 动画旋转
     */
    protected void animateRotate(final float angle) {
        if (Math.abs(mFMMap.getRotateAngle() - angle) > 2) {
            mFMMap.setRotateAngle(angle);
        }
    }
    /**
     * 移动至中心点,如果中心与屏幕中心点距离大于20米，将移动
     *
     * @param mapCoord 坐标
     */
    protected void moveToCenter(final FMMapCoord mapCoord) {
        FMMapCoord centerCoord = mFMMap.getMapCenter();
        double length = FMMath.length(centerCoord, mapCoord);
        if (length > MAX_BETWEEN_LENGTH) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mFMMap.moveToCenter(mapCoord, true);
                }
            });
        }
    }

    @Override
    public void onAnimationStart() {

    }

    @Override
    public void onAnimationUpdate(FMMapCoord mapCoord, double distance, double angle) {
        updateHandledMarker(mapCoord, angle);
        scheduleCalcWalkingRouteLine(mapCoord, distance);
    }

    @Override
    public void onAnimationEnd() {
        // 已经行走过终点
        if (isWalkComplete()) {
            setStartAnimationEnable(true);
            return;
        }

        if (isWalkComplete()) {
            setStartAnimationEnable(true);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    String info = getResources().getString(R.string.label_walk_format, 0f,
                            0, "到达目的地");
                    Log.d("Message", "info: "+info);
                    //ViewHelper.setViewText(FMNavigationApplication.this, R.id.txt_info, info);
                }
            });
            return;
        }

        int focusGroupId = getWillWalkingGroupId();
        //跳转至下一层
        setFocusGroupId(focusGroupId);
    }

    /**
     * 判断是否行走到终点
     *
     * @return
     */
    protected boolean isWalkComplete() {
        if (mCurrentIndex > mNaviGroupIds.size() - 1) {
            return true;
        }
        return false;
    }
    /**
     * 设置动画按钮是否可以使用
     *
     * @param enable true 可以执行, false 不可以执行
     */
    protected void setStartAnimationEnable(final boolean enable) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //ViewHelper.setViewEnable(BaseActivity.this, R.id.btn_start_navigation, enable);
            }
        });
    }
    /**
     * 设置缩放动画
     *
     * @return
     */
    protected void setZoomLevel() {
        if (mFMMap.getZoomLevel() != MAP_NORMAL_LEVEL) {
            mFMMap.setZoomLevel(MAP_NORMAL_LEVEL, true);
        }
    }
    /**
     * 填充导航线段点
     */
    protected void fillWithPoints() {
        clearWalkPoints();

        //获取路径规划上点集合数据
        ArrayList<FMNaviResult> results = mNaviAnalyser.getNaviResults();
        int focusGroupId = Integer.MIN_VALUE;
        for (FMNaviResult r : results) {
            int groupId = r.getGroupId();
            ArrayList<FMMapCoord> points = r.getPointList();
            //点数据小于2，则为单个数据集合
            if (points.size() < 2) {
                continue;
            }
            //判断是否为同层导航数据，非同层数据即其他层数据
            if (focusGroupId == Integer.MIN_VALUE || focusGroupId != groupId) {
                focusGroupId = groupId;
                //添加即将行走的楼层与点集合
                mNaviGroupIds.add(groupId);
                mNaviPoints.add(points);
            } else {
                mNaviPoints.get(mNaviPoints.size() - 1).addAll(points);
            }
        }
    }

    /**
     * 清空行走的点集合数据
     */
    private void clearWalkPoints() {
        mCurrentIndex = 0;
        mNaviPoints.clear();
        mNaviGroupIds.clear();
    }

    /**
     * 格式化距离
     *
     * @param sceneRouteLength 行走总距离
     */
    private void setSceneRouteLength(double sceneRouteLength) {
        int time = ConvertUtils.getTimeByWalk(sceneRouteLength);
        String text = "距离：" + (int) sceneRouteLength + "米\n" + "大约需要" + time + "分钟";

//        TextView textView = ViewHelper.getView(FMNavigationDistance.this, R.id.txt_info);
//        textView.setText(text);

        //配置snackbar+
        CoordinatorLayout mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        snackbar = Snackbar.make(mCoordinatorLayout, text, Snackbar.LENGTH_INDEFINITE);
        SnackbarUtil.setBackgroundColor(snackbar, SnackbarUtil.blue);
        SnackbarUtil.SnackbarAddView(snackbar, R.layout.snackbar, 0);
        View view = snackbar.getView();
        Button go_there = (Button) view.findViewById(R.id.go);
        go_there.setText("开始导航");

        //"开始导航"按钮的点击事件
        go_there.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "开始导航", Toast.LENGTH_SHORT).show();
                startWalkingRouteLine();
                clearLocationMarker();
                //createStartImageMarker();
            }
        });

        //显示sanckbar
        snackbar.show();
    }

    /**
     * 获取即将行走的下一层groupId
     *
     * @return
     */
    protected int getWillWalkingGroupId() {
        if (mCurrentIndex > mNaviGroupIds.size() - 1) {
            return mFMMap.getFocusGroupId();
        } else {
            return mNaviGroupIds.get(mCurrentIndex);
        }
    }

    /**
     * 获取即将行走的下一层点集合
     *
     * @return
     */
    protected ArrayList<FMMapCoord> getWillWalkingPoints() {
        if (mCurrentIndex > mNaviGroupIds.size() - 1) {
            return null;
        }
        return mNaviPoints.get(mCurrentIndex++);
    }

    /**
     * 切换楼层显示
     */
    public void updateLocateGroupView() {
        int groupSize = mFMMap.getFMMapInfo().getGroupSize();
        int position = groupSize - mFMMap.getFocusGroupId();
        mSwitchFloorComponent.setSelected(position);
    }

    /**
     * 更新处理过定位点
     *
     * @param coord 坐标
     * @param angle 角度
     */
    private void updateHandledMarker(FMMapCoord coord, double angle) {
        if (mHandledMarker == null) {
            mHandledMarker = ViewHelper.buildLocationMarker(mFMMap.getFocusGroupId(),
                    coord);
            mLocationLayer.addMarker(mHandledMarker);
        } else {
            FMMapCoord mapCoord = makeConstraint(coord);
            mHandledMarker.updateAngleAndPosition((float) angle, mapCoord);

            if (angle != 0) {
                animateRotate((float) -angle);
            }
        }

        //updateLocationMarker();

        //上次真实行走坐标
        mLastMoveCoord = coord.clone();
        moveToCenter(mLastMoveCoord);
    }

    private void isLocationMarker(int groupId){
        boolean visible = mLocationAPI.getGroupId() == groupId;
        mHandledMarker.setVisible(visible);
    }
    /**
     * 判断定位点是否应该处于屏幕中央
     */
    private void checkLocationIsCenter() {
        if (mHasFollowed || mIsFirstView) {
            //切换楼层
            int groupId = mLocationAPI.getGroupId();
            if (mFMMap.getFocusGroupId() != groupId) {
                mFMMap.setFocusByGroupId(groupId, this);
            }
        }
    }

    /**
     * 判断定位点是否为跟随状态
     */
    private void checkLocationFollowState() {
        if (mHasFollowed) {
            moveToCenter(mLastMoveCoord);
        }
    }

    /**
     * 路径约束
     *
     * @param mapCoord 地图坐标点
     * @return
     */
    private FMMapCoord makeConstraint(FMMapCoord mapCoord) {
        FMMapCoord currentCoord = mapCoord.clone();
        int groupId = mLocationAPI.getGroupId();
        //获取当层绘制路径线点集合
        ArrayList<FMMapCoord> coords = mLocationAPI.getSimulateCoords();
        //路径约束
        mNaviAnalyser.naviConstraint(groupId, coords, mLastMoveCoord, currentCoord);
        return currentCoord;
    }

    /**
     * 设置是否为第一人称
     *
     * @param enable true 第一人称
     *               false 第三人称
     */
    private void setViewState(boolean enable) {
        this.mIsFirstView = !enable;
        setFloorControlEnable();
    }

    /**
     * 设置跟随状态
     *
     * @param enable true 跟随
     *               false 不跟随
     */
    private void setFollowState(boolean enable) {
        mHasFollowed = enable;
        setFloorControlEnable();
    }

    /**
     * 设置楼层控件是否可用
     */
    private void setFloorControlEnable() {
        if (getFloorControlEnable()) {
            mSwitchFloorComponent.close();
            mSwitchFloorComponent.setEnabled(false);
        } else {
            mSwitchFloorComponent.setEnabled(true);
        }
    }

    /**
     * 楼层控件是否可以使用
     *
     * @return
     */
    private boolean getFloorControlEnable() {
        return mHasFollowed || mIsFirstView;
    }

    /**
     * 计算行走距离
     *
     * @param mapCoord 定位点
     * @param distance 已行走距离
     */
    private void scheduleCalcWalkingRouteLine(FMMapCoord mapCoord, double distance) {
        mLeftDistance -= distance;
        if (mLeftDistance <= 0) {
            mLeftDistance = 0;
        }

        Message message = Message.obtain();
        message.what = WHAT_WALKING_ROUTE_LINE;
        message.obj = mapCoord;
        mHandler.sendMessage(message);
    }

    /**
     * 更新行走距离和文字导航
     *
     * @param mapCoord 定位点坐标
     */
    private void updateWalkRouteLine(FMMapCoord mapCoord) {
        int timeByWalk = ConvertUtils.getTimeByWalk(mLeftDistance);
        String description = ConvertUtils.getDescription(mNaviAnalyser.getNaviDescriptionData(),
                mapCoord.clone(), mLocationAPI.getGroupId());
        String info = getResources().getString(R.string.label_walk_format, mLeftDistance,
                timeByWalk, description);
        Log.d("Message", "info: "+info);
//        ViewHelper.setViewText(FMNavigationApplication.this, R.id.txt_info, info);

//        if(descriptionChanged(description)&&!mKqwSpeechCompound.isSpeaking()){
//            mKqwSpeechCompound.speaking(description);
//        }
    }

//    private boolean descriptionChanged(String description) {
//        if(test!=description){
//            test=description;
//            return true;
//        }
//        return false;
//    }

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
     * 添加线标注
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
     * 创建起点
     */
    protected void createStartImageMarker() {
        clearStartImageLayer();
        // 添加起点图层
        stImageLayer = new FMImageLayer(mFMMap, stCoord.getGroupId());
        mFMMap.addLayer(stImageLayer);
        // 标注物样式
        FMImageMarker imageMarker = ViewHelper.buildImageMarker(getResources(), stCoord.getMapCoord(), R.drawable.start);
        stImageLayer.addMarker(imageMarker);
    }

    protected void addStartImageMaeker(){
        FMImageMarker imageMarker = ViewHelper.buildImageMarker(getResources(), stCoord.getMapCoord(), R.drawable.start);
        stImageLayer.addMarker(imageMarker);
    }

    /**
     * 创建终点图层
     */
    protected void createEndImageMarker() {
        clearEndImageLayer();
        // 添加起点图层
        endImageLayer = new FMImageLayer(mFMMap, endCoord.getGroupId());
        mFMMap.addLayer(endImageLayer);
        mImageLayer.removeAll();
        // 标注物样式
        FMImageMarker imageMarker = ViewHelper.buildImageMarker(getResources(), endCoord.getMapCoord(), R.drawable.end);
        endImageLayer.addMarker(imageMarker);
    }

    /**
     * 延时函数
     */
    @Override
    public void run() {
        try {
            Thread.sleep(4000);//睡一段时间
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        handler.sendEmptyMessage(1);//睡醒来了，传送消息，扫描完成
    }

    /**
     * 处理、保存、上传扫描信息
     *
     * @author jin
     * Data:2017/7/17
     */
    public void ProcessData(Context contextMain, List<iBeacon> mIBeaconList) {

        int count = 0;//记录扫描到iBeacon的数量

        Location.isOk = false;

        //扫描到几个iBeacon（MAC地址不同），就置入几个
        // iBeacons类继承于iBeacon类，保存了一个iBeacon在一段时间内所有的RSSI信息
        List<iBeacons> iBeaconsList = new ArrayList<>();

        //所有扫描到的iBeacon信号的MAC地址都置入该List，扫描到几次就存入几次
        List<String> iBeaconMacList = new ArrayList<>();

        //处理后的iBeacon信息，一个iBeacon对应一条数据
        List<iBeacon> answerIBeaconList = new ArrayList<>();

        if (mIBeaconList.size() == 0) {
            Toast.makeText(contextMain, "您当前环境暂不支持室内定位", Toast.LENGTH_SHORT).show();
            Location.isProblem = true;
            return;
        }

        //遍历所有接收到的信号信息
        for (iBeacon item : mIBeaconList) {
            //Log.d(TAG, "run: item mac:"+item.getBluetoothAddress());
            //统计iBeacon个数
            if (iBeaconMacList.indexOf(item.getBluetoothAddress()) == -1) {
                //首次在该位置接收到某iBeacon信号
                count++;
                iBeaconMacList.add(item.getBluetoothAddress());
                iBeacons miBeacons=new iBeacons(item);
                miBeacons.rssiList.add(String.valueOf(item.getRssi()));
                iBeaconsList.add(miBeacons);
            } else {//非首次在该位置接收到某iBeacon信号
                //遍历iBeacons类的List
                for (int i = 0; i < iBeaconsList.size(); i++) {
                    //找到已存在的对象
                    if (item.getBluetoothAddress().equals(iBeaconsList.get(i).getBluetoothAddress())) {
                        //向已存在的对象中添加RSSI数据
                        iBeaconsList.get(i).rssiList.add(String.valueOf(item.getRssi()));
                    }
                }
            }
        }
        //Log.d(TAG, "run: count="+count);
        //Log.d(TAG, "run: iBeaconsList size="+iBeaconsList.size());

        //处理iBeacons类，将处理结果置入answerIBeaconList
        for (iBeacons ibeacons : iBeaconsList) {
            answerIBeaconList.add(new iBeacon(ibeacons, Integer.valueOf(FitRssi.FitRssiData(ibeacons.getRssiList()))));
        }

        try {
            //这里Place先不存数据库，直接上传
            //存数据库发生了IBeaconList丢失的情况，
            // 在数据库取出的Place中再次获取IBeaconList的时候返回了null
            SendDatebase(contextMain, answerIBeaconList);
        } catch (Exception e) {
            //Toast.makeText(RecordActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    /**
     * 将数据上传至服务器后台
     *
     * @param place
     * @author fang
     * Data:2017/7/16
     */

    protected static final String IP = "192.168.1.110";
    protected static final String URL = "http://" + IP + ":80/locate";

    public void SendDatebase(final Context contextMain, final List<iBeacon> iBeaconList) {

        final String TAG = "SendDate";
        Log.d(TAG, "SendDatebase:" + iBeaconListToString(iBeaconList));

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //执行耗时操作
                try {
                    Log.d(TAG, "run: " + URL);
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new FormBody.Builder()
                            .add("recordData", iBeaconListToString(iBeaconList))
                            .build();
                    Request request = new Request.Builder()
                            .url(URL)
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    Log.d("SendDatabase", "run: fuuuuuuuuuck   " + response);
                    if (response.isSuccessful()) {
                        //广播上传成功消息
                        //Toast.makeText(contextMain,"Success!",Toast.LENGTH_SHORT).show();//广播上传成功
                        Log.d(TAG, "run: SendDate Success!!!!!!!" + response);
                        Log.d(TAG, "run: Response Message:" + response.header("location"));
                        //解析数据失败
                        if (dealWithResponse(response.header("location")) == true) {
                            Location.isOk = true;
                            Toast.makeText(contextMain, "定位成功", Toast.LENGTH_SHORT).show();
                            handler.sendEmptyMessage(2);
                        } else {
                            Toast.makeText(contextMain, "您当前环境暂不支持定位", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(contextMain, "上传失败，数据格式错误", Toast.LENGTH_LONG).show();
                        Location.isProblem = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(contextMain, "无法连接到服务器，请检查网络设置", Toast.LENGTH_LONG).show();
                    Location.isProblem = true;
                    //Log.d("SendDatabase Fail ", e.getMessage());
                }
            }
        };
        new Thread() {
            public void run() {
                Looper.prepare();
                new Handler().post(runnable);//在子线程中直接去new 一个handler
                Looper.loop();//这种情况下，Runnable对象是运行在子线程中的，可以进行联网操作，但是不能更新UI
            }
        }.start();

    }

    /**
     * 解析后台返回的定位位置信息
     *
     * @param message
     * @return
     */
    private boolean dealWithResponse(String message) {

        String TAG = "Location";
        String floor = "0";
        String x = "0";
        String y = "0";
        try {
            if (message != null) {
                //解析出有效信息
                int maohao = message.indexOf(':');
                int xiegang = message.indexOf('/');

                floor = message.substring(maohao + 1, xiegang);

                maohao = message.indexOf(':', maohao + 1);
                xiegang = message.indexOf('/', xiegang + 1);

                x = message.substring(maohao + 1, xiegang);

                maohao = message.indexOf(':', maohao + 1);

                y = message.substring(maohao + 1);
            } else {
                return false;
            }

            Location.mapCoord.setGroupId(Integer.valueOf(floor));
            Location.mapCoord.setMapCoord(new FMMapCoord(Double.valueOf(x),Double.valueOf(y)));

            Log.d(TAG, "dealWithResponse: groupId:" + Location.mapCoord.getGroupId());
            Log.d(TAG, "dealWithResponse: x:" + x);
            Log.d(TAG, "dealWithResponse: y:" + y);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * 格式化iBeaconList的信息
     * @param iBeaconList
     * @return
     */
    private String iBeaconListToString(List<iBeacon> iBeaconList) {
        String answer = "";
        for (iBeacon ibeacon : iBeaconList) {
            answer += ibeacon.toString();
        }
        return answer;
    }
}
