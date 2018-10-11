package no.nav.joarkinngaaendehendelser;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.joarkinngaaendehendelser.config.KafkaConfig;
import no.nav.joarkinngaaendehendelser.metrics.DokMetricsAspect;
import no.nav.joarkinngaaendehendelser.nais.NaisContract;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan
@Import(value = {
		KafkaConfig.class,
		NaisContract.class
})
@EnableAutoConfiguration
public class ApplicationConfig {

	@Bean
	public DokMetricsAspect timedAspect(MeterRegistry meterRegistry) {
		return new DokMetricsAspect(meterRegistry);
	}

}
