package com.example.jin.hellofengmap.utils;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.example.jin.hellofengmap.widget.ImageViewCheckBox;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.geometry.FMMapCoord;
import com.fengmap.android.map.marker.FMImageMarker;
import com.fengmap.android.map.marker.FMLineMarker;
import com.fengmap.android.map.marker.FMLocationMarker;
import com.fengmap.android.map.marker.FMSegment;
import com.fengmap.android.map.marker.FMTextMarker;

import java.util.ArrayList;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 控件使用帮助类
 */
public class ViewHelper {

    /**
     * 获取控件
     *
     * @param activity Activity
     * @param id       控件id
     * @param <T>
     * @return
     */
    public static <T extends View> T getView(Activity activity, int id) {
        return (T) activity.findViewById(id);
    }

    /**
     * 获取控件
     *
     * @param view 视图
     * @param id   控件id
     * @param <T>
     * @return
     */
    public static <T extends View> T getView(View view, int id) {
        return (T) view.findViewById(id);
    }

    /**
     * 设置控件的点击事件
     *
     * @param activity Activity
     * @param id       控件id
     * @param listener 点击监听事件
     */
    public static void setViewClickListener(Activity activity, int id, View.OnClickListener listener) {
        View view = getView(activity, id);
        view.setOnClickListener(listener);
    }

    /**
     * 设置控件文字
     *
     * @param activity Activity
     * @param id       控件id
     * @param text     文字
     */
    public static void setViewText(Activity activity, int id, String text) {
        TextView view = getView(activity, id);
        view.setText(text);
    }

    /**
     * 设置控件是否可用
     *
     * @param activity Activity
     * @param id       控件id
     * @param enabled  true 可以使用 false 不可使用
     */
    public static void setViewEnable(Activity activity, int id, boolean enabled) {
        View view = getView(activity, id);
        view.setEnabled(enabled);
    }

    /**
     * 设置控件的状态改变事件
     *
     * @param activity Activity
     * @param id       控件id
     * @param listener ImageView状态改变事件
     */
    public static void setViewCheckedChangeListener(Activity activity, int id,
                                                    ImageViewCheckBox.OnCheckStateChangedListener listener) {
        ImageViewCheckBox view = getView(activity, id);
        view.setOnCheckStateChangedListener(listener);
    }

    /**
     * 添加图片标注
     *
     * @param resources 资源
     * @param mapCoord  坐标
     * @param resId     资源id
     */
    public static FMImageMarker buildImageMarker(Resources resources, FMMapCoord mapCoord, int resId) {
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resId);
        FMImageMarker imageMarker = new FMImageMarker(mapCoord, bitmap);
        //设置图片宽高
        imageMarker.setMarkerWidth(90);
        imageMarker.setMarkerHeight(90);
        //设置图片在模型之上
        imageMarker.setFMImageMarkerOffsetMode(FMImageMarker.FMImageMarkerOffsetMode.FMNODE_MODEL_ABOVE);
        return imageMarker;
    }

    /**
     * 创建文字标注
     *
     * @param mapCoord 坐标
     * @param text     文字
     * @return
     */
    public static FMTextMarker buildTextMarker(FMMapCoord mapCoord, String text) {
        FMTextMarker textMarker = new FMTextMarker(mapCoord, text);
        textMarker.setTextFillColor(Color.RED);
        textMarker.setTextStrokeColor(Color.RED);
        textMarker.setTextSize(30);
        //设置文字偏移高度
        textMarker.setFMTextMarkerOffsetMode(FMTextMarker.FMTextMarkerOffsetMode.FMNODE_CUSTOM_HEIGHT);
        textMarker.setCustomOffsetHeight(5);
        return textMarker;
    }


    /**
     * 创建定位标注
     *
     * @param groupId  楼层id
     * @param mapCoord 坐标点
     * @param angle    方向
     * @return
     */
    public static FMLocationMarker buildLocationMarker(int groupId, FMMapCoord mapCoord, float angle) {
        FMLocationMarker locationMarker = new FMLocationMarker(groupId, mapCoord);
        //设置定位点图片
        locationMarker.setActiveImageFromAssets("active.png");
        //设置定位图片宽高
        locationMarker.setMarkerWidth(90);
        locationMarker.setMarkerHeight(90);
        locationMarker.setAngle(angle);
        return locationMarker;
    }

    /**
     * 创建定位标注
     *
     * @param groupId  楼层id
     * @param mapCoord 坐标点
     * @return
     */
    public static FMLocationMarker buildLocationMarker(int groupId, FMMapCoord mapCoord) {
        return buildLocationMarker(groupId, mapCoord, 0f);
    }

    /**
     * 创建定位标注
     *
     * @param groupId  楼层id
     * @param mapCoord 坐标点
     * @param resID    资源文件
     * @return
     */
    public static FMLocationMarker buildLocationMarker(int groupId, FMMapCoord mapCoord, int resID) {
        FMLocationMarker locationMarker = new FMLocationMarker(groupId, mapCoord);
        //设置定位点图片
        locationMarker.setActiveImageFromRes(resID);
        //设置定位图片宽高
        locationMarker.setMarkerWidth(30);
        locationMarker.setMarkerHeight(30);
        return locationMarker;
    }

    /**
     * 创建线
     *
     * @param segments 线段集合
     * @return
     */
    public static FMLineMarker buildLineMarker(ArrayList<FMSegment> segments) {
        FMLineMarker lineMarker = new FMLineMarker(segments);
        lineMarker.setLineWidth(3.0f);
        return lineMarker;
    }

    /**
     * 获取当前角度
     *
     * @param map     地图对象
     * @param toAngle 到达的角度
     * @return
     */
    public static float getCurrentAngle(FMMap map, float toAngle) {
        float startAngle = map.getRotateAngle();
        float changeAngle = toAngle - startAngle;
        if (changeAngle > 180) {
            startAngle += 360.0f;
        } else if (changeAngle < -180) {
            startAngle -= 360.0f;
        }
        return startAngle;
    }

    /**
     * 动画转动地图
     *
     * @param map   地图对象
     * @param angle 角度
     */
    public static void animateRotate(final FMMap map, float angle) {
        map.setRotateAngle(angle);
    }

}
