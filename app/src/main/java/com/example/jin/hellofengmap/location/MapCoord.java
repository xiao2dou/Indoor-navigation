package com.example.jin.hellofengmap.location;

import com.fengmap.android.map.geometry.FMMapCoord;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 对楼层id和FMMapCoord封装的实体类
 */
public class MapCoord {

    private int groupId;
    private FMMapCoord mapCoord;

    public MapCoord(int groupId, FMMapCoord mapCoord) {
        this.groupId = groupId;
        this.mapCoord = mapCoord;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public FMMapCoord getMapCoord() {
        return mapCoord;
    }

    public void setMapCoord(FMMapCoord mapCoord) {
        this.mapCoord = mapCoord;
    }
}
