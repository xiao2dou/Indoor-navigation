package com.example.jin.hellofengmap.location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jin on 2017/7/16.
 */

public class iBeacons extends iBeacon {

    public List<String> rssiList=new ArrayList<String>();

    public iBeacons(){

    }
    public iBeacons(iBeacon ibeacon){
        super(ibeacon.name,ibeacon.major,ibeacon.minor,ibeacon.proximityUuid,ibeacon.bluetoothAddress,ibeacon.txPower,ibeacon.rssi);
    }

    public List<String> getRssiList() {
        return rssiList;
    }

    public void setRssiList(List<String> rssiList) {
        this.rssiList = rssiList;
    }
}
