/*
 * Copyright (C) 2018 Source (source (at) kosmisk.dk)
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
package dk.kosmisk.ee.stats;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import java.lang.reflect.Executable;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;

/**
 *
 * @author Source (source (at) kosmisk.dk)
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class MetricReporter {

    @Inject
    private MetricRegistry registry;

    private JmxReporter reporter;

    final static HashMap<Executable, MethodWrapper> WRAPPERS = new HashMap<>();

    @PostConstruct
    public void init() {
        this.reporter = JmxReporter.forRegistry(registry)
                .inDomain("metrics")
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start();
    }

    @PreDestroy
    public void destroy() {
        reporter.stop();
    }

    public Object invoke(Executable exec, InvocationContext ic) throws Exception {
        return WRAPPERS.getOrDefault(exec, DEFAULT_METHOD_WRAPPER).invoke(ic);
    }

    @FunctionalInterface
    interface MethodWrapper {

        Object invoke(InvocationContext ic) throws Exception;
    }
    MethodWrapper DEFAULT_METHOD_WRAPPER = (ic) -> ic.proceed();
}
