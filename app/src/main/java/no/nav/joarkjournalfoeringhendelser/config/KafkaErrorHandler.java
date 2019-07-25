package no.nav.joarkjournalfoeringhendelser.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.ContainerStoppingErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * @author Martin Burheim Tingstad, Visma Consulting
 */
@Component
@Slf4j
public class KafkaErrorHandler extends SeekToCurrentErrorHandler {

	private static final ContainerStoppingErrorHandler STOPPING_ERROR_HANDLER = new ContainerStoppingErrorHandler();
	private static final int TIMEOUT = 30;

	@Autowired
	KafkaErrorCounter counter;

	public KafkaErrorHandler() {
	    super(null, -1);
    }

	@Override
	public void handle(Exception e, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer, MessageListenerContainer container) {
		try {
			log.info("Venter i {} sekunder før vi prøver på nytt", TIMEOUT);
			Thread.sleep(Duration.ofSeconds(TIMEOUT).toMillis());
		} catch (Exception exception) {
			log.error("Feil oppstod ved venting i " + TIMEOUT + " sekunder", exception);
		}

		super.handle(e, records, consumer, container);

//		records.stream()
//				.map(ConsumerRecord::topic)
//				.findAny()
//				.ifPresent(topic -> scheduleRestart(e, records, consumer, container, topic));
	}

	@SuppressWarnings({"pmd:DoNotUseThreads", "fb-contrib:SEC_SIDE_EFFECT_CONSTRUCTOR"})
	private void scheduleRestart(Exception e, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer, MessageListenerContainer container, String topic) {
		new Thread(() -> {
			try {
				Thread.sleep(Duration.ofSeconds(5).toMillis());
				int retryNumber = counter.incrementAndGet();
				int sleepIntervalInSeconds = ((Double)(Math.pow(2, retryNumber))).intValue();
				log.info("Thread sleep for {} seconds", sleepIntervalInSeconds);
				Thread.sleep(Duration.ofSeconds(sleepIntervalInSeconds).toMillis());
				log.warn("Forsøk {}: Starter kafka container for {}", retryNumber, topic);
				container.start();
				Thread.sleep(Duration.ofSeconds(5).toMillis());
				if(container.isRunning()) {
					counter.reset();
				}
			} catch (Exception exception) {
				log.error("Feil oppstod ved venting og oppstart av kafka container", exception);
			}
		}).start();

		log.warn("Stopper kafka container for {}", topic);
		if(container.isRunning()) {
			STOPPING_ERROR_HANDLER.handle(e, records, consumer, container);
		}
	}
}
