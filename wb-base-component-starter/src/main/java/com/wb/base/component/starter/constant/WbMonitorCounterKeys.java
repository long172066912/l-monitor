package com.wb.base.component.starter.constant;

import com.wb.base.component.monitor.WbMonitorCounterKey;
import com.wb.base.component.monitor.convert.AbstractMeterConverter;

/**   
* @Title: WbMonitorCounterKeys
* @Description: //TODO (用一句话描述该文件做什么) 
* @author JerryLong  
* @date 2022/5/10 10:01 AM 
* @version V1.0    
*/
public enum WbMonitorCounterKeys implements WbMonitorCounterKey {
    /**
     * 缓存命令执行次数统计
     */
    CACHE_COMMANDS("CacheCommandsCount"),
    /**
     * RPC接口请求监控
     */
    WB_RPC_HTTP_SERVER_REQUEST(AbstractMeterConverter.METER_PREFIX + "rpcHttpServerRequests"),
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
