package com.monitor.base.component.starter.constant;

import com.monitor.base.component.monitor.LMonitorCounterKey;
import com.monitor.base.component.monitor.convert.AbstractMeterConverter;

/**   
* @Title: LMonitorCounterKeys
* @Description: //TODO (用一句话描述该文件做什么) 
* @author JerryLong  
* @date 2022/5/10 10:01 AM 
* @version V1.0    
*/
public enum LMonitorCounterKeys implements LMonitorCounterKey {
    /**
     * 缓存命令执行次数统计
     */
    CACHE_COMMANDS("CacheCommandsCount"),
    /**
     * RPC接口请求监控
     */
    L_MONITOR_RPC_HTTP_SERVER_REQUEST(AbstractMeterConverter.METER_PREFIX + "rpcHttpServerRequests"),
    ;

    private String monitorKey;

    LMonitorCounterKeys(String monitorKey){
        this.monitorKey = monitorKey;
    }

    @Override
    public String getMonitorKey() {
        return monitorKey;
    }
}
