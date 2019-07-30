package no.nav.joarkjournalfoeringhendelser.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * @author Martin Burheim Tingstad, Visma Consulting
 */
@Component("DefaultKafkaListenerErrorHandler")
@Slf4j
public class DefaultKafkaListenerErrorHandler implements KafkaListenerErrorHandler {

	private final MeterRegistry meterRegistry;

	public DefaultKafkaListenerErrorHandler(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@Override
	public Object handleError(Message<?> message, ListenerExecutionFailedException e) {
		log.warn("Kafka Listener feilet med feilmelding={}", e.getCause().getMessage(), e.getCause());
		meterRegistry.counter("dok_exception", "type", "technical", "exception_name", e.getCause().getClass().getSimpleName()).increment();
		throw e; //Må throwe videre for at retryTemplate skal kicke inn
	}

}
