package com.dam0.springbootelasticsearch.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

@Aspect
@Component
public class LogAspect {

    private final Logger LOGGER = LoggerFactory.getLogger(LogAspect.class);
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public LogAspect(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    // TODO 1: 메소드별 호출 횟수 카운트 ( + 호출자도 표시 )
    // TODO 2: 주기적으로 로그 삭제하는 스케줄러 필요함
    @Around("execution(* com.dam0.springbootelasticsearch.controller.*Controller.*(..))")
    public Object consoleLogging(ProceedingJoinPoint joinPoint) throws Throwable {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("## START - " + joinPoint.getSignature().getDeclaringTypeName() + " / " + joinPoint.getSignature().getName());
            LOGGER.debug("# REQUEST PARAMS : ");
            Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
            for (Map.Entry<String, String[]> e : parameterMap.entrySet()) {
                LOGGER.debug("# " + e.getKey() + " = " + Arrays.toString(e.getValue()) + " #");
            }
            LOGGER.debug("## JOIN POINT PROCEED ##");
        }

        Object result = joinPoint.proceed();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("# RESULT : " + result);
            LOGGER.debug("## FINISH - " + joinPoint.getSignature().getDeclaringTypeName() + " / " + joinPoint.getSignature().getName());
        }

        return result;
    }
}
