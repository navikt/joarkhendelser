package no.nav.joarkjournalfoeringhendelser;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.joarkjournalfoeringhendelser.config.KafkaConfig;
import no.nav.joarkjournalfoeringhendelser.metrics.DokMetricsAspect;
import no.nav.joarkjournalfoeringhendelser.nais.NaisContract;
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
