package com.wb.base.component.monitor.convert.impl;

import com.google.common.collect.ImmutableMap;
import com.wb.base.component.monitor.convert.AbstractMeterConverter;
import com.wb.base.component.monitor.model.OpsMonitorStatisticsData;
import com.wb.base.component.monitor.model.WbMonitorStatisticsData;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Statistic;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: GaugeMeterConverter
 * @Description: Gauge类转换器
 * @date 2022/3/29 11:36 AM
 */
@Slf4j
public class GaugeMeterConverter extends AbstractMeterConverter<Gauge> {

    @Override
    protected boolean convertCheck(MeterInfo<Gauge> meterInfo) {
        return true;
    }

    @Override
    public WbMonitorStatisticsData doConvert(MeterInfo<Gauge> meterInfo) {
        try {
            OpsMonitorStatisticsData data = OpsMonitorStatisticsData.builder().measurement(meterInfo.getMeasurement()).timestamp(meterInfo.getTimestamp() / 1000).tags(meterInfo.getTags()).value(ImmutableMap.of(Statistic.VALUE.getTagValueRepresentation(), meterInfo.getMeter().value())).build();
            return WbMonitorStatisticsData.builder().businessType(meterInfo.getBusinessType()).data(Arrays.asList(data)).build();
        } catch (Exception e) {
            log.warn("GaugeMeterConverter convert error ! Meter : {}", meterInfo.getMeter().getId().toString(), e);
        }
        return null;
    }
}
