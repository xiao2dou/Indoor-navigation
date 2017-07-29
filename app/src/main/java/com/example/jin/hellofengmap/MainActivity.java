package com.example.jin.hellofengmap;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.jin.hellofengmap.location.Location;
import com.example.jin.hellofengmap.location.iBeacon;
import com.example.jin.hellofengmap.location.iBeaconClass;
import com.example.jin.hellofengmap.utils.ViewHelper;
import com.fengmap.android.FMDevice;
import com.fengmap.android.FMErrorMsg;
import com.fengmap.android.FMMapSDK;
import com.fengmap.android.data.OnFMDownloadProgressListener;
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
import com.fengmap.android.map.layer.FMModelLayer;
import com.fengmap.android.map.marker.FMFacility;
import com.fengmap.android.map.marker.FMImageMarker;
import com.fengmap.android.map.marker.FMModel;
import com.fengmap.android.map.marker.FMNode;
import com.fengmap.android.widget.FM3DControllerButton;
import com.fengmap.android.widget.FMFloorControllerComponent;
import com.fengmap.android.widget.FMZoomComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements OnFMMapInitListener,
        OnFMCompassListener,OnFMSwitchGroupListener,OnFMMapClickListener{

    FMMap mFMMap;
    FMMapView mapView;
    FloatingActionButton btnMyLocation;//定位按钮

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

    public static BluetoothAdapter mBluetoothAdapter;//蓝牙
    public boolean isScanning=false;
    public Timer timer;//定时器
    public List<iBeacon> mIBeaconList=new ArrayList<>();//所有扫描到的数据

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
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                    Toast.makeText(this, "定位需要开启蓝牙", Toast.LENGTH_SHORT).show();
                }
            }


            // 先检测权限   目前SDK只需2个危险权限，读和写存储卡
            int p1 = MainActivity.this.checkSelfPermission(FMMapSDK.SDK_PERMISSIONS[0]);
            int p2 = MainActivity.this.checkSelfPermission(FMMapSDK.SDK_PERMISSIONS[1]);
            // 只要有任一权限没通过，则申请
            if (p1 != PackageManager.PERMISSION_GRANTED || p2 != PackageManager.PERMISSION_GRANTED ) {
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

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        //定位
        btnMyLocation=(FloatingActionButton)findViewById(R.id.btn_my_location);
        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScanning==false){
                    beginScan();
                }
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

        String bid = "10380";             //地图id
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
        Toast.makeText(this,"加载地图失败，请检查网络设置",Toast.LENGTH_SHORT).show();
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
        //清空标记
        mImageLayer.removeAll();
        //切换图层
        mGroupId=groupId;
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

    /**
     * 处理权限申请结果
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 申请权限被拒绝，则退出程序。
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                grantResults[1] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"拒绝了必要权限，无法使用该程序！",Toast.LENGTH_SHORT).show();
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
            if(mClickedModel!=null){
                mClickedModel.setSelected(false);
            }
            FMModel model = (FMModel) node;
            mClickedModel = model;

            model.setSelected(true);
            mFMMap.updateMap();
            FMMapCoord centerMapCoord = model.getCenterMapCoord();

            showMarket(centerMapCoord);

            String content = getString(R.string.event_click_content, "模型:"+mClickedModel.getName(), mGroupId, centerMapCoord.x, centerMapCoord.y);
            Toast.makeText(MainActivity.this,content,Toast.LENGTH_SHORT).show();
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
            FMMapCoord centerMapCoord = facility.getPosition();

            showMarket(centerMapCoord);

            String content = getString(R.string.event_click_content, "公共设施", mGroupId, centerMapCoord.x, centerMapCoord.y);
            Toast.makeText(MainActivity.this,content,Toast.LENGTH_SHORT).show();
            //ViewHelper.setViewText(MainActivity.this, R.id.map_result, content);
            return true;
        }

        @Override
        public boolean onLongPress(FMNode node) {
            return false;
        }
    };

    /**
     * 地图点击事件
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

//        String content = getString(R.string.event_click_content, "地图", mGroupId, pX, pY);
//        Toast.makeText(MainActivity.this,content,Toast.LENGTH_SHORT).show();
        //ViewHelper.setViewText(MainActivity.this, R.id.map_result, content);
    }

    /**
     * 显示标记
     * @param centerMapCoord 标记位置
     * @author jin
     */
    public void showMarket(FMMapCoord centerMapCoord){
        if (centerMapCoord!=null&&mImageLayer==null){
            //添加图片标注
            FMImageMarker imageMarker = ViewHelper.buildImageMarker(getResources(),
                    centerMapCoord, R.drawable.ic_marker_blue);
            mImageLayer.addMarker(imageMarker);
        }else if (centerMapCoord!=null&&mImageLayer!=null){
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

    private void beginScan(){
        scanLeDevice(true);
        timer = new Timer(true);
        timer.schedule(task,0, 1000); //延时0ms后执行，1000ms执行一次
    }

    private void stopScan(){
        scanLeDevice(false);
        timer.cancel();
    }

    /**
     * 扫描
     * 新开线程
     * @param enable
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevice(final boolean enable)
    {
        if (enable)//enable =true就是说要开始扫描
        {
            // Stops scanning after a pre-defined scan period.
            // 下边的代码是为了在 SCAN_PERIOD （以毫秒位单位）后，停止扫描的
            // 如果需要不停的扫描，可以注释掉
//            mHandler.postDelayed(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                    // 这个是重置menu，将 menu中的停止按钮->扫描按钮
//                    invalidateOptionsMenu();
//                }
//            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);//这句就是开始扫描了
            isScanning=true;
        }
        else
        {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);//这句就是停止扫描
            isScanning=false;
        }
        // 这个是重置menu，将 menu中的扫描按钮->停止按钮
        invalidateOptionsMenu();
    }

    /**
     * Device scan callback.
     * 回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord)
        {
            final iBeacon ibeacon = iBeaconClass.fromScanData(device,rssi,scanRecord);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(ibeacon != null&&ibeacon.getMinor()!=0) {//扫描到有效信息
                        Log.d("Scan iBeacon", "run: "+ibeacon.getBluetoothAddress());
                        mIBeaconList.add(ibeacon);
                    }
                }
            });
        }
    };


    //定时执行
    TimerTask task = new TimerTask(){
        public void run() {
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

    //异步消息处理机制
    //定义处理消息的对象
    public Handler handler = new Handler(){
        /**
         * 处理消息
         * @param msg
         */
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    break;
                case 1://1s到，处理、发送数据
                    Location.ProcessData(MainActivity.this, mIBeaconList);
                    break;
                default:
                    break;
            }
        }
    };
}
