package com.monitor.base.component.starter.monitor.config;

import com.monitor.base.component.monitor.convert.LMeterConverterFactory;
import com.monitor.base.component.monitor.handler.LMonitorPushHandler;
import com.monitor.base.component.monitor.push.impl.DefaultOpsMonitorPusher;
import com.monitor.base.component.monitor.register.LJvmMeterRegistry;
import com.monitor.base.component.monitor.register.LMeterRegistry;
import com.monitor.base.component.monitor.utils.LMeterRegistryCommonTagsUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: MicrometerConfig
 * @Description: monitor监控配置
 * @date 2022/3/29 3:16 PM
 */
@Configuration
@Slf4j
public class MicrometerConfig {

    private static String podName = System.getenv("HOSTNAME");

    @Autowired
    private Environment environment;
    @Autowired
    private ApplicationContext applicationContext;
    /**
     * JVM工厂
     */
    private static LJvmMeterRegistry lJvmMeterRegistry;

    static {
        lJvmMeterRegistry = LJvmMeterRegistry.getInstance(new LMonitorPushHandler(new DefaultOpsMonitorPusher(), new LMeterConverterFactory()));
        Metrics.addRegistry(lJvmMeterRegistry);
    }

    @Bean
    @Primary
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        String appName = getAppName();
        log.info("l monitor start , appname : {}", appName);
        return registry -> registry.config().commonTags("appname", appName, "podName", StringUtils.isBlank(podName) ? appName : podName);
    }

    private String getAppName() {
        String appName = "UNKNOWN".equals(LMeterRegistryCommonTagsUtil.appName) ? environment.getProperty("spring.application.name") : LMeterRegistryCommonTagsUtil.appName;
        appName = StringUtils.isBlank(appName) ? applicationContext.getApplicationName() : appName;
        appName = StringUtils.isNotBlank(appName) ? appName : "UNKNOWN";
        return appName;
    }

    @Bean
    @Primary
    MeterRegistry meterRegistry() {
        return LMeterRegistry.getInstance(new LMonitorPushHandler(new DefaultOpsMonitorPusher(), new LMeterConverterFactory()));
    }

    @Bean
    @Primary
    public JvmGcMetrics jvmGcMetrics() {
        JvmGcMetrics jvmGcMetrics = new JvmGcMetrics();
        jvmGcMetrics.bindTo(lJvmMeterRegistry);
        return jvmGcMetrics;
    }

    @Bean
    @Primary
    public JvmMemoryMetrics jvmMemoryMetrics() {
        JvmMemoryMetrics jvmMemoryMetrics = new JvmMemoryMetrics();
        jvmMemoryMetrics.bindTo(lJvmMeterRegistry);
        return jvmMemoryMetrics;
    }

    @Bean
    @Primary
    public JvmThreadMetrics jvmThreadMetrics() {
        JvmThreadMetrics jvmThreadMetrics = new JvmThreadMetrics();
        jvmThreadMetrics.bindTo(lJvmMeterRegistry);
        return jvmThreadMetrics;
    }

    @Bean
    @Primary
    public ClassLoaderMetrics classLoaderMetrics() {
        ClassLoaderMetrics classLoaderMetrics = new ClassLoaderMetrics();
        classLoaderMetrics.bindTo(lJvmMeterRegistry);
        return classLoaderMetrics;
    }
}
