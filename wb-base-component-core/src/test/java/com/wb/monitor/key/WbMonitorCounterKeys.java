package com.wb.monitor.key;

import com.wb.base.component.monitor.WbMonitorCounterKey;

/**
* @Title: WbMonitorCounterKeys
* @Description: Counter类型监控指标管理枚举
* @author JerryLong
* @date 2022/4/7 11:51 AM
* @version V1.0
*/
public enum WbMonitorCounterKeys implements WbMonitorCounterKey {
    /**
     * 要求：驼峰，简明释义
     */
    COUNTER_KEY_1("counterOne"),
    ;

    private String monitorKey;

    WbMonitorCounterKeys(String monitorKey){
        this.monitorKey = monitorKey;
    }

    @Override
    public String getMonitorKey() {
        return monitorKey;
    }
}
