package com.wb.monitor.key;


import com.wb.base.component.monitor.WbMonitorTimerKey;

/**
* @Title: FriendMonitorTimerKey
* @Description: Timer类型监控指标管理枚举
* @author JerryLong
* @date 2022/4/7 11:51 AM
* @version V1.0
*/
public enum WbMonitorTimerKeys implements WbMonitorTimerKey {
    /**
     * 要求：驼峰，简明释义
     */
    TIMER_KEY_1("timerOne"),
    ;

    private String monitorKey;

    WbMonitorTimerKeys(String monitorKey){
        this.monitorKey = monitorKey;
    }

    @Override
    public String getMonitorKey() {
        return monitorKey;
    }
}
