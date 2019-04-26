package com.example.a14776.testwear;

import android.util.Log;

import java.util.LinkedList;

/**
 * Created by 14776 on 2019/4/21.
 */

public class Utils {
    //滑动窗口
    public static void slide_win(LinkedList<DataItem> dataList, long win_size, float rate) {
        long win_size1 = (long)(win_size*rate);
        while(dataList.getLast().getTimestamp() - dataList.getFirst().getTimestamp() >  win_size1) {
            dataList.removeFirst();
        }
    }

    //判断是否发生tap事件

    /**
     *
     * @param dataList
     * @param thre
     * @return
     */
    public static boolean isTap(LinkedList<DataItem> dataList, float thre) {
        int len = dataList.size();
        DataItem  firstItem = dataList.getFirst();
        float sum = 0;
        for(DataItem dataItem: dataList) {
            float x = dataItem.getX() - firstItem.getX();
            float y = dataItem.getY() - firstItem.getY();
            float z = dataItem.getZ() - firstItem.getZ();
            sum +=  x * x + y * y + z * z;
            firstItem = dataItem;
        }
        if(sum > Thresholds.ACC_TH) {
            return true;
        } else {
            return false;
        }
    }

    //判断是否只用线性加速度变化，而磁力计没有变化
    public static boolean isFalseStroke(LinkedList<DataItem> dataList, float thre) {
        float sum_x = 0;
        float sum_y = 0;
        float sum_z = 0;
        int n = dataList.size();
        for(DataItem dataItem: dataList) {
            sum_x += dataItem.getX();
            sum_y += dataItem.getY();
            sum_z += dataItem.getZ();
        }
        float mean_x = sum_x/n;
        float mean_y = sum_y/n;
        float mean_z = sum_z/n;
        sum_x = 0;
        sum_y = 0;
        sum_z = 0;
        for(DataItem dataItem: dataList) {
            sum_x +=(dataItem.getX() - mean_x) * (dataItem.getX() - mean_x);
            sum_y += (dataItem.getY() - mean_y) * (dataItem.getY() - mean_y);
            sum_z += (dataItem.getZ() - mean_z) * (dataItem.getZ() - mean_z);
        }
        double sum = Math.sqrt(sum_x/n) + Math.sqrt(sum_y/n) + Math.sqrt(sum_z/n);
        if(sum < Thresholds.FALSE_STROKE_TH_STD) {
            Log.d("iss", "fase stoken" + sum);
            return false;
        } else {
            return true;
        }
    }


}
