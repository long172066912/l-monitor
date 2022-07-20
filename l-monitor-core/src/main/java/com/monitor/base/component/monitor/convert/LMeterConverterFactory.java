package com.monitor.base.component.monitor.convert;

import com.monitor.base.component.monitor.convert.impl.*;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Meter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: LMeterConverterFactory
 * @Description: //TODO (用一句话描述该文件做什么)
 * @date 2022/3/29 11:39 AM
 */
public class LMeterConverterFactory {

    private static Map<String, MeterConverter> converterMap = new ConcurrentHashMap<>();

    static {
        converterMap.put("COUNTER", new CounterMeterConverter());
        converterMap.put(FunctionCounterMeterConverter.TYPE, new FunctionCounterMeterConverter());
        converterMap.put(FunctionTimerMeterConverter.TYPE, new FunctionTimerMeterConverter());
        converterMap.put("GAUGE", new GaugeMeterConverter());
        converterMap.put("DISTRIBUTION_SUMMARY", new SummaryMeterConverter());
        converterMap.put("TIMER", new TimerMeterConverter());
        /*converterMap.put("LONG_TASK_TIMER", new LongTimerMeterConverter());*/
    }

    public MeterConverter getConverter(Meter.Type type, Meter meter) {
        switch (type) {
            case COUNTER:
                if (meter instanceof FunctionCounter) {
                    return converterMap.get(FunctionCounterMeterConverter.TYPE);
                }
                return converterMap.get(type.name());
            case TIMER:
                if (meter instanceof FunctionTimer) {
                    return converterMap.get(FunctionTimerMeterConverter.TYPE);
                }
                return converterMap.get(type.name());
            default:
                return converterMap.get(type.name());
        }
    }
}
