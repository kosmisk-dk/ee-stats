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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundConstruct;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 *
 * @author Source (source (at) kosmisk.dk)
 */
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 10)
@LifeCycleMetric
public class InterceptorForMetricLifeCycle {

    @Inject
    MetricReporter reporter;

    @AroundConstruct
    public Object aroundConstruct(InvocationContext ic) throws Exception {
        return reporter.invoke(ic.getConstructor(), ic);
    }

    @PostConstruct
    public void aroundPostConstruct(InvocationContext ic) throws Exception {
        reporter.invoke(ic.getMethod(), ic);
    }

    @PreDestroy
    public void aroundPreDestroy(InvocationContext ic) throws Exception {
        reporter.invoke(ic.getMethod(), ic);
    }

}
