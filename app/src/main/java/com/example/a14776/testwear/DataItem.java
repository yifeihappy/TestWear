package com.example.a14776.testwear;

/**
 * Created by 14776 on 2019/4/21.
 */

public class DataItem {
    private float x;
    private float y;
    private float z;
    private long timestamp;
    private int sensorType;

    public DataItem(int sensorType, long timestamp, float x, float y, float z) {
        this.sensorType = sensorType;
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getSensorType() {
        return sensorType;
    }

    public void setSensorType(int sensorType) {
        this.sensorType = sensorType;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
