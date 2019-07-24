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
	private static final long UNIT = Duration.ofSeconds(5).toMillis();

	private AtomicInteger counter = new AtomicInteger(1);

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
				Thread.sleep(UNIT * ((Double)(Math.pow(2, counter.getAndIncrement()))).longValue());
				log.warn("Forsøk {}: Starter kafka container for {}", counter.get(), topic);
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
