package com.monitor.base.component.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
* @Title: LMonitorStatisticsData
* @Description: //TODO (用一句话描述该文件做什么)
* @author JerryLong
* @date 2022/3/29 4:00 PM
* @version V1.0
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LMonitorStatisticsData {
    /**
     * 数据类型
     */
    private MonitorBusinessType businessType;
    /**
     * 监控数据列表
     */
    List<OpsMonitorStatisticsData> data;
}
