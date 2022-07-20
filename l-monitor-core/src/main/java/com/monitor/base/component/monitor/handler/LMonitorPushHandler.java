package com.monitor.base.component.monitor.handler;

import com.monitor.base.component.monitor.convert.LMeterConverterFactory;
import com.monitor.base.component.monitor.convert.MeterConverter;
import com.monitor.base.component.monitor.model.LMonitorStatisticsData;
import com.monitor.base.component.monitor.push.AbstractLMonitorPusher;
import io.micrometer.core.instrument.Meter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: LMonitorPushHandler
 * @Description: //TODO (用一句话描述该文件做什么)
 * @date 2022/3/29 2:10 PM
 */
@Slf4j
public class LMonitorPushHandler {

    private AbstractLMonitorPusher lMonitorPusher;
    private LMeterConverterFactory converterFactory;

    public LMonitorPushHandler(AbstractLMonitorPusher lMonitorPusher, LMeterConverterFactory converterFactory) {
        this.lMonitorPusher = lMonitorPusher;
        this.converterFactory = converterFactory;
    }

    public void doPush(Meter meter, long timestamp) {
        //根据meter获取转换器
        MeterConverter<Meter, LMonitorStatisticsData> converter = converterFactory.getConverter(meter.getId().getType(), meter);
        if (null == converter) {
            log.warn("LMonitorPushHandler warning ! converter is null ! type : {}", meter.getId().getType().name());
            return;
        }
        //转换，并进行安全检测
        LMonitorStatisticsData data = converter.convert(meter, timestamp);
        if (null == data || data.getData().size() == 0) {
            return;
        }
        //推送
        try {
            lMonitorPusher.push(data);
        } catch (Exception e) {
            log.warn("LMonitorPushHandler fail ! type : {}", meter.getId().getType().name(), e);
        }
    }

    public void close() {
        lMonitorPusher.close();
    }
}
