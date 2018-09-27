package no.nav.joarkinngaaendehendelser;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.joarkinngaaendehendelser.metrics.DokMetricsAspect;

@ComponentScan
@EnableAutoConfiguration
@EnableAspectJAutoProxy
public class ApplicationConfig {
    @Bean
    public DokMetricsAspect timedAspect(MeterRegistry meterRegistry) {
        return new DokMetricsAspect(meterRegistry);
    }

}
