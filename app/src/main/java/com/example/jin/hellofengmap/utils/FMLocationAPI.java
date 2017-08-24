package com.example.jin.hellofengmap.utils;

import com.fengmap.android.map.geometry.FMMapCoord;
import com.fengmap.android.utils.FMMath;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 模拟数据点
 */
public class FMLocationAPI {
    private OnFMLocationListener mListener;
    private ArrayList<FMMapCoord> mCoordList;   //插值后的点和角度集合
    private double mTotalDistance;
    private volatile int mIndex;
    private double mWalkStep;
    private Timer mTimer;
    private TimerTask mTask;
    private long mFrameTime = 300;//80
    private long mDelayTime = 50;
    private int mGroupId = -1;

    public FMLocationAPI() {
        this.mTimer = new Timer();
        this.mCoordList = new ArrayList<FMMapCoord>();
    }

    public static ArrayList<FMMapCoord> cloneCollections(ArrayList<FMMapCoord> points) {
        ArrayList<FMMapCoord> list = new ArrayList<FMMapCoord>();
        for (FMMapCoord mapCoord : points) {
            list.add(mapCoord.clone());
        }
        return list;
    }

    public void setupTargetLine(ArrayList<FMMapCoord> points, int groupId) {
        float verticalDist = 0;
        setupTargetLine(points, groupId, verticalDist);
    }

    public void setupTargetLine(ArrayList<FMMapCoord> points, int groupId, float verticalDist) {
        this.mIndex = 0;
        this.mGroupId = groupId;
        this.mCoordList.clear();
        this.mTotalDistance = ConvertUtils.getDistance(points);
        //添加模拟坐标点
        ArrayList<FMMapCoord> clones = cloneCollections(points);
        FMMath.makeBezierSmooth(clones, 3);
        mCoordList.addAll(makeSimulatePoints(clones, verticalDist));

        //平均行走距离
        mWalkStep = mTotalDistance / mCoordList.size();
    }

    /**
     * 添加模拟坐标点
     *
     * @param point 坐标点
     */
    public void addSimulatePoint(FMMapCoord point) {
        int index = compareSimulatePoint(point);
        int count = 20;
        for (int i = 0; i < count; i++) {
            FMMapCoord coord = new FMMapCoord(point.x + 1 * i, point.y);
            mCoordList.add(index + i, coord);
        }
    }

    /**
     * 比对模拟点
     *
     * @param origin 要插入的模拟点坐标
     * @return
     */
    private int compareSimulatePoint(FMMapCoord origin) {
        for (int i = 0; i < mCoordList.size(); i++) {
            FMMapCoord coord = mCoordList.get(i);
            double offset = origin.x - coord.x;
            if (offset >= 0 && offset <= 2) {
                return i;
            }
        }
        return 0;
    }

    public void setFMLocationListener(OnFMLocationListener pListener) {
        this.mListener = pListener;
    }

    public void setFrameTime(long time) {
        this.mFrameTime = time;
    }

    public int getGroupId() {
        return mGroupId;
    }

    /**
     * 开始执行动画
     */
    public void start() {
        this.mIndex = 0;
        if (mListener != null) {
            mListener.onAnimationStart();
        }
        final double[] angles = FMMath.getAnglesBetweenCoords(mCoordList);
        //开始执行刷新动画
        mTask = new TimerTask() {
            @Override
            public void run() {
                if (mIndex > mCoordList.size() - 1) {
                    mTask.cancel();
                    mTimer.purge();

                    if (mListener != null) {
                        mListener.onAnimationEnd();
                    }
                    return;
                }
                //行走多少距离
                double distance = mWalkStep * mIndex;
                if (mIndex == mCoordList.size() - 1) {
                    distance = mTotalDistance;
                }

                if (mListener != null) {
                    mListener.onAnimationUpdate(mCoordList.get(mIndex), mWalkStep, angles[mIndex]);
                }

                mIndex++;
            }
        };

        mTimer.schedule(mTask, mDelayTime, mFrameTime);
    }

    /**
     * 生成模拟数据
     *
     * @param points 点集合
     * @return
     */
    private ArrayList<FMMapCoord> makeSimulatePoints(ArrayList<FMMapCoord> points) {
        float verticalDist = 2.0f;
        return makeSimulatePoints(points, verticalDist);
    }

    /**
     * 生成模拟数据
     *
     * @param points       点集合
     * @param verticalDist 垂直偏移
     * @return
     */
    private ArrayList<FMMapCoord> makeSimulatePoints(ArrayList<FMMapCoord> points, float verticalDist) {
        float speed = 0.5f;
        return CalcSimulate.calcSimulateLocationPoints(points, speed, verticalDist);
    }

    public ArrayList<FMMapCoord> getSimulateCoords() {
        return mCoordList;
    }

    /**
     * 暂停
     */

    public void stop() {
        if (mTask != null) {
            mTask.cancel();
            mTimer.purge();
        }
    }

    public void setDelayTime(long delayTime) {
        this.mDelayTime = delayTime;
    }


    /**
     * 销毁
     */
    public void destroy() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mCoordList != null) {
            mCoordList.clear();
        }
        mListener = null;
        mTask = null;
    }


    public double getTotalDistance() {
        return mTotalDistance;
    }

    /**
     * 动画回调监听
     */
    public interface OnFMLocationListener {
        /**
         * 模拟动画开始
         */
        void onAnimationStart();

        /**
         * 模拟动画更新
         *
         * @param mapCoord 当前点坐标
         * @param distance 行走距离
         * @param angle    当前点角度
         */
        void onAnimationUpdate(FMMapCoord mapCoord, double distance, double angle);

        /**
         * 模拟动画结束
         */
        void onAnimationEnd();
    }

}
