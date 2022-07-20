package com.monitor.base.component.monitor.convert.impl;

import com.monitor.base.component.monitor.convert.AbstractMeterConverter;
import com.monitor.base.component.monitor.model.OpsMonitorStatisticsData;
import com.monitor.base.component.monitor.model.LMonitorStatisticsData;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Statistic;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: SummaryMeterConverter
 * @Description: Summary类转换器
 * @date 2022/3/29 11:36 AM
 */
@Slf4j
public class SummaryMeterConverter extends AbstractMeterConverter<DistributionSummary> {

    @Override
    protected boolean convertCheck(MeterInfo<DistributionSummary> meterInfo) {
        return true;
    }

    @Override
    public LMonitorStatisticsData doConvert(MeterInfo<DistributionSummary> meterInfo) {
        try {
            List<OpsMonitorStatisticsData> list = new ArrayList<>();
            OpsMonitorStatisticsData.OpsMonitorStatisticsDataBuilder builder = OpsMonitorStatisticsData.builder().measurement(meterInfo.getMeasurement()).timestamp(meterInfo.getTimestamp() / 1000).tags(meterInfo.getTags());
            //count
            Map<String, Object> values = new HashMap<>(16);
            values.put(Statistic.COUNT.getTagValueRepresentation(), meterInfo.getMeter().count());
            values.put(Statistic.MAX.getTagValueRepresentation(), meterInfo.getMeter().max());
            values.put("avg", meterInfo.getMeter().mean());
            //百分位与直方图
            Arrays.stream(meterInfo.getMeter().takeSnapshot().percentileValues()).forEach(e-> values.put("P" + String.valueOf(e.percentile() * 100).replace(".0","").replace(".",""), e.value()));
            if (meterInfo.getBusinessType().isBusiness()){
                Arrays.stream(meterInfo.getMeter().takeSnapshot().histogramCounts()).forEach(e -> values.put(String.valueOf((int) e.bucket()), e.count()));
            }
            list.add(builder.value(values).build());
            return LMonitorStatisticsData.builder().businessType(meterInfo.getBusinessType()).data(list).build();
        } catch (Exception e) {
            log.warn("SummaryMeterConverter convert error ! Meter : {}", meterInfo.getMeter().getId().toString(), e);
        }
        return null;
    }
}
