package com.monitor.base.component.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
* @Title: OpsMonitorStatisticsData
* @author JerryLong
* @date 2022/3/29 4:00 PM
* @version V1.0
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpsMonitorStatisticsData {
    /**
     * 指标名称，插入即创建，不需要提前创建。驼峰命名
     */
    private String measurement;
    /**
     * 秒级时间戳
     */
    private long timestamp;
    /**
     * 不常变的数据放在此，该字段有索引，子字段类型为字符串。
     */
    private Map<String, String> tags;
    /**
     * 当前时间点取值，变化值。子字段可取浮点数、字符串、数字
     */
    private Map<String, Object> value;
}
