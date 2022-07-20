package com.monitor.base.component.monitor.utils;

import com.monitor.base.component.constants.CommonConstants;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.apache.commons.lang3.StringUtils;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: LMeterRegistryCommonTagsUtil
 * @Description: //TODO (用一句话描述该文件做什么)
 * @date 2022/6/24 6:21 PM
 */
public class LMeterRegistryCommonTagsUtil {

    public static String podName = System.getenv(CommonConstants.HOST_NAME);
    public static String appName = System.getenv(CommonConstants.APP_NAME);
    public static String envName = System.getenv(CommonConstants.APP_ENV);

    /**
     * 获取公共tags
     *
     * @return
     */
    public static Iterable<Tag> buildCommonTags() {
        return Tags.of("envName", StringUtils.isBlank(envName) ? "UNKNOWN" : envName,
                "appName", StringUtils.isBlank(appName) ? "UNKNOWN" : appName,
                "podName", StringUtils.isBlank(podName) ? "UNKNOWN" : podName);
    }
}
