package no.nav.joarkinngaaendehendelser.config;

import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;

/**
 *
 * @author Martin Burheim Tingstad, Visma Consulting
 */
@EnableKafka
@Configuration
public class KafkaConfig {
    public static final String KAFKA_LISTENER_CONTAINER = "kafkaListenerContainer";

    @Bean("kafkaListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory,
            KafkaErrorHandler errorHandler,
            KafkaTransactionManager<?, ?> transactionManager) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setErrorHandler(errorHandler);
        factory.getContainerProperties().setTransactionManager(transactionManager);
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setConcurrency(6);
        return factory;
    }

}
