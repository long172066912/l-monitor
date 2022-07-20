package com.monitor.base.component.starter.db.mybatis.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.monitor.base.component.starter.db.mybatis.intercepter.MybatisMonitorInterceptor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.annotation.Resource;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: MicrometerConfig
 * @Description: monitor监控配置
 * @date 2022/3/29 3:16 PM
 */
@Configuration
public class MybatisMonitorApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private MybatisConfiguration mybatisConfiguration;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        mybatisConfiguration.addInterceptor(new MybatisMonitorInterceptor());
    }
}
