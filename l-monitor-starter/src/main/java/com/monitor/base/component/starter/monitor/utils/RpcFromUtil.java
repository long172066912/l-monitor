package com.monitor.base.component.starter.monitor.utils;

import com.google.common.base.Splitter;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: RequestFromUtil
 * @Description: 获取请求来源，拿header中的from
 * @date 2022/7/6 6:43 PM
 */
@Slf4j
public class RpcFromUtil {

    /**
     * RPC-client通过header传递来源
     */
    private static final String SYS_PARAM_FROM = "from";

    private static final Tag DEFAULT_FROM = Tag.of(SYS_PARAM_FROM, "UNKNOWN");

    private static Splitter splitter = Splitter.on(";");

    /**
     * 获取请求来源
     *
     * @param request
     * @return
     */
    public static Tag getFrom(HttpServletRequest request) {
        if (null == request) {
            return DEFAULT_FROM;
        }
        try {
            return Optional.ofNullable(request.getHeader(SYS_PARAM_FROM)).map(e -> Tag.of(SYS_PARAM_FROM, e)).orElse(DEFAULT_FROM);
        } catch (Exception e) {
            log.warn("RpcFromUtil getFrom fail !", e);
            return DEFAULT_FROM;
        }
    }
}
