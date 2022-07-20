package com.monitor.base.component.monitor.client;

import com.monitor.base.component.monitor.LMonitorCounterKey;
import com.monitor.base.component.monitor.LMonitorSamplerKey;
import com.monitor.base.component.monitor.LMonitorTimerKey;
import com.monitor.base.component.monitor.convert.LMeterConverterFactory;
import com.monitor.base.component.monitor.handler.LMonitorPushHandler;
import com.monitor.base.component.monitor.model.MonitorBusinessType;
import com.monitor.base.component.monitor.push.impl.DefaultOpsMonitorPusher;
import com.monitor.base.component.monitor.register.LJvmMeterRegistry;
import com.monitor.base.component.monitor.register.LMeterRegistry;
import com.monitor.base.component.monitor.utils.MonitorKeyUtils;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: LMonitor
 * @Description: Monitor监控客户端
 * @date 2022/3/29 11:29 AM
 */
@Slf4j
public class LMonitor {

    private static String podName = System.getenv("HOSTNAME");

    /**
     * 自增
     *
     * @param key
     * @param tags
     */
    public static void count(LMonitorCounterKey key, String... tags) {
        SingleMeterRegister.getMeterRegistry().counter(key.getKey(), buildIterableTags(tags)).increment();
    }

    /**
     * 自增
     *
     * @param key
     * @param tags
     */
    public static void count(LMonitorCounterKey key, int amount, String... tags) {
        SingleMeterRegister.getMeterRegistry().counter(key.getKey(), buildIterableTags(tags)).increment(amount);
    }


    /**
     * 自增
     *
     * @param key
     * @param tags
     */
    public static void count(LMonitorCounterKey key, Map<String, String> tags) {
        SingleMeterRegister.getMeterRegistry().counter(key.getKey(), addBusinessTypeTags(tags)).increment();
    }

    /**
     * 自增
     *
     * @param key
     * @param tags
     */
    public static void count(LMonitorCounterKey key, Map<String, String> tags, int amount) {
        SingleMeterRegister.getMeterRegistry().counter(key.getKey(), addBusinessTypeTags(tags)).increment(amount);
    }

    /**
     * 取样
     *
     * @param key
     * @param obj  数值对象
     * @param f    取值方法
     * @param tags
     * @param <T>
     * @return
     */
    public static <T> T sampler(LMonitorSamplerKey key, T obj, ToDoubleFunction<T> f, String... tags) {
        return SingleMeterRegister.getMeterRegistry().gauge(key.getKey(), buildIterableTags(tags), obj, f);
    }

    /**
     * 取样-map
     *
     * @param key
     * @param map
     * @param tags
     * @param <T>
     * @return
     */
    public static <T extends Map<?, ?>> T samplerMapSize(LMonitorSamplerKey key, T map, String... tags) {
        return SingleMeterRegister.getMeterRegistry().gaugeMapSize(key.getKey(), buildIterableTags(tags), map);
    }

    /**
     * 取样-集合
     *
     * @param key
     * @param collection
     * @param tags
     * @param <T>
     * @return
     */
    public static <T extends Collection<?>> T samplerCollectionSize(LMonitorSamplerKey key, T collection, String... tags) {
        return SingleMeterRegister.getMeterRegistry().gaugeCollectionSize(key.getKey(), buildIterableTags(tags), collection);
    }

    /**
     * 通过指定开始与结束，对代码块进行时间统计
     *
     * @param key
     * @param tags
     * @return
     */
    public static LTimer start(LMonitorTimerKey key, String... tags) {
        MeterRegistry meterRegistry = SingleMeterRegister.getMeterRegistry();
        Timer.Sample start = Timer.start(meterRegistry);
        return LTimer.builder().timer(Timer.builder(key.getKey()).tags(buildIterableTags(tags)).register(meterRegistry)).sample(start).stageSample(start).meterRegistry(meterRegistry).build();
    }

    /**
     * 通过闭包方式进行时间统计
     *
     * @param f
     * @param key
     * @param tags
     */
    public static void timer(Runnable f, LMonitorTimerKey key, String... tags) {
        SingleMeterRegister.getMeterRegistry().timer(key.getKey(), buildIterableTags(tags)).record(f);
    }

    /**
     * 进行时间统计
     *
     * @param amount 耗时
     * @param unit   时间单位
     * @param key
     * @param tags
     */
    public static void timer(long amount, TimeUnit unit, LMonitorTimerKey key, String... tags) {
        SingleMeterRegister.getMeterRegistry().timer(key.getKey(), buildIterableTags(tags)).record(amount, unit);
    }

    /**
     * 获取监控线程池
     *
     * @param executorServiceName
     * @param executorService
     * @return
     */
    public static ExecutorService getMonitorExecutorService(String executorServiceName, ExecutorService executorService) {
        return ExecutorServiceMetrics.monitor(SingleMeterRegister.getJvmMeterRegistry(), executorService, executorServiceName);
    }

    /**
     * 单例获取meter工厂
     */
    private static class SingleMeterRegister {

        static {
            build();
        }

        private static MeterRegistry meterRegistry;

        private static LJvmMeterRegistry jvmRegistry;

        private static void build() {
            //注册JVM自定义工厂
            LMeterRegistry lMeterRegistry = LMeterRegistry.getInstance(new LMonitorPushHandler(new DefaultOpsMonitorPusher(), new LMeterConverterFactory()));
            meterRegistry = lMeterRegistry;
            new ProcessorMetrics().bindTo(meterRegistry);

            //启动JVM自定义工厂
            LJvmMeterRegistry jvmMeterRegistry = LJvmMeterRegistry.getInstance(new LMonitorPushHandler(new DefaultOpsMonitorPusher(), new LMeterConverterFactory()));
            //绑定JVM相关监控
            new JvmGcMetrics().bindTo(jvmMeterRegistry);
            new JvmMemoryMetrics().bindTo(jvmMeterRegistry);
            new JvmThreadMetrics().bindTo(jvmMeterRegistry);
            new ClassLoaderMetrics().bindTo(jvmMeterRegistry);
            jvmRegistry = jvmMeterRegistry;
        }

        public static MeterRegistry getMeterRegistry() {
            return meterRegistry;
        }

        public static MeterRegistry getJvmMeterRegistry() {
            return jvmRegistry;
        }
    }

    public static MeterRegistry getLMeterRegistry() {
        return SingleMeterRegister.getMeterRegistry();
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LTimer implements Closeable {
        private Timer timer;
        private Timer.Sample sample;
        private Timer.Sample stageSample;
        private MeterRegistry meterRegistry;
        private static final String INTERVAL = "-";

        /**
         * 结束
         *
         * @return
         */
        public long stop() {
            return sample.stop(timer);
        }

        /**
         * 阶段结束
         *
         * @param stageName 阶段名，将会拼在Timer Key后面，作为新的Key
         * @return
         */
        public synchronized long split(String stageName) {
            if (StringUtils.isBlank(stageName)) {
                log.error("LTimer use error ! stageName is blank !");
                return -1;
            }
            //如果没拼上 - 则帮忙拼一个
            if (!stageName.startsWith(INTERVAL)) {
                stageName = INTERVAL + stageName;
            }
            Meter.Type type = timer.getId().getType();
            //结束上一阶段
            stageName = MonitorKeyUtils.buildMonitoryKey(MonitorKeyUtils.getMonitoryKey(timer.getId().getName(), type) + stageName, type);
            Timer stageTimer = Timer.builder(stageName).tags(timer.getId().getTags()).register(meterRegistry);
            //提交上一阶段
            long stop = stageSample.stop(stageTimer);
            //开始下一阶段
            stageSample = Timer.start(meterRegistry);
            return stop;
        }

        @Override
        public void close() {
            stop();
        }
    }

    private static List<Tag> addBusinessTypeTags(Map<String, String> tags) {
        tags.put(MonitorBusinessType.MONITOR_TYPE_KEY, MonitorBusinessType.BUSINESS.name());
        return tags.entrySet().stream().filter(e -> StringUtils.isNotBlank(e.getValue())).map(e -> new ImmutableTag(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    private static Iterable<Tag> buildIterableTags(String... tags) {
        String[] newTags = addBusinessTypeTags(tags);
        Set<Tag> tagSet = new HashSet<>();
        String key = "";
        String value = "";
        for (int i = 0; i < newTags.length; i++) {
            key = newTags[i];
            value = newTags[++i];
            if (StringUtils.isNotBlank(value)) {
                tagSet.add(Tag.of(key, value));
            }
        }
        return tagSet;
    }

    /**
     * 给tags添加类型
     *
     * @param tags
     * @return
     */
    private static String[] addBusinessTypeTags(String... tags) {
        Object[] newTags = ArrayUtils.add(tags, tags.length, MonitorBusinessType.MONITOR_TYPE_KEY);
        newTags = ArrayUtils.add(newTags, newTags.length, MonitorBusinessType.BUSINESS.name());
        return (String[]) newTags;
    }
}
