package com.monitor.base.component.monitor;

/**
* @Title: LMonitorTimerKey
* @Description: //TODO (用一句话描述该文件做什么)
* @author JerryLong
* @date 2022/3/29 3:05 PM
* @version V1.0
*/
@FunctionalInterface
public interface LMonitorTimerKey {
    /**
     * 实现指标名称-时间维度统计，max/avg/count/P50/P90/P99
     *
     * @return
     */
    String getMonitorKey();

    /**
     * 获取带业务属性的指标名称
     * @return
     */
    default String getKey(){
        return getMonitorKey();
    }
}
