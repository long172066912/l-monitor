package com.wb.base.component.starter.db.mybatis.intercepter;

import com.wb.base.component.monitor.client.WbMonitor;
import com.wb.base.component.starter.constant.WbMonitorTimerKeys;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;

import java.sql.Statement;


/**
 * @author JerryLong
 * @version V1.0
 * @Title: MybatisMonitorInterceptor
 * @Description: 拦截mybatis执行，增加监控
 * @date 2022/7/20 10:51 AM
 */
@Intercepts(value = {
        @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
})
public class MybatisMonitorInterceptor implements Interceptor {

    /**
     * 拦截目标对象的目标方法的执行
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        WbMonitor.WbTimer start = WbMonitor.start(WbMonitorTimerKeys.DB_METHOD, "method", invocation.getTarget().getClass().getSimpleName() + "." + invocation.getMethod().getName());
        try {
            return invocation.proceed();
        } finally {
            start.stop();
        }
    }
}