package com.wb.base.component.monitor.convert.impl;

import com.wb.base.component.monitor.constant.MonitorConstants;
import com.wb.base.component.monitor.convert.AbstractMeterConverter;
import com.wb.base.component.monitor.model.OpsMonitorStatisticsData;
import com.wb.base.component.monitor.model.WbMonitorStatisticsData;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: TimerMeterConverter
 * @Description: Timer类转换器
 * @date 2022/3/29 11:36 AM
 */
@Slf4j
public class LongTimerMeterConverter extends AbstractMeterConverter<LongTaskTimer> {

    @Override
    protected boolean convertCheck(MeterInfo<LongTaskTimer> meterInfo) {
        /**
         * 排除老的微服务整体统计
         */
        return meterInfo.getMeter().max(MonitorConstants.DEFAULT_TIME_UNIT) > 0 && !"/ms/*/*".equals(MapUtils.getString(meterInfo.getTags(), "uri"));
    }

    @Override
    public WbMonitorStatisticsData doConvert(MeterInfo<LongTaskTimer> meterInfo) {
        try {
            List<OpsMonitorStatisticsData> list = new ArrayList<>();
            TimeUnit timeUnit = MonitorConstants.DEFAULT_TIME_UNIT;
            OpsMonitorStatisticsData.OpsMonitorStatisticsDataBuilder builder = OpsMonitorStatisticsData.builder().measurement(meterInfo.getMeasurement()).timestamp(meterInfo.getTimestamp() / 1000).tags(meterInfo.getTags());
            //count
            Map<String, Object> values = new HashMap<>(16);
            values.put(Statistic.MAX.getTagValueRepresentation(), meterInfo.getMeter().max(timeUnit));
            values.put("avg", meterInfo.getMeter().mean(timeUnit));
            list.add(builder.value(values).build());
            return WbMonitorStatisticsData.builder().businessType(meterInfo.getBusinessType()).data(list).build();
        } catch (Exception e) {
            log.warn("LongTimerMeterConverter convert error ! Meter : {}", meterInfo.getMeter().getId(), e);
        }
        return null;
    }
}