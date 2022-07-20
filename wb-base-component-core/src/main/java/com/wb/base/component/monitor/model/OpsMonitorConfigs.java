package com.wb.base.component.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: OpsMonitorConfigs
 * @Description: //TODO (用一句话描述该文件做什么)
 * @date 2022/4/8 3:58 PM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpsMonitorConfigs {
    /**
     * monitor数据推送地址
     */
    private String uri;
    /**
     * 服务接口同步地址
     */
    private String rpcSyncUri;
}