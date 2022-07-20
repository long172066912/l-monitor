package com.wb.monitor.push;

import com.github.phantomthief.collection.BufferTrigger;
import com.github.phantomthief.collection.impl.MultiIntervalTriggerStrategy;
import com.wb.base.component.monitor.model.MonitorBusinessType;
import com.wb.base.component.monitor.model.OpsMonitorStatisticsData;
import com.wb.base.component.monitor.model.WbMonitorStatisticsData;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

public class BufferTriggerTest {
    /**
     * 一次推送多少条
     */
    private static final Integer ONCE_PUSH_SIZE = 100;

    private static BufferTrigger<WbMonitorStatisticsData> BUFFER = BufferTrigger.<WbMonitorStatisticsData, List<WbMonitorStatisticsData>>simple()
            .name("wb-monitor-ops-buffer")
            .triggerStrategy(new MultiIntervalTriggerStrategy()
                    .on(10, SECONDS, 1)
                    .on(1, SECONDS, ONCE_PUSH_SIZE)
            )
            .consumer(set -> Optional.ofNullable(set).ifPresent(data -> doPush(data.stream().filter(e -> null != e).map(e -> e.getData()).flatMap(List::stream).collect(Collectors.toList()))))
            .setContainer(CopyOnWriteArrayList::new, List::add)
            .maxBufferCount(10000)
            .build();

    private static AtomicLong incr = new AtomicLong(0);

    public static void main(String[] args) {
        int n = 0;
        for (int i = 0; i < 100; i++) {
            int p = ThreadLocalRandom.current().nextInt(1, 10);
            ArrayList<OpsMonitorStatisticsData> list = new ArrayList<>();
            for (int j = 0; j < p; j++) {
                list.add(OpsMonitorStatisticsData.builder().measurement("test" + i + "-" + p).build());
                n ++;
            }
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BUFFER.enqueue(WbMonitorStatisticsData.builder().businessType(MonitorBusinessType.BUSINESS).data(list).build());
        }
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        BUFFER.manuallyDoTrigger();
        System.out.println(n == incr.get());
    }

    public static void doPush(List list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        System.out.println("推送：" + list.size());
        incr.addAndGet(list.size());
    }
}
