package com.monitor.base.component.starter.monitor.config.annotation;

import com.monitor.base.component.monitor.model.MonitorBusinessType;
import com.monitor.base.component.starter.monitor.config.mvcprovider.WebMvcTagsProvider;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;

import java.util.function.Function;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: PrometheusAspectConfig
 * @Description: 支持monitor注解解析
 * @date 2022/3/22 4:25 PM
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class MicrometerAspectConfig {

    public static final String FUNCTION_NAME_KEY = "functionName";

    @Bean
    @Primary
    public LTimedAspect timedAspect(MeterRegistry registry) {
        return new LTimedAspect(registry, this.getDefaultTags());
    }

    @Bean
    @Primary
    public LCountedAspect countedAspect(MeterRegistry registry) {
        return new LCountedAspect(registry, this.getDefaultTags());
    }

    private Function<ProceedingJoinPoint, Iterable<Tag>> getDefaultTags() {
        return pjp -> Tags.of(
                MonitorBusinessType.MONITOR_TYPE_KEY, MonitorBusinessType.BUSINESS.name(),
                FUNCTION_NAME_KEY, pjp.getStaticPart().getSignature().getDeclaringType().getSimpleName() + "." + pjp.getStaticPart().getSignature().getName()
        );
    }

    @Bean
    public org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsProvider webMvcTagsProvider() {
        return new WebMvcTagsProvider();
    }
}
