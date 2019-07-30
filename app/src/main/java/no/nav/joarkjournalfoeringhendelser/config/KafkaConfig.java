package no.nav.joarkjournalfoeringhendelser.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

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
			RetryTemplate retryTemplate,
			KafkaTransactionManager<?, ?> transactionManager) {

		ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setErrorHandler(errorHandler);
		transactionManager.setRollbackOnCommitFailure(true);
		factory.getContainerProperties().setAckOnError(false);
		factory.getContainerProperties().setTransactionManager(transactionManager);
		configurer.configure(factory, kafkaConsumerFactory);
		factory.setConcurrency(N_CONCURRENCY);
		factory.setRetryTemplate(retryTemplate);
		return factory;
	}

	@Bean
	public AdminClient kafkaAdminClient(KafkaProperties kafkaProperties){
		AdminClient kafkaAdminClient = AdminClient.create(kafkaProperties.buildAdminProperties());
		return kafkaAdminClient;

	}

	@Bean
	public RetryTemplate retryTemplate() {

		final ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
		backOffPolicy.setInitialInterval(1000);
		backOffPolicy.setMultiplier(2);

		final RetryTemplate template = new RetryTemplate();
		template.setRetryPolicy(new AlwaysRetryPolicy());
		template.setBackOffPolicy(backOffPolicy);

		return template;
	}



}
