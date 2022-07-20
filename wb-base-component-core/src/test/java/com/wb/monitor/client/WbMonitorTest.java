package com.wb.monitor.client;

import com.wb.base.component.monitor.client.WbMonitor;
import com.wb.monitor.key.WbMonitorCounterKeys;
import com.wb.monitor.key.WbMonitorTimerKeys;
import org.junit.Test;

class WbMonitorTest {

    @Test
    void count() {
        /**
         * 计数类监控统计，每分钟统计一次，推送至Ops-Grafana监控平台，可在Ops后台进行
         */
        WbMonitor.count(WbMonitorCounterKeys.COUNTER_KEY_1, "tag-key", "tag-value");
    }

    @Test
    void start() {
        WbMonitor.WbTimer wbTimer = WbMonitor.start(WbMonitorTimerKeys.TIMER_KEY_1, "tag-key", "tag-value");
        //xxx
        wbTimer.stop();
        /**
         *
         */
        try (WbMonitor.WbTimer wbTimer1 = WbMonitor.start(WbMonitorTimerKeys.TIMER_KEY_1, "tag-key", "tag-value")) {
            /**
             * 业务逻辑1 xxx
             * 对 WbMonitorTimerKeys.TIMER_KEY_1 拼上 -segment1，监控统计 业务逻辑1
             */
            wbTimer1.split("-segment1");
            /**
             * 业务逻辑2 xxx
             * 对 WbMonitorTimerKeys.TIMER_KEY_1 拼上 -segment2，监控统计 业务逻辑2
             */
            wbTimer1.split("-segment2");
        }
    }

    @Test
    void timer() {


        WbMonitor.timer(() -> {
            //xxx
        }, WbMonitorTimerKeys.TIMER_KEY_1, "tag-key", "tag-value");
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            WbMonitor.count(() -> "tagsSizeTest", "tagKey", "tagValue" + i);
        }
        while (true) {

        }
    }
}