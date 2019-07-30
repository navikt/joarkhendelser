package no.nav.joarkjournalfoeringhendelser.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class MetricUtils {

	private final MeterRegistry meterRegistry;

	public MetricUtils(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	public void incrementExceptionCounter(String exceptionName, String type) {
		meterRegistry.counter("dok_exception", "type", type, "exception_name", exceptionName).increment();

	}

}
