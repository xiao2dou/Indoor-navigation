package com.example.jin.hellofengmap.utils;

import com.fengmap.android.analysis.navi.FMNaviResult;
import com.fengmap.android.map.geometry.FMMapCoord;
import com.fengmap.android.utils.FMMath;

import java.util.ArrayList;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 地图信息工具类
 */
public class ConvertUtils {

    /**
     * 获取当前层的行走距离
     *
     * @param results      结果集合
     * @param focusGroupId 当层id
     * @return
     */
    public static double getDistance(ArrayList<FMNaviResult> results, int focusGroupId) {
        double distance = 0;
        for (FMNaviResult r : results) {
            int groupId = r.getGroupId();
            if (groupId == focusGroupId) {
                int size = r.getPointList().size();
                if (size > 1) {
                    distance = getDistance(r.getPointList());
                    break;
                }
            }
        }
        return distance;
    }

    /**
     * 获取点的集合的数据
     *
     * @param points 点集合
     * @return
     */
    public static double getDistance(ArrayList<FMMapCoord> points) {
        double distance = 0;
        int size = points.size();
        for (int i = 0; i < size - 1; i++) {
            distance += FMMath.length(points.get(i), points.get(i + 1));
        }
        return distance;
    }

    /**
     * 获取当前层的点集合
     *
     * @param results      结果集合
     * @param focusGroupId 当层id
     * @return
     */
    public static ArrayList<FMMapCoord> getMapCoords(ArrayList<FMNaviResult> results, int focusGroupId) {
        ArrayList<FMMapCoord> pointList = new ArrayList<>();
        for (FMNaviResult r : results) {
            int groupId = r.getGroupId();
            if (groupId == focusGroupId) {
                int size = r.getPointList().size();
                if (size > 1) {
                    pointList.addAll(r.getPointList());
                }
            }
        }
        return pointList;
    }


    /**
     * 行走时间
     *
     * @param distance 距离
     * @return
     */
    public static int getTimeByWalk(double distance) {
        if (distance == 0) {
            return 0;
        }
        int time = (int) Math.ceil(distance / 80);
        if (time < 1) {
            time = 1;
        }
        return time;
    }


}
