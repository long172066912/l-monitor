package com.monitor.base.component.monitor.model;

import org.apache.commons.lang3.StringUtils;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: MonitorBusinessType
 * @Description: 监控数据业务类型
 * @date 2022/3/30 11:35 AM
 */
public enum MonitorBusinessType {
    /**
     * 系统
     */
    SYSTEM,
    /**
     * 业务
     */
    BUSINESS,
    ;

    /**
     * key
     */
    public static final String MONITOR_TYPE_KEY = "MonitorBusinessType";

    public boolean isBusiness(){
        return MonitorBusinessType.BUSINESS.equals(this);
    }

    /**
     * 获取监控类型
     * @param name
     * @return
     */
    public static MonitorBusinessType getMonitorType(String name) {
        if (StringUtils.isBlank(name)) {
            return SYSTEM;
        }
        return MonitorBusinessType.valueOf(name);
    }
}