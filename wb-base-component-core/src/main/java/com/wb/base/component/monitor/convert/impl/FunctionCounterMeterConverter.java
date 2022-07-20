package com.wb.base.component.monitor.convert.impl;

import com.google.common.collect.ImmutableMap;
import com.wb.base.component.monitor.convert.AbstractMeterConverter;
import com.wb.base.component.monitor.model.OpsMonitorStatisticsData;
import com.wb.base.component.monitor.model.WbMonitorStatisticsData;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Statistic;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: CounterMeterConverter
 * @Description: Counter类转换器
 * @date 2022/3/29 11:36 AM
 */
@Slf4j
public class FunctionCounterMeterConverter extends AbstractMeterConverter<FunctionCounter> {

    public static final String TYPE = "FUNCTION_COUNTER";

    @Override
    protected boolean convertCheck(MeterInfo<FunctionCounter> meterInfo) {
        return !meterInfo.getBusinessType().isBusiness() || meterInfo.getMeter().count() > 0;
    }

    @Override
    public WbMonitorStatisticsData doConvert(MeterInfo<FunctionCounter> meterInfo) {
        try {
            OpsMonitorStatisticsData data = OpsMonitorStatisticsData.builder().measurement(meterInfo.getMeasurement()).timestamp(meterInfo.getTimestamp() / 1000).tags(meterInfo.getTags()).value(ImmutableMap.of(Statistic.COUNT.getTagValueRepresentation(), meterInfo.getMeter().count())).build();
            return WbMonitorStatisticsData.builder().businessType(meterInfo.getBusinessType()).data(Arrays.asList(data)).build();
        } catch (Exception e) {
            log.warn("FunctionCounterMeterConverter convert error ! Meter : {}", meterInfo.getMeter().getId().toString(), e);
        }
        return null;
    }
}
