package no.nav.joarkjournalfoeringhendelser.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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

	private AtomicInteger counter = new AtomicInteger(0);

	@Override
	public void handle(Exception e, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer, MessageListenerContainer container) {
		records.stream()
				.map(ConsumerRecord::topic)
				.findAny()
				.ifPresent(topic -> scheduleRestart(e, records, consumer, container, topic));
	}

	@SuppressWarnings({"pmd:DoNotUseThreads", "fb-contrib:SEC_SIDE_EFFECT_CONSTRUCTOR"})
	private void scheduleRestart(Exception e, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer, MessageListenerContainer container, String topic) {
		new Thread(() -> {
			try {
				long slow = Duration.ofHours(3).toMillis();
				long fast = Duration.ofSeconds(20).toMillis();
				Thread.sleep((counter.get() > 10) ? slow : fast * counter.getAndIncrement());
				log.warn("Starter kafka container for {}", topic);
				container.start();
				counter.set(0);
			} catch (Exception exception) {
				log.error("Feil oppstod ved venting og oppstart av kafka container", exception);
			}
		}).start();

		log.warn("Stopper kafka container for {}", topic);
		STOPPING_ERROR_HANDLER.handle(e, records, consumer, container);
	}
}
