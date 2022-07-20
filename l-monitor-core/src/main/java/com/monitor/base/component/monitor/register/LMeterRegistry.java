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
 * @Title: LMeterRegistry
 * @Description: //TODO (用一句话描述该文件做什么)
 * @date 2022/3/29 10:31 AM
 */
@Slf4j
public class LMeterRegistry extends LoggingMeterRegistry {

    private static LMeterRegistry instance;

    private LMonitorPushHandler lMonitorPushHandler;

    private LMeterRegistry(LMonitorPushHandler lMonitorPushHandler) {
        super(LoggingRegistryConfig.DEFAULT, Clock.SYSTEM);
        this.lMonitorPushHandler = lMonitorPushHandler;
        this.getBaseTimeUnit();
    }

    /**
     * 获取单例工厂
     * @param lMonitorPushHandler
     * @return
     */
    public static LMeterRegistry getInstance(LMonitorPushHandler lMonitorPushHandler) {
        if (null == instance) {
            synchronized (LMeterRegistry.class) {
                if (null == instance) {
                    instance = new LMeterRegistry(lMonitorPushHandler);
                    instance.config().commonTags(LMeterRegistryCommonTagsUtil.buildCommonTags());
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
            getMeters().parallelStream().forEach(m -> lMonitorPushHandler.doPush(m, now));
        } catch (Exception e) {
            log.warn("lMeterRegistry publish error !", e);
        }
    }

    @Override
    public void stop() {
        super.stop();
        lMonitorPushHandler.close();
    }
}