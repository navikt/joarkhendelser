package no.nav.joarkjournalfoeringhendelser.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.AuthenticationException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.springframework.kafka.listener.ContainerAwareErrorHandler;
import org.springframework.kafka.listener.ContainerStoppingErrorHandler;
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

	private static final ContainerStoppingErrorHandler STOPPING_ERROR_HANDLER = new ContainerStoppingErrorHandler();

	public static final AtomicInteger authorizationErrorCounter = new AtomicInteger(0);
	private final MeterRegistry meterRegistry;

	public KafkaErrorHandler(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@Override
	public void handle(Exception e, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer, MessageListenerContainer container) {
		log.warn("KafkaContainer feilet med feilmelding={}", e.getMessage(), e);
		meterRegistry.counter("dok_exception", "type", "technical");
		if (e instanceof AuthenticationException || e instanceof AuthorizationException) {
			log.warn("Forsøker å restarte Kafka container for {}", String.join(", ", container.getContainerProperties().getTopics()));
			authorizationErrorCounter.incrementAndGet();
			scheduleRestart(e, records, consumer, container);
		}
	}

	@SuppressWarnings({"pmd:DoNotUseThreads", "fb-contrib:SEC_SIDE_EFFECT_CONSTRUCTOR"})
	private void scheduleRestart(Exception e, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer, MessageListenerContainer container) {
		new Thread(() -> {
			try {
				Thread.sleep(Duration.ofSeconds(10).toMillis());
				log.warn("Starter kafka container for {}", String.join(", ", container.getContainerProperties().getTopics()));
				container.start();
			} catch (Exception exception) {
				log.error("Feil oppstod ved venting og oppstart av kafka container", exception);
			}
		}).start();

		log.warn("Stopper kafka container for {}", String.join(", ", container.getContainerProperties().getTopics()));
		STOPPING_ERROR_HANDLER.handle(e, records, consumer, container);
	}
}
