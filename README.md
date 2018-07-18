# EE-Stats

An EJB to depend on in a ee-project, that supplies:

# Services provided

* a `jolokia.html` index file with stats (and pie chart) from the application
* `jolokia/` the [jolokia](http://jolokia.org/) servlet
* `jolokia/name` a text file providing the first found value of
    1. Environment variable `$JOLOKIA_NAME`
    1. Environment variable `$APPLICAITON_NAME`
    1. the application name as declared in the ee container
* Annotations to provide stats using [metric](https://metrics.dropwizard.io/)
    1. `@Counted` - simple counter
    1. `@Metered` - `@Counted` + Rate of calls
    1. `@Timed` - `@Metered` + durations of calls
    1. `@LifeCycleMetric` - class level annotation needed for the other 3
       to work ok `Constructor`, `@PostConstruct` and `@PreDestroy`. If this
       is needed but not supplied, an `EJBException` is thrown on startup. If
       it's present, but unneeded, an error is logged.

# How to provide stats

This is only for *Beans* example:

Simple bean invocations

    @Stateless
    @Path("foo")
    public class Foo {

        @GET
        @Path("now")
        @Produces(MediaType.TEXT_PLAIN)
        @dk.kosmisk.ee.stats.Timed // @Metered or @Counted
        public String now() {
            return Instant.now().toString();
        }
    }

LifeCycle measurements

    @Stateless
    @LifeCycleMetric
    public class Foo {

        @PostConstruct
        @Timed
        public void init() {
            // Something time consuming
            // like initialization of JavaScript environment
        }
    }

Remember measurements can only be made on bean-method invocations, ie.
`this.method()` cannot me measured.
One workaround is, Make a bean with business logic, and invoke the methods
needed to accomplish the task, this way you can measure which part of the
process that takes time.

# @Inject

You can inject:
 1. MetricRegistry
 1. Counter
 1. Meter
 1. Timer

Counter/Meter/Timer gets fully qualified name as JMX metric name unless annotated
with `@ExposeAs("name")`.

# Example application in example

[example](example/)

# How to depend on it

in `pom.xml`

    <dependencies>
        ...

        <dependency>
            <groupId>dk.kosmisk</groupId>
            <artifactId>ee-stats</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>javax</groupId>
            <artifactId>javaee-api</artifactId>
            <version>7.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
