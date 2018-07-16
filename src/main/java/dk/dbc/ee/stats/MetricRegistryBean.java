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

import com.codahale.metrics.MetricRegistry;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @author Source (source (at) dbc.dk)
 */
final class MetricRegistryBean implements Bean<MetricRegistry>, PassivationCapable {

    private final HashSet<Annotation> qualifiers = new HashSet<>(
            Arrays.<Annotation>asList(
                    new AnnotationLiteral<Any>() {
                private static final long serialVersionUID = 3109256773218160485L;
            },
                    new AnnotationLiteral<Default>() {
                private static final long serialVersionUID = 3109256773218160486L;
            }));

    private final Set<Type> types;
    private final InjectionTarget<MetricRegistry> target;

    MetricRegistryBean(BeanManager manager) {
        AnnotatedType<MetricRegistry> annotatedType = manager.createAnnotatedType(MetricRegistry.class);
        this.types = annotatedType.getTypeClosure();
        this.target = manager.createInjectionTarget(annotatedType);
    }

    @Override
    public Class<?> getBeanClass() {
        return MetricRegistry.class;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.EMPTY_SET;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public MetricRegistry create(CreationalContext<MetricRegistry> creationalContext) {
        MetricRegistry reporter = target.produce(creationalContext);
        target.inject(reporter, creationalContext);
        target.postConstruct(reporter);
        creationalContext.push(reporter);
        return reporter;
    }

    @Override
    public void destroy(MetricRegistry instance, CreationalContext<MetricRegistry> creationalContext) {
        target.preDestroy(instance);
        target.dispose(instance);
        creationalContext.release();
    }

    @Override
    public Set<Type> getTypes() {
        return Collections.unmodifiableSet(types);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return Collections.unmodifiableSet(qualifiers);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ApplicationScoped.class;
    }

    @Override
    public String getName() {
        return "MetricRegistry";
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.EMPTY_SET;
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public String getId() {
        return "MetricRegistryBean";
    }
}
