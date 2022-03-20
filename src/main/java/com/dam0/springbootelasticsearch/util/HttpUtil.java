package com.dam0.springbootelasticsearch.util;

import javax.servlet.http.HttpServletRequest;

public class HttpUtil {

    /**
     * 헤더 내 요청자 IP 추출
     */
    public static String getRemoteIp(HttpServletRequest httpServletRequest) {

        String remoteIp = httpServletRequest.getHeader("X-Forwarded-For");

        if (isNullOrEmpty(remoteIp))
            remoteIp = httpServletRequest.getHeader("Proxy-Client-IP");
        if (isNullOrEmpty(remoteIp))
            remoteIp = httpServletRequest.getHeader("WL-Proxy-Client-IP");
        if (isNullOrEmpty(remoteIp))
            remoteIp = httpServletRequest.getHeader("HTTP_CLIENT_IP");
        if (isNullOrEmpty(remoteIp))
            remoteIp = httpServletRequest.getHeader("HTTP_X_FORWARDED_FOR");
        if (isNullOrEmpty(remoteIp))
            remoteIp = httpServletRequest.getHeader("HeaderXRealIP");
        if (isNullOrEmpty(remoteIp))
            remoteIp = httpServletRequest.getHeader("realIP");
        if (isNullOrEmpty(remoteIp))
            remoteIp = httpServletRequest.getRemoteAddr();

        return remoteIp;
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || "".equals(value);
    }

}
