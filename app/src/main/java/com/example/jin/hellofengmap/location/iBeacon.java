package com.example.jin.hellofengmap.location;


/**
 * iBeacon类
 * Created by jin on 2017/7/15.
 */

public class iBeacon {

    protected String name;
    protected int major;
    protected int minor;
    protected String proximityUuid;
    protected String bluetoothAddress;
    protected int txPower;
    protected int rssi;
    protected double distance;

    public iBeacon(String name, int major, int minor, String proximityUuid, String bluetoothAddress, int txPower, int rssi){
        this.name=name;
        this.major=major;
        this.minor=minor;
        this.proximityUuid=proximityUuid;
        this.bluetoothAddress=bluetoothAddress;
        this.txPower=txPower;
        this.rssi=rssi;
    }
    public iBeacon(iBeacons ibeacons,int rssi){
        this.name=ibeacons.name;
        this.major=ibeacons.getMajor();
        this.minor=ibeacons.getMinor();
        this.proximityUuid=ibeacons.getProximityUuid();
        this.bluetoothAddress=ibeacons.getBluetoothAddress();
        this.txPower=ibeacons.getTxPower();
        this.rssi=rssi;
    }
    public iBeacon(){

    }

    public String getName() {
        return name;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public String getProximityUuid() {
        return proximityUuid;
    }

    public String getBluetoothAddress() {
        return bluetoothAddress;
    }

    public int getTxPower() {
        return txPower;
    }

    public int getRssi() {
        return rssi;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * 重载toString()方法
     * 为网络上传做准备
     * @return
     */
    @Override
    public String toString() {
        return "[" + this.name +"$"+this.major+"$"+this.minor+"$"+this.proximityUuid+"$"+this.bluetoothAddress+"$"+this.txPower+"$"+this.rssi+"$"+"]";
    }
}
