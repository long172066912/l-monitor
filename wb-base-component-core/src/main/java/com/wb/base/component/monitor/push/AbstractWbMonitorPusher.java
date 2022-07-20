package com.wb.base.component.monitor.push;

import com.wb.base.component.monitor.model.WbMonitorStatisticsData;

/**
* @Title: WbMonitorPusher
* @Description: 监控数据推送
* @author JerryLong
* @date 2022/3/29 10:31 AM
* @version V1.0
*/
public abstract class AbstractWbMonitorPusher {

    /**
     * 数据推送
     * @param data
     */
    public abstract void push(WbMonitorStatisticsData data);

    /**
     * 关闭，需要显示调用
     */
    public abstract void close();
}
