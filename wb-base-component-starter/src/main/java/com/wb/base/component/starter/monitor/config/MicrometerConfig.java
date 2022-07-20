package com.wb.base.component.starter.monitor.config;

import com.wb.base.component.monitor.convert.WbMeterConverterFactory;
import com.wb.base.component.monitor.handler.WbMonitorPushHandler;
import com.wb.base.component.monitor.push.impl.DefaultWbOpsMonitorPusher;
import com.wb.base.component.monitor.register.WbJvmMeterRegistry;
import com.wb.base.component.monitor.register.WbMeterRegistry;
import com.wb.base.component.monitor.utils.WbMeterRegistryCommonTagsUtil;
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
    private static WbJvmMeterRegistry wbJvmMeterRegistry;

    static {
        wbJvmMeterRegistry = WbJvmMeterRegistry.getInstance(new WbMonitorPushHandler(new DefaultWbOpsMonitorPusher(), new WbMeterConverterFactory()));
        Metrics.addRegistry(wbJvmMeterRegistry);
    }

    @Bean
    @Primary
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        String appName = getAppName();
        log.info("wb monitor start , appname : {}", appName);
        return registry -> registry.config().commonTags("appname", appName, "podName", StringUtils.isBlank(podName) ? appName : podName);
    }

    private String getAppName() {
        String appName = "UNKNOWN".equals(WbMeterRegistryCommonTagsUtil.appName) ? environment.getProperty("spring.application.name") : WbMeterRegistryCommonTagsUtil.appName;
        appName = StringUtils.isBlank(appName) ? applicationContext.getApplicationName() : appName;
        appName = StringUtils.isNotBlank(appName) ? appName : "UNKNOWN";
        return appName;
    }

    @Bean
    @Primary
    MeterRegistry meterRegistry() {
        return WbMeterRegistry.getInstance(new WbMonitorPushHandler(new DefaultWbOpsMonitorPusher(), new WbMeterConverterFactory()));
    }

    @Bean
    @Primary
    public JvmGcMetrics jvmGcMetrics() {
        JvmGcMetrics jvmGcMetrics = new JvmGcMetrics();
        jvmGcMetrics.bindTo(wbJvmMeterRegistry);
        return jvmGcMetrics;
    }

    @Bean
    @Primary
    public JvmMemoryMetrics jvmMemoryMetrics() {
        JvmMemoryMetrics jvmMemoryMetrics = new JvmMemoryMetrics();
        jvmMemoryMetrics.bindTo(wbJvmMeterRegistry);
        return jvmMemoryMetrics;
    }

    @Bean
    @Primary
    public JvmThreadMetrics jvmThreadMetrics() {
        JvmThreadMetrics jvmThreadMetrics = new JvmThreadMetrics();
        jvmThreadMetrics.bindTo(wbJvmMeterRegistry);
        return jvmThreadMetrics;
    }

    @Bean
    @Primary
    public ClassLoaderMetrics classLoaderMetrics() {
        ClassLoaderMetrics classLoaderMetrics = new ClassLoaderMetrics();
        classLoaderMetrics.bindTo(wbJvmMeterRegistry);
        return classLoaderMetrics;
    }
}
