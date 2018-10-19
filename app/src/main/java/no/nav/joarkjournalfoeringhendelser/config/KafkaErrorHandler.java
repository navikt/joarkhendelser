package no.nav.joarkjournalfoeringhendelser.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ContainerAwareErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Martin Burheim Tingstad, Visma Consulting
 */
@Component
public class KafkaErrorHandler implements ContainerAwareErrorHandler {

	@Override
	public void handle(Exception e, List<ConsumerRecord<?, ?>> list,
					   Consumer<?, ?> consumer,
					   MessageListenerContainer messageListenerContainer) {

	}
}
