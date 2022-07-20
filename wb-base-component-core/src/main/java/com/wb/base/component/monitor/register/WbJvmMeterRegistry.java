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
 * @Description: JVM监控统计工厂
 * @date 2022/3/29 10:31 AM
 */
@Slf4j
public class WbJvmMeterRegistry extends LoggingMeterRegistry {

    private static WbJvmMeterRegistry instance;

    private WbMonitorPushHandler wbMonitorPushHandler;

    private static final int TIME_SECOND = 10;

    private WbJvmMeterRegistry(WbMonitorPushHandler wbMonitorPushHandler) {
        super(new WbJvmLoggingRegistryConfig(), Clock.SYSTEM);
        this.wbMonitorPushHandler = wbMonitorPushHandler;
        this.getBaseTimeUnit();
    }

    /**
     * JVM10秒一次
     */
    private static class WbJvmLoggingRegistryConfig implements LoggingRegistryConfig {
        @Override
        public Duration step() {
            return Duration.ofSeconds(TIME_SECOND);
        }

        @Override
        public String get(String key) {
            return null;
        }
    }

    /**
     * 获取单例工厂
     * @param wbMonitorPushHandler
     * @return
     */
    public static WbJvmMeterRegistry getInstance(WbMonitorPushHandler wbMonitorPushHandler) {
        if (null == instance) {
            synchronized (WbJvmMeterRegistry.class) {
                if (null == instance) {
                    instance = new WbJvmMeterRegistry(wbMonitorPushHandler);
                    instance.config().commonTags(WbMeterRegistryCommonTagsUtil.buildCommonTags());
                    instance.config().meterFilter(new MeterFilter() {
                        @Override
                        public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                            return DistributionStatisticConfig.builder()
                                    .expiry(Duration.ofSeconds(TIME_SECOND))
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
            log.warn("WbJvmMeterRegistry publish error !", e);
        }
    }

    @Override
    public void stop() {
        super.stop();
        wbMonitorPushHandler.close();
    }
}