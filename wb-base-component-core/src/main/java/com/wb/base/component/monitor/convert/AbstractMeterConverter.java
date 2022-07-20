package com.wb.base.component.monitor.convert;

import com.google.common.collect.ImmutableSet;
import com.wb.base.component.monitor.model.MonitorBusinessType;
import com.wb.base.component.monitor.model.WbMonitorStatisticsData;
import com.wb.base.component.monitor.utils.MonitorKeyUtils;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Meter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.text.CaseUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: MeterConverter
 * @Description: 转换器接口
 * @date 2022/3/29 11:33 AM
 */
public abstract class AbstractMeterConverter<T extends Meter> implements MeterConverter<T, WbMonitorStatisticsData>, MeterConvertFunction<T, WbMonitorStatisticsData> {
    /**
     * 需要排除的tags
     */
    private static final String[] EXCLUDE_TAGS = new String[]{MonitorBusinessType.MONITOR_TYPE_KEY};
    /**
     * 不进行统计的names，以前缀进行匹配
     */
    private static final Set<String> EXCLUDE_NAMES = ImmutableSet.of("logback.events");
    /**
     * 所有监控指标默认前缀
     */
    public static final String METER_PREFIX = "wb-";

    private static final String HTTP_REQUEST_OUT_COME = "outcome";
    private static final String FUNCTION_RESULT = "result";

    /**
     * 对meter进行校验，返回检查结果，false将不处理直接返回null
     *
     * @param meter
     * @return
     */
    protected abstract boolean convertCheck(MeterInfo<T> meter);

    @Override
    public WbMonitorStatisticsData convert(T meter, long timestamp) {
        if (!this.isConvert(meter)) {
            return null;
        }
        MeterInfo meterInfo = this.doAnalysis(meter, timestamp);
        if (!this.convertCheck(meterInfo)) {
            return null;
        }
        return doConvert(meterInfo);
    }

    /**
     * 对Meter进行检测，判断是否需要转换
     *
     * @param meter
     * @return
     */
    protected boolean isConvert(T meter) {
        if (EXCLUDE_NAMES.contains(meter.getId().getName())) {
            return false;
        }
        return true;
    }

    /**
     * 解析meter，拿到解析结果
     *
     * @param meter
     * @return
     */
    protected MeterInfo doAnalysis(T meter, long timestamp) {
        Map<String, String> tags = StreamSupport.stream(meter.getId().getTagsAsIterable().spliterator(), false).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        MonitorBusinessType monitorBusinessType = getMonitorBusinessType(tags);
        String measurement = this.buildMeasurement(meter, monitorBusinessType);
        tags = tagFilter(tags);
        return MeterInfo.builder().meter(meter).timestamp(timestamp).tags(tags).businessType(monitorBusinessType).measurement(measurement).build();
    }

    /**
     * 对tags进行过滤处理
     *
     * @param tags
     * @return
     */
    private Map<String, String> tagFilter(Map<String, String> tags) {
        //对tags做处理，排除一些不必要的
        Arrays.stream(EXCLUDE_TAGS).forEach(e -> tags.remove(e));
        if (tags.containsKey(HTTP_REQUEST_OUT_COME) && !MapUtils.getString(tags, TimedAspect.EXCEPTION_TAG, TimedAspect.DEFAULT_EXCEPTION_TAG_VALUE).toLowerCase(Locale.ROOT).equals(TimedAspect.DEFAULT_EXCEPTION_TAG_VALUE)) {
            tags.put(HTTP_REQUEST_OUT_COME, "FAIL");
        }
        if (tags.containsKey(FUNCTION_RESULT) && !MapUtils.getString(tags, TimedAspect.EXCEPTION_TAG, TimedAspect.DEFAULT_EXCEPTION_TAG_VALUE).toLowerCase(Locale.ROOT).equals(TimedAspect.DEFAULT_EXCEPTION_TAG_VALUE)) {
            tags.put(FUNCTION_RESULT, "FAIL");
        }
        return tags;
    }

    private String buildMeasurement(T meter, MonitorBusinessType monitorBusinessType) {
        boolean isBusiness = monitorBusinessType.equals(MonitorBusinessType.BUSINESS);
        String measurement = meter.getId().getName();
        measurement = measurement.indexOf(".") > 0 ? CaseUtils.toCamelCase(measurement, false, new char[]{'.'}) : measurement;
        return isBusiness ? measurement : METER_PREFIX + measurement;
    }

    /**
     * 获取监控数据业务类型
     *
     * @param tags
     * @return
     */
    private MonitorBusinessType getMonitorBusinessType(Map<String, String> tags) {
        return MonitorBusinessType.getMonitorType(MapUtils.getString(tags, MonitorBusinessType.MONITOR_TYPE_KEY));
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterInfo<T> {
        private T meter;
        private String measurement;
        private long timestamp;
        private Map<String, String> tags;
        private MonitorBusinessType businessType;
    }
}
