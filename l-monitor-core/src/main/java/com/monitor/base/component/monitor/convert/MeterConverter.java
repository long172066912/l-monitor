package com.monitor.base.component.monitor.convert;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: MeterConverter
 * @Description: 转换器接口
 * @date 2022/3/29 11:33 AM
 */
public interface MeterConverter<T, R> {

    /**
     * Meter转换
     *
     * @param meter
     * @param timestamp
     * @return
     */
    R convert(T  meter, long timestamp);
}
