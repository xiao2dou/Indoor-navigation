package com.example.jin.hellofengmap;

/**
 * 作为功能ListView的适配类型
 * Created by Zhaoguo Wang on 2017/7/27.
 */

public class SettingClass {
    private String functionName;

    private int imageId;

    public SettingClass(String functionName,int imageId){
        this.functionName=functionName;
        this.imageId=imageId;
    }

    public String getFunctionName(){return this.functionName;}

    public int getImageId(){return this.imageId;}
}
