package com.wb.base.component.starter.constant;

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
     * 缓存命令
     */
    CACHE_COMMANDS("CacheCommandsTime"),
    /**
     * DB方法执行
     */
    DB_METHOD("dbExecuteTimer"),
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