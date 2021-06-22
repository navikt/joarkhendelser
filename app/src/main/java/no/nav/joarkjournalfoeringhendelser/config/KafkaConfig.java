package no.nav.joarkjournalfoeringhendelser.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import static org.springframework.util.backoff.FixedBackOff.DEFAULT_INTERVAL;
import static org.springframework.util.backoff.FixedBackOff.UNLIMITED_ATTEMPTS;

import javax.inject.Inject;
import java.time.Duration;

@Slf4j
@EnableKafka
@Configuration
public class KafkaConfig {

	private final MeterRegistry meterRegistry;

	@Inject
	public KafkaConfig(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@Bean("kafkaListenerContainerFactory")
	ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerFactory(
			ConsumerFactory<Object, Object> kafkaConsumerFactory
	) {
		ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(kafkaConsumerFactory);
		factory.getContainerProperties()
				.setAuthorizationExceptionRetryInterval(Duration.ofSeconds(10L));

		factory.setConcurrency(1);
		factory.setErrorHandler(new SeekToCurrentErrorHandler(
				this::handleError,
				new FixedBackOff(DEFAULT_INTERVAL, UNLIMITED_ATTEMPTS)));
		return factory;
	}

	private void handleError(ConsumerRecord<?, ?> rec, Exception thr) {
		log.error("Exception oppstått i joarkjournalfoeringhendelser: {} kafka record til topic: {}, partition: {}, offset: {}, UUID: {} feilmelding={}",
				thr.getClass().getSimpleName(),
				rec.topic(),
				rec.partition(),
				rec.offset(),
				rec.key(),
				thr.getCause()
		);
		Throwable throwable = thr.getCause() == null ? thr : thr.getCause();
		String exceptionName = throwable.getClass().getSimpleName();

		meterRegistry.counter("dok_exception", "type", "technical", "exception_name", exceptionName)
				.increment();
	}
}