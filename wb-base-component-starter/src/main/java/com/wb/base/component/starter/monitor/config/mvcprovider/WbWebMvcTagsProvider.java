package com.wb.base.component.starter.monitor.config.mvcprovider;

import com.wb.base.component.starter.monitor.utils.ExceptionTagUtils;
import com.wb.base.component.starter.monitor.utils.RpcFromUtil;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTags;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: WbWebMvcTagsProvider
 * @Description: web接口tag构建器
 * @date 2022/5/5 10:57 AM
 */
public class WbWebMvcTagsProvider implements WebMvcTagsProvider {

    @Override
    public Iterable<Tag> getTags(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Throwable exception) {
        return Tags.of(WebMvcTags.method(request), WebMvcTags.uri(request, response),
                WebMvcTags.exception(exception), WebMvcTags.status(response), ExceptionTagUtils.buildExceptionCode(exception, response), RpcFromUtil.getFrom(request));
    }

    @Override
    public Iterable<Tag> getLongRequestTags(HttpServletRequest request, Object handler) {
        return Tags.of(WebMvcTags.method(request), WebMvcTags.uri(request, null), RpcFromUtil.getFrom(request));
    }
}