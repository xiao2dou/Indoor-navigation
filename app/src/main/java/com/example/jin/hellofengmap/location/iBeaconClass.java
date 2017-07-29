package com.example.jin.hellofengmap.location;

import android.bluetooth.BluetoothDevice;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

/**
 * Created by jin on 2017/7/14.
 */

public class iBeaconClass {

    /**
     * 解析iBeacon数据报
     * @param device
     * @param rssi
     * @param scanData
     * @return
     */
    public static iBeacon fromScanData(BluetoothDevice device, int rssi, byte[] scanData) {

        int startByte = 2;
        boolean patternFound = false;
        while (startByte <= 5) {
            if (((int)scanData[startByte+2] & 0xff) == 0x02 &&
                    ((int)scanData[startByte+3] & 0xff) == 0x15) {
                // yes!  This is an iBeacon
                patternFound = true;
                break;
            }
            else if (((int)scanData[startByte] & 0xff) == 0x2d &&
                    ((int)scanData[startByte+1] & 0xff) == 0x24 &&
                    ((int)scanData[startByte+2] & 0xff) == 0xbf &&
                    ((int)scanData[startByte+3] & 0xff) == 0x16) {
                iBeacon iBeacon = new iBeacon();
                iBeacon.major = 0;
                iBeacon.minor = 0;
                iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
                iBeacon.txPower = -55;
                return iBeacon;
            }
            else if (((int)scanData[startByte] & 0xff) == 0xad &&
                    ((int)scanData[startByte+1] & 0xff) == 0x77 &&
                    ((int)scanData[startByte+2] & 0xff) == 0x00 &&
                    ((int)scanData[startByte+3] & 0xff) == 0xc6) {

                iBeacon iBeacon = new iBeacon();
                iBeacon.major = 0;
                iBeacon.minor = 0;
                iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
                iBeacon.txPower = -55;
                return iBeacon;
            }
            startByte++;
        }


        if (patternFound == false) {
            // This is not an iBeacon
            return null;
        }

        iBeacon iBeacon = new iBeacon();

        iBeacon.major = (scanData[startByte+20] & 0xff) * 0x100 + (scanData[startByte+21] & 0xff);
        iBeacon.minor = (scanData[startByte+22] & 0xff) * 0x100 + (scanData[startByte+23] & 0xff);
        iBeacon.txPower = (int)scanData[startByte+24]; // this one is signed
        iBeacon.rssi = rssi;

        // AirLocate:
        // 02 01 1a 1a ff 4c 00 02 15  # Apple's fixed iBeacon advertising prefix
        // e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0 # iBeacon profile uuid
        // 00 00 # major
        // 00 00 # minor
        // c5 # The 2's complement of the calibrated Tx Power

        // Estimote:
        // 02 01 1a 11 07 2d 24 bf 16
        // 394b31ba3f486415ab376e5c0f09457374696d6f7465426561636f6e00000000000000000000000000000000000000000000000000

        byte[] proximityUuidBytes = new byte[16];
        System.arraycopy(scanData, startByte+4, proximityUuidBytes, 0, 16);
        String hexString = bytesToHexString(proximityUuidBytes);
        StringBuilder sb = new StringBuilder();
        sb.append(hexString.substring(0,8));
        sb.append("-");
        sb.append(hexString.substring(8,12));
        sb.append("-");
        sb.append(hexString.substring(12,16));
        sb.append("-");
        sb.append(hexString.substring(16,20));
        sb.append("-");
        sb.append(hexString.substring(20,32));
        iBeacon.proximityUuid = sb.toString();

        if (device != null) {
            iBeacon.bluetoothAddress = device.getAddress();
            iBeacon.name = device.getName();
        }

        return iBeacon;
    }

    //处理iBeacon的UUID的一个方法，在解析iBeacon数据报时被调用
    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
//    //一个很不准的测距函数
//    protected static double calculateAccuracy(int txPower, int rssi) {
//        if (rssi == 0) {
//            return -1.0; // if we cannot determine accuracy, return -1.
//        }
//
//        double ratio = rssi * 1.0 / txPower;
//        if (ratio < 1.0) {
//            return Math.pow(ratio, 10);
//        } else {
//            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
//            return accuracy;
//        }
//    }

    /**
     * 计算距离函数
     * 超不准
     * 算法有待改进
     * @param txPower
     * @param rssi
     * @return 距离
     *
     * @author jin
     * Data:2017/7/16
     */
    protected static double CalculateDistance(int txPower,int rssi){
        double distance;
        double n=100.0;//环境衰减因子
        distance = pow(10,(abs(rssi) - txPower) / (10 * n));
        return distance;
    }
}
