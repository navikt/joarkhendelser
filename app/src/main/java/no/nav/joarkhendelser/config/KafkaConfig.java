package no.nav.joarkhendelser.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import static java.time.Duration.ofSeconds;
import static org.springframework.util.backoff.FixedBackOff.DEFAULT_INTERVAL;
import static org.springframework.util.backoff.FixedBackOff.UNLIMITED_ATTEMPTS;

@Slf4j
@Configuration
public class KafkaConfig {

	@Bean("kafkaListenerContainerFactory")
	ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerFactory(ConsumerFactory<Object, Object> kafkaConsumerFactory) {

		ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(kafkaConsumerFactory);
		factory.getContainerProperties().setAuthExceptionRetryInterval(ofSeconds(10L));

		factory.setConcurrency(1);
		var errorHandler = new DefaultErrorHandler(this::handleError, new FixedBackOff(DEFAULT_INTERVAL, UNLIMITED_ATTEMPTS));
		factory.setCommonErrorHandler(errorHandler);

		return factory;
	}

	private void handleError(ConsumerRecord<?, ?> record, Exception exception) {
		log.error("Håndtering av kafka-record på topic={}, partition={} og offset={} med key={} har feilet med feilmelding={}",
				record.topic(),
				record.partition(),
				record.offset(),
				record.key(),
				exception.getCause(),
				exception
		);
	}
}