package no.nav.joarkjournalfoeringhendelser.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.joarkjournalfoeringhendelser.metrics.MetricUtils;
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

	private final MetricUtils metricUtils;

	public DefaultKafkaListenerErrorHandler(MetricUtils metricUtils) {
		this.metricUtils = metricUtils;
	}

	@Override
	public Object handleError(Message<?> message, ListenerExecutionFailedException e) {
		log.warn("Kafka Listener feilet med feilmelding={}", e.getCause().getMessage(), e.getCause());
		metricUtils.incrementExceptionCounter(e.getCause().getClass().getSimpleName(), "technical");
		throw e; //Må throwe videre for at retryTemplate skal kicke inn
	}

}
