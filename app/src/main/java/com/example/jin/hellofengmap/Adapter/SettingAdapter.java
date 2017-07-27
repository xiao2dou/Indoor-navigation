package com.example.jin.hellofengmap.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jin.hellofengmap.R;
import com.example.jin.hellofengmap.SettingClass;

import java.util.List;

/**
 * ListView的适配器
 * Created by Zhaoguo Wang on 2017/7/27.
 */

public class SettingAdapter extends ArrayAdapter<SettingClass> {
    private int resourceId;     //索引

    public SettingAdapter(Context context, int textViewResourceId, List<SettingClass> objects){
        super(context,textViewResourceId,objects);
        resourceId = textViewResourceId;      //获取索引值
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SettingClass setting=getItem(position);     //获取当前实例
        //加载当前子项，在listview被滚动时调用
        View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        ImageView settingImage = (ImageView) view.findViewById(R.id.function_image);
        TextView settingName = (TextView) view.findViewById(R.id.function_name);
        settingImage.setImageResource(setting.getImageId());
        settingName.setText(setting.getFunctionName());
        return view;
    }
}
