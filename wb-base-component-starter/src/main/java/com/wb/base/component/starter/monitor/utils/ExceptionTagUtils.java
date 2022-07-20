package com.wb.base.component.starter.monitor.utils;

import io.micrometer.core.instrument.Tag;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.servlet.http.HttpServletResponse;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: ExceptionTagUtils
 * @Description: //TODO (用一句话描述该文件做什么)
 * @date 2022/5/11 10:15 AM
 */
public class ExceptionTagUtils {

    private static final String CODE = "code";

    public static final String SUCCESS_CODE = "0";

    private static final Tag DEFAULT_CODE = Tag.of(CODE, "SUCCESS");

    private static final Tag ERROR_CODE = Tag.of(CODE, "-1");

    private static final Tag METHOD_EXCEPTION_CODE = Tag.of(CODE, "PARAM_ERROR");

    /**
     * 构建业务异常code码
     *
     * @param exception
     * @return
     */
    public static Tag buildExceptionCode(Throwable exception) {
        if (null == exception) {
            return DEFAULT_CODE;
        }
        if (exception instanceof MethodArgumentNotValidException) {
            return METHOD_EXCEPTION_CODE;
        } else {
            return ERROR_CODE;
        }
    }

    /**
     * 构建业务服务异常code码
     *
     * @param exception
     * @return
     */
    public static Tag buildExceptionCode(Throwable exception, HttpServletResponse response) {
        if (null == exception) {
            return DEFAULT_CODE;
        }
        if (null != exception) {
            if (exception instanceof MethodArgumentNotValidException) {
                return METHOD_EXCEPTION_CODE;
            } else {
                return ERROR_CODE;
            }
        } else {
            return DEFAULT_CODE;
        }
    }
}
