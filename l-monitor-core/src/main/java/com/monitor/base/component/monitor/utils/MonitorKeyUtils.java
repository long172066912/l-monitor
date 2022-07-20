package com.monitor.base.component.monitor.utils;

import io.micrometer.core.instrument.Meter;

/**
* @Title: MonitorKeyUtils
* @Description: 对Key的相关操作
* @author JerryLong
* @date 2022/3/30 5:12 PM
* @version V1.0
*/
public class MonitorKeyUtils {

    public static String buildMonitoryKey(String key, Meter.Type type){
        return key + type.name();
    }

    public static String getMonitoryKey(String key, Meter.Type type){
        return key.endsWith(type.name()) ? key.replace(type.name(), "") : key;
    }
}
