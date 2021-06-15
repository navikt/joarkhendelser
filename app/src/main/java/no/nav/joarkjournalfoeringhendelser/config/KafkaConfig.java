package no.nav.joarkjournalfoeringhendelser.config;

import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;

import java.time.Duration;

@EnableKafka
@Configuration
public class KafkaConfig {

	@Bean("kafkaListenerContainerFactory")
	ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerFactory(
			ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
			ConsumerFactory<Object, Object> kafkaConsumerFactory,
			KafkaErrorHandler errorHandler,
			KafkaTransactionManager<?, ?> transactionManager
	) {
		ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConcurrency(4);
		factory.setErrorHandler(errorHandler);
		factory.getContainerProperties().setTransactionManager(transactionManager);
		factory.getContainerProperties()
				.setAuthorizationExceptionRetryInterval(Duration.ofSeconds(10L));

		configurer.configure(factory, kafkaConsumerFactory);
		return factory;
	}

}
