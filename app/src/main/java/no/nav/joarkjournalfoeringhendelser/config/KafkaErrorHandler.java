package no.nav.joarkjournalfoeringhendelser.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.ContainerAwareErrorHandler;
import org.springframework.kafka.listener.ContainerStoppingErrorHandler;
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

	private static final ContainerStoppingErrorHandler STOPPING_ERROR_HANDLER = new ContainerStoppingErrorHandler();

	@Autowired
	KafkaErrorCounter counter;

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
				int retryNumber = counter.incrementAndGet();
				int sleepIntervalInSeconds = ((Double)(Math.pow(2, retryNumber))).intValue();
				log.info("Thread sleep for {} seconds", sleepIntervalInSeconds);
				Thread.sleep(Duration.ofSeconds(sleepIntervalInSeconds).toMillis());
				log.warn("Forsøk {}: Starter kafka container for {}", retryNumber, topic);
				container.start();
				counter.reset();
			} catch (Exception exception) {
				log.error("Feil oppstod ved venting og oppstart av kafka container", exception);
			}
		}).start();

		log.warn("Stopper kafka container for {}", topic);
		STOPPING_ERROR_HANDLER.handle(e, records, consumer, container);
	}
}
