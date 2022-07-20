package com.wb.base.component.monitor.handler;

import com.wb.base.component.monitor.convert.MeterConverter;
import com.wb.base.component.monitor.convert.WbMeterConverterFactory;
import com.wb.base.component.monitor.model.WbMonitorStatisticsData;
import com.wb.base.component.monitor.push.AbstractWbMonitorPusher;
import io.micrometer.core.instrument.Meter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: WbMonitorPushHandler
 * @Description: //TODO (用一句话描述该文件做什么)
 * @date 2022/3/29 2:10 PM
 */
@Slf4j
public class WbMonitorPushHandler {

    private AbstractWbMonitorPusher wbMonitorPusher;
    private WbMeterConverterFactory converterFactory;

    public WbMonitorPushHandler(AbstractWbMonitorPusher wbMonitorPusher, WbMeterConverterFactory converterFactory) {
        this.wbMonitorPusher = wbMonitorPusher;
        this.converterFactory = converterFactory;
    }

    public void doPush(Meter meter, long timestamp) {
        //根据meter获取转换器
        MeterConverter<Meter, WbMonitorStatisticsData> converter = converterFactory.getConverter(meter.getId().getType(), meter);
        if (null == converter) {
            log.warn("WbMonitorPushHandler warning ! converter is null ! type : {}", meter.getId().getType().name());
            return;
        }
        //转换，并进行安全检测
        WbMonitorStatisticsData data = converter.convert(meter, timestamp);
        if (null == data || data.getData().size() == 0) {
            return;
        }
        //推送
        try {
            wbMonitorPusher.push(data);
        } catch (Exception e) {
            log.warn("WbMonitorPushHandler fail ! type : {}", meter.getId().getType().name(), e);
        }
    }

    public void close() {
        wbMonitorPusher.close();
    }
}
