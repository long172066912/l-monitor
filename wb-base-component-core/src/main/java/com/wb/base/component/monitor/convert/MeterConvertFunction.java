package com.wb.base.component.monitor.convert;

/**
* @Title: MeterConvertFunction
* @Description: 转换逻辑处理
* @author JerryLong
* @date 2022/3/31 11:56 AM
* @version V1.0
*/
@FunctionalInterface
public interface MeterConvertFunction<T, R> {
    /**
     * 执行转换逻辑
     *
     * @param meter
     * @return
     */
    R doConvert(AbstractMeterConverter.MeterInfo<T> meter);
}