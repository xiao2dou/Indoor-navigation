package com.example.jin.hellofengmap.location;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jin on 2017/7/19.
 *
 * 滤波算法
 * 处理一段时间内某个iBeacon基站在某点的RSSI值
 * 使用了数理统计知识——标准正态分布
 */

public class FitRssi {

    private static final String TAG="FitRssi";

    public static String FitRssiData(List<String> rssiList){
        String answer="1";

        List<Integer> fitRssiList=new ArrayList<>();

        double confidence=0.5;
        double average=0;
        double variance=0;
        for (String string:rssiList) {
            average+=Integer.valueOf(string);
        }
        average/=rssiList.size();
        //System.out.println("average"+average);
        for (String string:rssiList) {
            variance+=Math.pow(Integer.valueOf(string)-average, 2);
        }
        variance/=rssiList.size();
        //System.out.println("variance:"+variance);
        double normal=0;
        for (String string:rssiList) {
            normal=(Integer.valueOf(string)-average)/variance;
            if (normal>-confidence&&normal<confidence) {
                fitRssiList.add(Integer.valueOf(string));
            }
            normal=0;
        }

        int averageRssi=0;
        for (Integer i:fitRssiList){
            averageRssi+=i;//求平均值
        }
        if (rssiList.size()!=0){
            answer=String.valueOf(averageRssi/fitRssiList.size());
        }

        Log.d(TAG, "FitRssiData: 处理前:"+rssiList);
        Log.d(TAG, "FitRssiData: 处理后:"+fitRssiList);

        return answer;
    }
}
