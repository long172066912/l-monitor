package com.wb.base.component.monitor.utils;

import com.wb.base.component.monitor.model.OpsMonitorConfigs;

/**
* @Title: MonitorConfigUtils
* @Description: //TODO (用一句话描述该文件做什么)
* @author JerryLong
* @date 2022/4/8 4:04 PM
* @version V1.0
*/
public class MonitorConfigUtils {

    /**
     * 获取Ops配置
     *
     * @return
     */
    public static OpsMonitorConfigs getConfigByBusinessTag() {
        return OpsMonitorConfigs.builder().uri("http://localhost/reportMonitorData").build();
    }
}
