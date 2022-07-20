package com.wb.base.component.starter.monitor.config.advice;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: ApiResponseBodyAdviceHandler
 * @Description: 返回拦截
 * @date 2021/10/13 2:09 PM
 */
@ControllerAdvice
public class MonitorResponseBodyAdviceHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
//        TODO 此处获取包装类型的返回code
//        WbMonitor.count(WbMonitorCounterKeys.WB_RPC_HTTP_SERVER_REQUEST, "uri", request.getURI().getPath(), "code", "");
        return body;
    }
}
