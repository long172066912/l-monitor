package com.wb.base.component.monitor.register;

import com.wb.base.component.monitor.handler.WbMonitorPushHandler;
import com.wb.base.component.monitor.utils.WbMeterRegistryCommonTagsUtil;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: WbLoggingMeterRegistry
 * @Description: //TODO (用一句话描述该文件做什么)
 * @date 2022/3/29 10:31 AM
 */
@Slf4j
public class WbMeterRegistry extends LoggingMeterRegistry {

    private static WbMeterRegistry instance;

    private WbMonitorPushHandler wbMonitorPushHandler;

    private WbMeterRegistry(WbMonitorPushHandler wbMonitorPushHandler) {
        super(LoggingRegistryConfig.DEFAULT, Clock.SYSTEM);
        this.wbMonitorPushHandler = wbMonitorPushHandler;
        this.getBaseTimeUnit();
    }

    /**
     * 获取单例工厂
     * @param wbMonitorPushHandler
     * @return
     */
    public static WbMeterRegistry getInstance(WbMonitorPushHandler wbMonitorPushHandler) {
        if (null == instance) {
            synchronized (WbMeterRegistry.class) {
                if (null == instance) {
                    instance = new WbMeterRegistry(wbMonitorPushHandler);
                    instance.config().commonTags(WbMeterRegistryCommonTagsUtil.buildCommonTags());
                    instance.config().meterFilter(new MeterFilter() {
                        @Override
                        public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                            return DistributionStatisticConfig.builder()
                                    //百分位
                                    .percentiles(0.5, 0.90, 0.95, 0.99, 0.999)
                                    .expiry(Duration.ofSeconds(60))
                                    .build().merge(config);
                        }
                    });
                }
            }
        }
        return instance;
    }

    @Override
    protected void publish() {
        try {
            long now = System.currentTimeMillis();
            getMeters().parallelStream().forEach(m -> wbMonitorPushHandler.doPush(m, now));
        } catch (Exception e) {
            log.warn("WbMeterRegistry publish error !", e);
        }
    }

    @Override
    public void stop() {
        super.stop();
        wbMonitorPushHandler.close();
    }
}