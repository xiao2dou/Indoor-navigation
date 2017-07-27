package com.example.jin.hellofengmap;

import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.jin.hellofengmap.Adapter.SettingAdapter;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MySettingActivity extends AppCompatActivity {

    //存放每个子项
    private List<SettingClass> settingList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_setting);
        initSettings();
        //利用settingList获取并设置适配器
        SettingAdapter adapter=new SettingAdapter(MySettingActivity.this,R.layout.function_item,settingList);
        ListView listView=(ListView)findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        //设置栏的点击事件
        //可以在这里添加相应的功能
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //根据位置获取点击的子项
                SettingClass setting=settingList.get(i);
                switch(setting.getImageId()){
                    case R.drawable.ic_business_black_24dp:
                        Toast.makeText(MySettingActivity.this,"商业合作",Toast.LENGTH_SHORT).show();
                        break;
                    case R.drawable.ic_room_black_24dp:
                        Toast.makeText(MySettingActivity.this,"定位",Toast.LENGTH_SHORT).show();
                        break;
                    case R.drawable.ic_help_black_24dp:
                        Toast.makeText(MySettingActivity.this,"帮助",Toast.LENGTH_SHORT).show();
                        break;
                    case R.drawable.ic_reply_black_24dp:
                        Toast.makeText(MySettingActivity.this,"反馈",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });

        //头像的点击事件
        //这里可以添加修改个人信息的功能
        CircleImageView head=(CircleImageView)findViewById(R.id.icon_image);
        head.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(MySettingActivity.this,"更换头像",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 初始化ListView里面的子项
     */
    private void initSettings(){
        SettingClass business=new SettingClass("商业合作",R.drawable.ic_business_black_24dp);
        settingList.add(business);
        SettingClass location=new SettingClass("定位",R.drawable.ic_room_black_24dp);
        settingList.add(location);
        SettingClass help=new SettingClass("帮助",R.drawable.ic_help_black_24dp);
        settingList.add(help);
        SettingClass feedBack=new SettingClass("反馈",R.drawable.ic_reply_black_24dp);
        settingList.add(feedBack);
    }
}
