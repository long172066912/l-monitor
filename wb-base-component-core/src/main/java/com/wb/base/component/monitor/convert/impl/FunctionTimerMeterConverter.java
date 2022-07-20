package com.wb.base.component.monitor.convert.impl;

import com.wb.base.component.monitor.constant.MonitorConstants;
import com.wb.base.component.monitor.convert.AbstractMeterConverter;
import com.wb.base.component.monitor.model.OpsMonitorStatisticsData;
import com.wb.base.component.monitor.model.WbMonitorStatisticsData;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Statistic;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: TimerMeterConverter
 * @Description: Timer类转换器
 * @date 2022/3/29 11:36 AM
 */
@Slf4j
public class FunctionTimerMeterConverter extends AbstractMeterConverter<FunctionTimer> {

    public static final String TYPE = "FUNCTION_TIMER";

    @Override
    protected boolean convertCheck(MeterInfo<FunctionTimer> meterInfo) {
        return meterInfo.getMeter().count() > 0;
    }

    @Override
    public WbMonitorStatisticsData doConvert(MeterInfo<FunctionTimer> meterInfo) {
        try {
            List<OpsMonitorStatisticsData> list = new ArrayList<>();
            TimeUnit timeUnit = MonitorConstants.DEFAULT_TIME_UNIT;
            OpsMonitorStatisticsData.OpsMonitorStatisticsDataBuilder builder = OpsMonitorStatisticsData.builder().measurement(meterInfo.getMeasurement()).timestamp(meterInfo.getTimestamp() / 1000).tags(meterInfo.getTags());
            //count
            Map<String, Object> values = new HashMap<>(16);
            values.put(Statistic.COUNT.getTagValueRepresentation(), meterInfo.getMeter().count());
            values.put("avg", meterInfo.getMeter().mean(timeUnit));
            values.put("totalTime", meterInfo.getMeter().totalTime(timeUnit));
            list.add(builder.value(values).build());
            return WbMonitorStatisticsData.builder().businessType(meterInfo.getBusinessType()).data(list).build();
        } catch (Exception e) {
            log.warn("FunctionTimerMeterConverter convert error ! Meter : {}", meterInfo.getMeter().getId().toString(), e);
        }
        return null;
    }
}