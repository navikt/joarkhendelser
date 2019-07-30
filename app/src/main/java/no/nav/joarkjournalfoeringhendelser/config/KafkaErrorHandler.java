package no.nav.joarkjournalfoeringhendelser.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.joarkjournalfoeringhendelser.metrics.MetricUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.KafkaException;
import org.springframework.kafka.listener.ContainerAwareErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * @author Martin Burheim Tingstad, Visma Consulting
 */
@Component
@Slf4j
public class KafkaErrorHandler implements ContainerAwareErrorHandler {
	private final MetricUtils metricUtils;

	public KafkaErrorHandler(MetricUtils metricUtils) {
		this.metricUtils = metricUtils;
	}

	@Override
	public void handle(Exception e, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer, MessageListenerContainer container) {
		log.warn("KafkaContainer feilet med feilmelding={}", e.getMessage(), e);
		metricUtils.incrementExceptionCounter(e.getClass().getSimpleName(), "technical");
		if (e instanceof KafkaException) {
			try {
				log.warn("Venter for 20 sekunder før nytt forsøk med kobling mot topic {}", String.join(", ", container.getContainerProperties().getTopics()));
				Thread.sleep(Duration.ofSeconds(20).toMillis());
			} catch (InterruptedException ex) {
				log.error("Det skjedde en feil ved venting", e);
			}
		}
	}
}
