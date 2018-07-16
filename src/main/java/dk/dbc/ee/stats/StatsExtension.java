/*
 * Copyright (C) 2018 DBC A/S (http://dbc.dk/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.ee.stats;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJBException;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.interceptor.InvocationContext;

/**
 *
 * @author Source (source (at) dbc.dk)
 */
class StatsExtension implements Extension {

    private static final HashMap<Executable, String> mappedNames = new HashMap<>();

    private void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd) {
        bbd.addInterceptorBinding(Counted.class);
        bbd.addInterceptorBinding(Metered.class);
        bbd.addInterceptorBinding(Timed.class);
        bbd.addInterceptorBinding(LifeCycleMetric.class);
    }

    private void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager manager) {
        if (manager.getBeans(MetricRegistry.class).isEmpty()) {
            abd.addBean(new MetricRegistryBean(manager));
        }
    }

    private <T> void processAnnotatedType(@Observes @WithAnnotations({
        LifeCycleMetric.class, Counted.class, Metered.class, Timed.class}) ProcessAnnotatedType<T> processAnnotatedType) {
        AnnotatedType<T> type = processAnnotatedType.getAnnotatedType();
        Class<T> annotatedClass = type.getJavaClass();
        if (annotatedClass.equals(InterceptorForMetricLifeCycle.class) ||
            annotatedClass.equals(InterceptorForCount.class) ||
            annotatedClass.equals(InterceptorForMeter.class) ||
            annotatedClass.equals(InterceptorForTimer.class)) {
            return;
        }

        if (annotatedClass.isAnnotationPresent(Counted.class) ||
            annotatedClass.isAnnotationPresent(Metered.class) ||
            annotatedClass.isAnnotationPresent(Timed.class)) {
            throw new EJBException("Classes cannot be annotated with @Counted, @Metered or @Timed on " + annotatedClass.getTypeName());
        }

        boolean needLifeCycle = false;
        boolean isLifeCycle = type.isAnnotationPresent(LifeCycleMetric.class);
        for (AnnotatedMethod<? super T> method : type.getMethods()) {
            if (isAnnotated(method)) {
                String name = name(method.getJavaMember());
                mappedNames.put(method.getJavaMember(), name);
                if ((method.isAnnotationPresent(PostConstruct.class) ||
                     method.isAnnotationPresent(PreDestroy.class))) {
                    needLifeCycle = true;
                }
            }
        }
        for (AnnotatedConstructor<T> constructor : type.getConstructors()) {
            if (isAnnotated(constructor)) {
                String name = name(constructor.getJavaMember());
                mappedNames.put(constructor.getJavaMember(), name);
                needLifeCycle = true;
            }
        }
        if (needLifeCycle && !isLifeCycle) {
            throw new EJBException("Class should be annotated with @LifeCycleMetric at " + annotatedClass.getTypeName() + " for @PostConstruct/@PreDestroy/Constructor metric");
        }
        if (!needLifeCycle && isLifeCycle) {
            System.err.println("Class should not be annotated with @LifeCycleMetric at " + annotatedClass.getTypeName() + " has no @PostConstruct/@PreDestroy/Constructor metric");
        }
    }

    private <T> boolean isAnnotated(AnnotatedCallable<T> callable) {
        int count = 0;
        if (callable.isAnnotationPresent(Counted.class)) {
            count++;
        }
        if (callable.isAnnotationPresent(Metered.class)) {
            count++;
        }
        if (callable.isAnnotationPresent(Timed.class)) {
            count++;
        }
        if (count > 1) {
            throw new EJBException("Only one if @Counted, @Metered or @Timed on " + callable.getJavaMember());
        }
        return count == 1;
    }

    private void afterDeploymentValidation(@Observes AfterDeploymentValidation adv, BeanManager manager) {
        Bean<?> metricRegistryBean = manager.resolve(manager.getBeans(MetricRegistry.class));
        CreationalContext<?> metricRegistryCreationalContext = manager.createCreationalContext(metricRegistryBean);
        MetricRegistry registry = (MetricRegistry) manager.getReference(metricRegistryBean, MetricRegistry.class, metricRegistryCreationalContext);
        for (Map.Entry<Executable, String> entry : mappedNames.entrySet()) {
            Executable exec = entry.getKey();
            if (exec.isAnnotationPresent(Counted.class)) {
                Counter counter = registry.counter(entry.getValue());
                MetricReporter.WRAPPERS.put(exec, ic -> {
                                        counter.inc();
                                        return ic.proceed();
                                    });
            }
            if (exec.isAnnotationPresent(Metered.class)) {
                Meter meter = registry.meter(entry.getValue());
                MetricReporter.WRAPPERS.put(exec, ic -> {
                                        meter.mark();
                                        return ic.proceed();
                                    });
            }
            if (exec.isAnnotationPresent(Timed.class)) {
                Timer timer = registry.timer(entry.getValue());
                MetricReporter.WRAPPERS.put(exec, (InvocationContext ic) -> {
                                        try (Timer.Context time = timer.time()) {
                                            return ic.proceed();
                                        }
                                    });
            }
        }
        mappedNames.clear();
    }

    private static String name(Method method) {
        return declaredName(method).orElse(method.getDeclaringClass().getCanonicalName() + "." + method.getName());
    }

    private static String name(Constructor<?> constructor) {
        return declaredName(constructor).orElse(constructor.getDeclaringClass().getCanonicalName());
    }

    private static Optional<String> declaredName(Executable exec) {
        Counted counted = exec.getAnnotation(Counted.class);
        if (counted != null) {
            String value = counted.value();
            if (value != null && !value.isEmpty()) {
                return Optional.of(value);
            }
            return Optional.empty();
        }
        Metered metered = exec.getAnnotation(Metered.class);
        if (metered != null) {
            String value = metered.value();
            if (value != null && !value.isEmpty()) {
                return Optional.of(value);
            }
            return Optional.empty();
        }
        Timed timed = exec.getAnnotation(Timed.class);
        if (timed != null) {
            String value = timed.value();
            if (value != null && !value.isEmpty()) {
                return Optional.of(value);
            }
            return Optional.empty();
        }
        return Optional.empty();
    }

}
