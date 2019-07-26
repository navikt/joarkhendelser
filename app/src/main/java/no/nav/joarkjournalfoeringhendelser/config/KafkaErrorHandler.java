package no.nav.joarkjournalfoeringhendelser.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.KafkaException;
import org.springframework.kafka.listener.ContainerAwareErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Martin Burheim Tingstad, Visma Consulting
 */
@Component
@Slf4j
public class KafkaErrorHandler implements ContainerAwareErrorHandler {

	public static final AtomicInteger authorizationErrorCounter = new AtomicInteger(0);
	private final MeterRegistry meterRegistry;

	public KafkaErrorHandler(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@Override
	public void handle(Exception e, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer, MessageListenerContainer container) {
		log.warn("KafkaContainer feilet med feilmelding={}", e.getMessage(), e);
		meterRegistry.counter("dok_exception", "type", "technical", "exception_name", e.getClass().getSimpleName()).increment();
		if (e instanceof KafkaException) {
			authorizationErrorCounter.incrementAndGet();
			try {
				log.warn("Venter for 20 sekunder før nytt forsøk med kobling mot topic {}", String.join(", ", container.getContainerProperties().getTopics()));
				Thread.sleep(Duration.ofSeconds(20).toMillis());
			} catch (InterruptedException ex) {
				log.error("Feil oppstod ved venting", ex);
			}
		}
	}

}
