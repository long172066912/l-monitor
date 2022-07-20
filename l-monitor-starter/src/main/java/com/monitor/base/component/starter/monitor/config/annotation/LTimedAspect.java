package com.monitor.base.component.starter.monitor.config.annotation;

import com.monitor.base.component.starter.monitor.utils.ExceptionTagUtils;
import com.monitor.base.component.starter.monitor.utils.ExpressionEvaluator;
import com.monitor.base.component.monitor.utils.MonitorKeyUtils;
import io.micrometer.core.annotation.Incubating;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.*;
import io.micrometer.core.lang.NonNullApi;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <p>
 * AspectJ aspect for intercepting types or methods annotated with {@link Timed @Timed}.<br>
 * The aspect supports programmatic customizations through constructor-injectable custom logic.
 * </p>
 * <p>
 * You might want to add tags programmatically to the {@link Timer}.<br>
 * In this case, the tags provider function (<code>Function&lt;ProceedingJoinPoint, Iterable&lt;Tag&gt;&gt;</code>) can help.
 * It receives a {@link ProceedingJoinPoint} and returns the {@link Tag}s that will be attached to the {@link Timer}.
 * </p>
 * <p>
 * You might also want to skip the {@link Timer} creation programmatically.<br>
 * One use-case can be having another component in your application that already processes the {@link Timed @Timed} annotation
 * in some cases so that {@code TimedAspect} should not intercept these methods. E.g.: Spring Boot does this for its controllers.
 * By using the skip predicate (<code>Predicate&lt;ProceedingJoinPoint&gt;</code>)
 * you can tell the {@code TimedAspect} when not to create a {@link Timer}.
 * <p>
 * Here's an example to disable {@link Timer} creation for Spring controllers:
 * </p>
 * <pre>
 * &#064;Bean
 * public TimedAspect timedAspect(MeterRegistry meterRegistry) {
 *     return new TimedAspect(meterRegistry, this::skipControllers);
 * }
 *
 * private boolean skipControllers(ProceedingJoinPoint pjp) {
 *     Class&lt;?&gt; targetClass = pjp.getTarget().getClass();
 *     return targetClass.isAnnotationPresent(RestController.class) || targetClass.isAnnotationPresent(Controller.class);
 * }
 * </pre>
 *
 * @author David J. M. Karlsen
 * @author Jon Schneider
 * @author Johnny Lim
 * @author Nejc Korasa
 * @author Jonatan Ivanov
 * @author JerryLong
 * @since 1.0.0
 */
@Aspect
@NonNullApi
@Incubating(since = "1.0.0")
public class LTimedAspect {
    private static final Predicate<ProceedingJoinPoint> DONT_SKIP_ANYTHING = pjp -> false;
    public static final String DEFAULT_METRIC_NAME = "method.timed";
    public static final String DEFAULT_EXCEPTION_TAG_VALUE = "none";

    /**
     * Tag key for an exception.
     *
     * @since 1.1.0
     */
    public static final String EXCEPTION_TAG = "exception";

    private final MeterRegistry registry;
    private final Function<ProceedingJoinPoint, Iterable<Tag>> tagsBasedOnJoinPoint;
    private final Predicate<ProceedingJoinPoint> shouldSkip;

    /**
     * Creates a {@code TimedAspect} instance with {@link Metrics#globalRegistry}.
     *
     * @since 1.2.0
     */
    public LTimedAspect() {
        this(Metrics.globalRegistry);
    }

    /**
     * Creates a {@code TimedAspect} instance with the given {@code registry}.
     *
     * @param registry Where we're going to register metrics.
     */
    public LTimedAspect(MeterRegistry registry) {
        this(registry, DONT_SKIP_ANYTHING);
    }

    /**
     * Creates a {@code TimedAspect} instance with the given {@code registry} and tags provider function.
     *
     * @param registry             Where we're going to register metrics.
     * @param tagsBasedOnJoinPoint A function to generate tags given a join point.
     */
    public LTimedAspect(MeterRegistry registry, Function<ProceedingJoinPoint, Iterable<Tag>> tagsBasedOnJoinPoint) {
        this(registry, tagsBasedOnJoinPoint, DONT_SKIP_ANYTHING);
    }

    /**
     * Creates a {@code TimedAspect} instance with the given {@code registry} and skip predicate.
     *
     * @param registry   Where we're going to register metrics.
     * @param shouldSkip A predicate to decide if creating the timer should be skipped or not.
     * @since 1.7.0
     */
    public LTimedAspect(MeterRegistry registry, Predicate<ProceedingJoinPoint> shouldSkip) {
        this(
                registry,
                pjp -> Tags.of("class", pjp.getStaticPart().getSignature().getDeclaringTypeName(),
                        "method", pjp.getStaticPart().getSignature().getName()),
                shouldSkip
        );
    }

    /**
     * Creates a {@code TimedAspect} instance with the given {@code registry}, tags provider function and skip predicate.
     *
     * @param registry             Where we're going to register metrics.
     * @param tagsBasedOnJoinPoint A function to generate tags given a join point.
     * @param shouldSkip           A predicate to decide if creating the timer should be skipped or not.
     * @since 1.7.0
     */
    public LTimedAspect(MeterRegistry registry, Function<ProceedingJoinPoint, Iterable<Tag>> tagsBasedOnJoinPoint, Predicate<ProceedingJoinPoint> shouldSkip) {
        this.registry = registry;
        this.tagsBasedOnJoinPoint = tagsBasedOnJoinPoint;
        this.shouldSkip = shouldSkip;
    }

    @Around("execution (@io.micrometer.core.annotation.Timed * *.*(..))")
    public Object timedMethod(ProceedingJoinPoint pjp) throws Throwable {
        if (shouldSkip.test(pjp)) {
            return pjp.proceed();
        }

        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Timed timed = method.getAnnotation(Timed.class);
        String[] tags = new String[timed.extraTags().length];
        if (timed == null) {
            method = pjp.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
            timed = method.getAnnotation(Timed.class);
        } else {
            if (null != timed.extraTags()) {
                for (int i = 0; i < timed.extraTags().length; i++) {
                    tags[i] = ExpressionEvaluator.getConditionValue(pjp, timed.extraTags()[i], String.class);
                }
            }
        }

        final String metricName = timed.value().isEmpty() ? DEFAULT_METRIC_NAME : timed.value();
        final boolean stopWhenCompleted = CompletionStage.class.isAssignableFrom(method.getReturnType());

        if (!timed.longTask()) {
            return processWithTimer(pjp, timed, metricName, stopWhenCompleted, tags);
        } else {
            return processWithLongTaskTimer(pjp, timed, metricName, stopWhenCompleted, tags);
        }
    }

    private Object processWithTimer(ProceedingJoinPoint pjp, Timed timed, String metricName, boolean stopWhenCompleted, String[] tags) throws Throwable {

        Timer.Sample sample = Timer.start(registry);

        if (stopWhenCompleted) {
            try {
                return ((CompletionStage<?>) pjp.proceed()).whenComplete((result, throwable) ->
                        record(pjp, timed, metricName, sample, getExceptionTag(throwable), tags, ExceptionTagUtils.SUCCESS_CODE));
            } catch (Exception ex) {
                record(pjp, timed, metricName, sample, ex.getClass().getSimpleName(), tags, ExceptionTagUtils.buildExceptionCode(ex).getValue());
                throw ex;
            }
        }

        String exceptionClass = DEFAULT_EXCEPTION_TAG_VALUE;
        String code = ExceptionTagUtils.SUCCESS_CODE;
        try {
            return pjp.proceed();
        } catch (Exception ex) {
            exceptionClass = ex.getClass().getSimpleName();
            code = ExceptionTagUtils.buildExceptionCode(ex).getValue();
            throw ex;
        } finally {
            record(pjp, timed, metricName, sample, exceptionClass, tags, code);
        }
    }

    private void record(ProceedingJoinPoint pjp, Timed timed, String metricName, Timer.Sample sample, String exceptionClass, String[] tags, String code) {
        try {
            sample.stop(Timer.builder(MonitorKeyUtils.buildMonitoryKey(metricName, Meter.Type.TIMER))
                    .description(timed.description().isEmpty() ? null : timed.description())
                    .tags(tags)
                    .tags(EXCEPTION_TAG, exceptionClass)
                    .tags(tagsBasedOnJoinPoint.apply(pjp))
                    .tags("code", code)
                    .publishPercentileHistogram(timed.histogram())
                    .publishPercentiles(timed.percentiles().length == 0 ? null : timed.percentiles())
                    .register(registry));
        } catch (Exception e) {
            // ignoring on purpose
        }
    }

    private String getExceptionTag(Throwable throwable) {

        if (throwable == null) {
            return DEFAULT_EXCEPTION_TAG_VALUE;
        }

        if (throwable.getCause() == null) {
            return throwable.getClass().getSimpleName();
        }

        return throwable.getCause().getClass().getSimpleName();
    }

    private Object processWithLongTaskTimer(ProceedingJoinPoint pjp, Timed timed, String metricName, boolean stopWhenCompleted, String[] tags) throws Throwable {

        Optional<LongTaskTimer.Sample> sample = buildLongTaskTimer(pjp, timed, metricName, tags).map(LongTaskTimer::start);

        if (stopWhenCompleted) {
            try {
                return ((CompletionStage<?>) pjp.proceed()).whenComplete((result, throwable) -> sample.ifPresent(this::stopTimer));
            } catch (Exception ex) {
                sample.ifPresent(this::stopTimer);
                throw ex;
            }
        }

        try {
            return pjp.proceed();
        } finally {
            sample.ifPresent(this::stopTimer);
        }
    }

    private void stopTimer(LongTaskTimer.Sample sample) {
        try {
            sample.stop();
        } catch (Exception e) {
            // ignoring on purpose
        }
    }

    /**
     * Secure long task timer creation - it should not disrupt the application flow in case of exception
     */
    private Optional<LongTaskTimer> buildLongTaskTimer(ProceedingJoinPoint pjp, Timed timed, String metricName, String[] tags) {
        try {
            return Optional.of(LongTaskTimer.builder(MonitorKeyUtils.buildMonitoryKey(metricName, Meter.Type.TIMER))
                    .description(timed.description().isEmpty() ? null : timed.description())
                    .tags(tags)
                    .tags(tagsBasedOnJoinPoint.apply(pjp))
                    .register(registry));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}