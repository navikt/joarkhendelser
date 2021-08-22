package no.nav.joarkhendelser;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.joarkhendelser.config.KafkaConfig;
import no.nav.joarkhendelser.metrics.DokMetricsAspect;
import no.nav.joarkhendelser.nais.NaisContract;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@ComponentScan
@Import(value = {
		KafkaConfig.class,
		NaisContract.class
})
@EnableAutoConfiguration
@EnableAspectJAutoProxy
public class ApplicationConfig {

	@Bean
	public DokMetricsAspect timedAspect(MeterRegistry meterRegistry) {
		return new DokMetricsAspect(meterRegistry);
	}

}
