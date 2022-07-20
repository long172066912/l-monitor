package com.wb.base.component.starter.monitor.config.wrapper;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import io.micrometer.core.instrument.internal.TimedExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: ExecutorMetricWapper
 * @Description: 对bean方式的线程池监控
 * @date 2022/5/9 1:35 PM
 */
@Slf4j
@Component
public class ExecutorMetricWrapper implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ExecutorService) {
            if (bean instanceof TimedExecutorService) {
                return bean;
            }
            log.info("Executor beanName:{} Has been registered to monitor", beanName);
            return ExecutorServiceMetrics.monitor(Metrics.globalRegistry, (ExecutorService) bean, beanName);
        }
        return bean;
    }
}