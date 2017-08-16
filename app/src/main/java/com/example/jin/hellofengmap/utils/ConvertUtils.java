package com.example.jin.hellofengmap.utils;

import com.fengmap.android.analysis.navi.FMNaviDescriptionData;
import com.fengmap.android.analysis.navi.FMNaviResult;
import com.fengmap.android.map.FMGroupInfo;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapInfo;
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
     * 获取楼层名称
     *
     * @param map     地图
     * @param groupId 楼层id
     * @return
     */
    public static String convertToFloorName(FMMap map, int groupId) {
        String floorName = "F1";
        FMMapInfo mapInfo = map.getFMMapInfo();
        int groupSize = mapInfo.getGroups().size();
        for (int i = 0; i < groupSize; i++) {
            FMGroupInfo groupInfo = mapInfo.getGroups().get(i);
            int id = groupInfo.getGroupId();
            if (id == groupId) {
                return groupInfo.getGroupName().toUpperCase();
            }
        }
        return floorName;
    }

    /**
     * 绝对方向
     *
     * @param vector 坐标
     */
    public static float getAbsoluteDirectionWithVector(FMMapCoord vector) {
        FMMapCoord northVector = new FMMapCoord(0, 1);
        double angle = calTwoVectorIncludedAngleWithVector(vector, northVector);
        double direction = getTwoVectorMultiplicationCrossWithVector(vector, northVector);
        //向左为正  向右为负
        if (direction < 0) {
            angle = -angle;
        }
        return (float) angle;
    }

    /**
     * 获取两个向量的叉乘
     *
     * @param vector1 坐标
     * @param vector2 坐标
     */
    public static double getTwoVectorMultiplicationCrossWithVector(FMMapCoord vector1, FMMapCoord vector2) {
        return (vector1.x * vector2.y - vector2.x * vector1.y);
    }

    /**
     * 计算两个向量的夹角
     *
     * @param vector1 坐标
     * @param vector2 坐标
     */
    public static double calTwoVectorIncludedAngleWithVector(FMMapCoord vector1, FMMapCoord vector2) {
        double mapPoint1Length = getLength(vector1);
        double mapPoint2Length = getLength(vector2);
        double product = getTwoVectorProduct(vector1, vector2);
        double cosrad = product / (mapPoint1Length * mapPoint2Length);
        double angle = Math.acos(cosrad) * 180.0 / Math.PI;
        if (angle >= 0) {
            return angle;
        }
        return 0;
    }

    /**
     * 获取两个向量的点积
     *
     * @param vector1 坐标
     * @param vector2 坐标
     */
    public static double getTwoVectorProduct(FMMapCoord vector1, FMMapCoord vector2) {
        return (vector1.x * vector2.x + vector1.y * vector2.y);
    }


    /**
     * 获取向量的长度
     *
     * @param mapPoint 坐标
     */
    public static double getLength(FMMapCoord mapPoint) {
        return Math.sqrt(Math.pow(mapPoint.x, 2) + Math.pow(mapPoint.y, 2));
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

    /**
     * 获取地图旋转方向
     *
     * @param curPoint  当前点坐标
     * @param lastPoint 上个点坐标
     * @return
     */
    public static float getAbsoluteDirection(FMMapCoord curPoint, FMMapCoord lastPoint) {
        if (lastPoint == null) {
            return 0;
        }
        FMMapCoord vector = new FMMapCoord(curPoint.x - lastPoint.x, curPoint.y - lastPoint.y);
        float angle = getAbsoluteDirectionWithVector(vector);
        return angle;
    }

    /**
     * 获取导航描述
     *
     * @param datas        导航描述数据
     * @param mapCoord     定位点
     * @param focusGroupId 当前楼层id
     * @return
     */
    public static String getDescription(ArrayList<FMNaviDescriptionData> datas, FMMapCoord mapCoord,
                                        int focusGroupId) {
        String result = "";
        int index = 0;
        double minLength = Double.MAX_VALUE;
        for (int i = 0; i < datas.size(); i++) {
            FMNaviDescriptionData data = datas.get(i);
            if (focusGroupId != data.getStartGroupId()) {
                continue;
            }

            FMMapCoord start = data.getStartCoord();
            FMMapCoord end = data.getEndCoord();
            double len = pointToLine(start, end, mapCoord);
            if (len == 0) {
                index = i;
                break;
            } else if (len < minLength) {
                minLength = len;
                index = i;
            }
        }

        if (datas.isEmpty()) {
            return result;
        }

        FMNaviDescriptionData data = datas.get(index);
        double distance = ((int) (data.getDistance() * 100)) / 100d;
        String endDirection = data.getEndDirection();
        result = "直行" + distance + "米 " + endDirection;
        return result;
    }

    /**
     * 点到线段距离
     *
     * @param startCoord 起点
     * @param endCoord   终点
     * @param otherCoord 判断的点坐标
     * @return
     */
    public static double pointToLine(FMMapCoord startCoord, FMMapCoord endCoord, FMMapCoord otherCoord) {
        double space = 0;
        double len, len1, len2;
        len = FMMath.length(startCoord, endCoord);// 线段的长度
        len1 = FMMath.length(startCoord, otherCoord);// (x1,y1)到点的距离
        len2 = FMMath.length(endCoord, otherCoord);// (x2,y2)到点的距离

        if (len2 + len1 - len <= 0.000001) {//点在线段上
            space = 0;
            return space;
        }

        if (len <= 0.000001) {//不是线段，是一个点
            space = len1;
            return space;
        }

        if (len2 * len2 >= len * len + len1 * len1) { //组成直角三角形或钝角三角形，(x1,y1)为直角或钝角
            space = len1;
            return space;
        }

        if (len1 * len1 >= len * len + len2 * len2) {//组成直角三角形或钝角三角形，(x2,y2)为直角或钝角
            space = len2;
            return space;
        }

        //组成锐角三角形，则求三角形的高
        double p = (len + len1 + len2) / 2;// 半周长
        double s = Math.sqrt(p * (p - len) * (p - len1) * (p - len2));// 海伦公式求面积
        space = 2 * s / len;// 返回点到线的距离（利用三角形面积公式求高）
        return space;
    }
}

