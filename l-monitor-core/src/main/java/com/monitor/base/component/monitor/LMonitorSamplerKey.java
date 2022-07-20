package com.monitor.base.component.monitor;

/**
* @Title: LMonitorSamplerKey
* @Description: //TODO (用一句话描述该文件做什么) 
* @author JerryLong  
* @date 2022/3/29 3:05 PM 
* @version V1.0    
*/
@FunctionalInterface
public interface LMonitorSamplerKey {
    /**
     * 实现指标名称-自增类统计
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
