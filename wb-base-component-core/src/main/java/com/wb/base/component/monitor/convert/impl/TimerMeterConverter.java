package com.wb.base.component.monitor.convert.impl;

import com.wb.base.component.monitor.constant.MonitorConstants;
import com.wb.base.component.monitor.convert.AbstractMeterConverter;
import com.wb.base.component.monitor.model.OpsMonitorStatisticsData;
import com.wb.base.component.monitor.model.WbMonitorStatisticsData;
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
public class TimerMeterConverter extends AbstractMeterConverter<Timer> {

    @Override
    protected boolean convertCheck(MeterInfo<Timer> meterInfo) {
        /**
         * 排除老的微服务整体统计
         */
        return meterInfo.getMeter().count() > 0 && !"/ms/*/*".equals(MapUtils.getString(meterInfo.getTags(), "uri"));
    }

    @Override
    public WbMonitorStatisticsData doConvert(MeterInfo<Timer> meterInfo) {
        try {
            List<OpsMonitorStatisticsData> list = new ArrayList<>();
            TimeUnit timeUnit = MonitorConstants.DEFAULT_TIME_UNIT;
            OpsMonitorStatisticsData.OpsMonitorStatisticsDataBuilder builder = OpsMonitorStatisticsData.builder().measurement(meterInfo.getMeasurement()).timestamp(meterInfo.getTimestamp() / 1000).tags(meterInfo.getTags());
            //count
            Map<String, Object> values = new HashMap<>(16);
            values.put(Statistic.COUNT.getTagValueRepresentation(), meterInfo.getMeter().count());
            values.put(Statistic.MAX.getTagValueRepresentation(), meterInfo.getMeter().max(timeUnit));
            values.put("avg", meterInfo.getMeter().mean(timeUnit));
            //百分位与直方图
            Arrays.stream(meterInfo.getMeter().takeSnapshot().percentileValues()).forEach(e-> values.put("P" + String.valueOf(e.percentile() * 100).replace(".0","").replace(".",""), e.value(timeUnit)));
            if (meterInfo.getBusinessType().isBusiness()){
                Arrays.stream(meterInfo.getMeter().takeSnapshot().histogramCounts()).forEach(e -> values.put(String.valueOf((int) e.bucket()), e.count()));
            }
            list.add(builder.value(values).build());
            return WbMonitorStatisticsData.builder().businessType(meterInfo.getBusinessType()).data(list).build();
        } catch (Exception e) {
            log.warn("TimerMeterConverter convert error ! Meter : {}", meterInfo.getMeter().getId().toString(), e);
        }
        return null;
    }
}