package com.wb.base.component.starter.monitor.utils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
* @Title: ExpressionEvaluator
* @Description: Spring EL表达式转换器
* @author JerryLong
* @date 2021/12/13 2:10 下午
* @version V1.0
*/
public class ExpressionEvaluator extends CachedExpressionEvaluator {
    private final ParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final Map conditionCache = new ConcurrentHashMap<>(64);
    private final Map<AnnotatedElementKey, Method> targetMethodCache = new ConcurrentHashMap<>(64);

    /**
     * 单例
     */
    private static class ExpressionEvaluatorSingle {
        private static ExpressionEvaluator evaluator = new ExpressionEvaluator();

        public static ExpressionEvaluator getExpressionEvaluator() {
            return evaluator;
        }
    }

    /**
     * 根据注解表达式转换成Spring动态对象
     * @param joinPoint 切面信息
     * @param conditionExpression 表达式
     * @param clazz 转换类型
     * @param <T>
     * @return
     */
    public static <T> T getConditionValue(JoinPoint joinPoint, String conditionExpression, Class<T> clazz) {
        try {
            ExpressionEvaluator evaluator = ExpressionEvaluatorSingle.getExpressionEvaluator();
            EvaluationContext evaluationContext = evaluator.createEvaluationContext(joinPoint.getTarget(), joinPoint.getTarget().getClass(), ((MethodSignature) joinPoint.getSignature()).getMethod(), joinPoint.getArgs());
            AnnotatedElementKey methodKey = new AnnotatedElementKey(((MethodSignature) joinPoint.getSignature()).getMethod(), joinPoint.getTarget().getClass());
            return evaluator.condition(conditionExpression, methodKey, evaluationContext, clazz);
        } catch (Exception e) {
            return (T) conditionExpression;
        }
    }

    protected EvaluationContext createEvaluationContext(Object object, Class targetClass, Method method, Object[] args) {
        Method targetMethod = getTargetMethod(targetClass, method);
        ExpressionRootObject root = new ExpressionRootObject(object, args);
        return new MethodBasedEvaluationContext(root, targetMethod, args, this.paramNameDiscoverer);
    }

    protected <T> T condition(String conditionExpression, AnnotatedElementKey elementKey, EvaluationContext evalContext, Class<T> clazz) {
        return getExpression(this.conditionCache, elementKey, conditionExpression).getValue(evalContext, clazz);
    }

    private Method getTargetMethod(Class targetClass, Method method) {
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
        Method targetMethod = this.targetMethodCache.get(methodKey);
        if (targetMethod == null) {
            targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
            if (targetMethod == null) {
                targetMethod = method;
            }
            this.targetMethodCache.put(methodKey, targetMethod);
        }
        return targetMethod;
    }

    private static class ExpressionRootObject {
        private final Object object;
        private final Object[] args;

        public ExpressionRootObject(Object object, Object[] args) {
            this.object = object;
            this.args = args;
        }

        public Object getObject() {
            return object;
        }

        public Object[] getArgs() {
            return args;
        }
    }
}
