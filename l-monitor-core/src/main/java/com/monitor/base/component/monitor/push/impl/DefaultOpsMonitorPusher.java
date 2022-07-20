package com.monitor.base.component.monitor.push.impl;

import com.alibaba.fastjson2.JSON;
import com.github.phantomthief.collection.BufferTrigger;
import com.github.phantomthief.collection.impl.MultiIntervalTriggerStrategy;
import com.google.common.collect.ImmutableMap;
import com.monitor.base.component.monitor.model.OpsMonitorConfigs;
import com.monitor.base.component.monitor.model.LMonitorStatisticsData;
import com.monitor.base.component.monitor.push.AbstractLMonitorPusher;
import com.monitor.base.component.monitor.utils.MonitorConfigUtils;
import com.monitor.base.component.monitor.utils.OkHttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: DefaultOpsMonitorPusher
 * @Description: //TODO (用一句话描述该文件做什么)
 * @date 2022/3/29 3:16 PM
 */
@Slf4j
public class DefaultOpsMonitorPusher extends AbstractLMonitorPusher {

    /**
     * 一次推送多少条
     */
    private static final Integer ONCE_PUSH_SIZE = 100;

    private static volatile boolean shutdown = false;

    private static BufferTrigger<LMonitorStatisticsData> BUFFER = BufferTrigger.<LMonitorStatisticsData, List<LMonitorStatisticsData>>simple()
            .name("l-monitor-ops-buffer")
            .triggerStrategy(new MultiIntervalTriggerStrategy()
                    .on(10, SECONDS, 1)
                    .on(1, SECONDS, ONCE_PUSH_SIZE)
            )
            .consumer(set -> Optional.ofNullable(set).ifPresent(data -> OpsMonitorPusher.doPush(data.stream().filter(e -> null != e).map(e -> e.getData()).flatMap(List::stream).collect(Collectors.toList()))))
            .setContainer(CopyOnWriteArrayList::new, List::add)
            .maxBufferCount(10000)
            .build();

    @Override
    public void push(LMonitorStatisticsData data) {
        if (shutdown) {
            return;
        }
        try {
            BUFFER.enqueue(data);
        } catch (Exception e) {
            log.warn("OpsMonitorPusher inner push error ! data : {}", data, e);
        }
    }

    @Override
    public void close() {
        if (log.isDebugEnabled()){
            log.debug("OpsMonitorPusher Buffer manuallyDoTrigger ! size : {}", BUFFER.getPendingChanges());
        }
        BUFFER.close();
        shutdown = true;
    }

    /**
     * 推送
     */
    private static class OpsMonitorPusher {

        public static void doPush(List list) {
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            Optional<OpsMonitorConfigs> configByBusinessTag = Optional.ofNullable(MonitorConfigUtils.getConfigByBusinessTag());
            configByBusinessTag.ifPresent(configs -> {
                sendOps(configs.getUri(), JSON.toJSONString(ImmutableMap.of("data", list)));
            });
        }

        private static void sendOps(String uri, String data) {
            if (StringUtils.isBlank(uri)) {
                log.debug("未找到OPS URL配置，LMonitor监控数据： {}", data);
                return;
            }
            OkHttpUtils.send(uri, data);
        }
    }
}
