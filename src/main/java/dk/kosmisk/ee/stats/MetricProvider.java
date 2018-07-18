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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.lang.reflect.Member;
import javax.ejb.Singleton;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

/**
 *
 * @author Source (source (at) kosmisk.dk)
 */
@Singleton
public class MetricProvider {

    @Inject
    MetricRegistry registry;

    @Produces
    public Counter makeCounter(InjectionPoint ip) {
        return registry.counter(makeName(ip));
    }

    @Produces
    public Meter makeMeter(InjectionPoint ip) {
        return registry.meter(makeName(ip));
    }

    @Produces
    public Timer makeTimer(InjectionPoint ip) {
        return registry.timer(makeName(ip));
    }

    static String makeName(InjectionPoint ip) {
        Annotated annotated = ip.getAnnotated();
        ExposedAs called = annotated.getAnnotation(ExposedAs.class);
        if (called != null && called.value() != null && !called.value().isEmpty()) {
            return called.value();
        }
        Member member = ip.getMember();
        return member.getDeclaringClass().getCanonicalName() + "." + member.getName();
    }
}
