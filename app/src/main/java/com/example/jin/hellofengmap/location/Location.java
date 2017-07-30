package com.example.jin.hellofengmap.location;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.jin.hellofengmap.MainActivity;
import com.fengmap.android.map.geometry.FMMapCoord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jin on 2017/7/28.
 */

public class Location {

    protected FMMapCoord myLocation;

    public static boolean isProblem = false;

    /**
     * 处理、保存、上传扫描信息
     *
     * @author jin
     * Data:2017/7/17
     */
    public static void ProcessData(Context contextMain, List<iBeacon> mIBeaconList) {

        int count = 0;//记录扫描到iBeacon的数量

        //扫描到几个iBeacon（MAC地址不同），就置入几个
        // iBeacons类继承于iBeacon类，保存了一个iBeacon在一段时间内所有的RSSI信息
        List<iBeacons> iBeaconsList = new ArrayList<>();

        //所有扫描到的iBeacon信号的MAC地址都置入该List，扫描到几次就存入几次
        List<String> iBeaconMacList = new ArrayList<>();

        //处理后的iBeacon信息，一个iBeacon对应一条数据
        List<iBeacon> answerIBeaconList = new ArrayList<>();

        if (mIBeaconList.size() == 0) {
            Toast.makeText(contextMain, "您当前环境暂不支持室内定位", Toast.LENGTH_SHORT).show();
            isProblem = true;
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
                iBeaconsList.add(new iBeacons(item));
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
//    protected static final String IP = "192.168.1.106";
//    protected static final String URL = "http://" + IP + ":80/locate";

    protected static final String IP = "192.168.1.106";
    protected static final String URL = "http://" + IP + ":80/locate";

    public static void SendDatebase(final Context contextMain, final List<iBeacon> iBeaconList) {

        final String TAG = "SendDate";

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //执行耗时操作
                try {
                    Log.d(TAG, "run: "+URL);
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
                    if (response.isSuccessful()){
                        //广播上传成功消息
                        //Toast.makeText(contextMain,"Success!",Toast.LENGTH_SHORT).show();//广播上传成功
                        Log.d(TAG, "run: SendDate Success!!!!!!!");
                        Log.d(TAG, "run: Response Message:"+response.header("place"));
                    }else{
                        Toast.makeText(contextMain,"上传失败，数据格式错误",Toast.LENGTH_LONG).show();
                        isProblem=true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(contextMain,"无法连接到服务器，请检查网络设置",Toast.LENGTH_LONG).show();
                    isProblem=true;
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

    private static String iBeaconListToString(List<iBeacon> iBeaconList) {
        String answer = "";
        for (iBeacon ibeacon : iBeaconList) {
            answer += ibeacon.toString();
        }
        return answer;
    }

    public FMMapCoord getMyLocation() {
        return myLocation;
    }

    public void setMyLocation(FMMapCoord myLocation) {
        this.myLocation = myLocation;
    }

}
