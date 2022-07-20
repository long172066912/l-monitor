package com.monitor.base.component.monitor.register;

import com.monitor.base.component.monitor.handler.LMonitorPushHandler;
import com.monitor.base.component.monitor.utils.LMeterRegistryCommonTagsUtil;
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
 * @Title: LJvmMeterRegistry
 * @Description: JVM监控统计工厂
 * @date 2022/3/29 10:31 AM
 */
@Slf4j
public class LJvmMeterRegistry extends LoggingMeterRegistry {

    private static LJvmMeterRegistry instance;

    private LMonitorPushHandler lMonitorPushHandler;

    private static final int TIME_SECOND = 10;

    private LJvmMeterRegistry(LMonitorPushHandler lMonitorPushHandler) {
        super(new LJvmLoggingRegistryConfig(), Clock.SYSTEM);
        this.lMonitorPushHandler = lMonitorPushHandler;
        this.getBaseTimeUnit();
    }

    /**
     * JVM10秒一次
     */
    private static class LJvmLoggingRegistryConfig implements LoggingRegistryConfig {
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
     * @param lMonitorPushHandler
     * @return
     */
    public static LJvmMeterRegistry getInstance(LMonitorPushHandler lMonitorPushHandler) {
        if (null == instance) {
            synchronized (LJvmMeterRegistry.class) {
                if (null == instance) {
                    instance = new LJvmMeterRegistry(lMonitorPushHandler);
                    instance.config().commonTags(LMeterRegistryCommonTagsUtil.buildCommonTags());
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
            getMeters().parallelStream().forEach(m -> lMonitorPushHandler.doPush(m, now));
        } catch (Exception e) {
            log.warn("lJvmMeterRegistry publish error !", e);
        }
    }

    @Override
    public void stop() {
        super.stop();
        lMonitorPushHandler.close();
    }
}