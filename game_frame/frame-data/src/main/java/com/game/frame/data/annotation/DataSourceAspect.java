package com.game.frame.data.annotation;

import com.game.frame.data.datasource.DynamicDataSourceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 数据源切换切面
 * @author lx
 * @date 2025/06/08
 */
@Aspect
@Component
public class DataSourceAspect {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceAspect.class);

    @Pointcut("@annotation(com.game.frame.data.annotation.DataSource)")
    public void dataSourcePointcut() {}

    @Around("dataSourcePointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        
        DataSource dataSource = AnnotationUtils.findAnnotation(method, DataSource.class);
        if (dataSource == null) {
            dataSource = AnnotationUtils.findAnnotation(method.getDeclaringClass(), DataSource.class);
        }
        
        if (dataSource != null) {
            DynamicDataSourceContext.setDataSourceType(dataSource.value());
            logger.debug("Switch to {} data source for method: {}", 
                    dataSource.value(), method.getName());
        }
        
        try {
            return point.proceed();
        } finally {
            DynamicDataSourceContext.clearDataSourceType();
        }
    }
}