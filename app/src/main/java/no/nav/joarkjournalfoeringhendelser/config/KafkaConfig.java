package no.nav.joarkjournalfoeringhendelser.config;

import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

/**
 * @author Martin Burheim Tingstad, Visma Consulting
 */
@EnableKafka
@Configuration
public class KafkaConfig {

	public static Integer N_CONCURRENCY = 6;

	@Bean("kafkaListenerContainerFactory")
	ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerFactory(
			ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
			ConsumerFactory<Object, Object> kafkaConsumerFactory,
			KafkaErrorHandler errorHandler,
			KafkaTransactionManager<?, ?> transactionManager) {

		ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setErrorHandler(errorHandler);
		factory.getContainerProperties().setTransactionManager(transactionManager);
		configurer.configure(factory, kafkaConsumerFactory);
		factory.setConcurrency(N_CONCURRENCY);
		factory.setAfterRollbackProcessor(new InfiniteRollbackProcessor());
		return factory;
	}

	@Bean
	public KafkaTransactionManager kafkaTransactionManager(ProducerFactory producerFactory) {
		KafkaTransactionManager manager = new KafkaTransactionManager(producerFactory);
		manager.setFailEarlyOnGlobalRollbackOnly(true);
		manager.setNestedTransactionAllowed(true);
		manager.setValidateExistingTransaction(true);
		manager.setRollbackOnCommitFailure(true);
		manager.setTransactionSynchronization(AbstractPlatformTransactionManager.SYNCHRONIZATION_ALWAYS);
		return manager;
	}

}
