package com.example.jin.hellofengmap.utils;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.fengmap.android.map.geometry.FMMapCoord;
import com.fengmap.android.map.marker.FMImageMarker;
import com.fengmap.android.map.marker.FMTextMarker;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 控件控制帮助类
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
     * 设置控件的选中状态改变事件监听
     *
     * @param activity Activity
     * @param id       控件id
     * @param listener CheckBox选中状态改变事件
     */
    public static void setViewCheckedChangeListener(Activity activity, int id,
                                                    CompoundButton.OnCheckedChangeListener listener) {
        CheckBox view = getView(activity, id);
        view.setOnCheckedChangeListener(listener);
    }

    /**
     * 添加图片标注
     *
     * @param resId 资源id
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
        //设置文字在模型之上
        textMarker.setFMTextMarkerOffsetMode(FMTextMarker.FMTextMarkerOffsetMode.FMNODE_MODEL_ABOVE);
        return textMarker;
    }


    /**
     * 设置控件的点击事件
     *
     * @param activity Activity
     * @param id       控件id
     * @param enabled  是否可用
     */
    public static void setViewEnable(Activity activity, int id, boolean enabled) {
        View view = getView(activity, id);
        view.setEnabled(enabled);
    }
}
