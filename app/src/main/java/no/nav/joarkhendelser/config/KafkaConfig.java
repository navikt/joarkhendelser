package no.nav.joarkhendelser.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.time.Duration;

import static org.springframework.util.backoff.FixedBackOff.DEFAULT_INTERVAL;
import static org.springframework.util.backoff.FixedBackOff.UNLIMITED_ATTEMPTS;

@Slf4j
@Configuration
public class KafkaConfig {

	private final MeterRegistry meterRegistry;

	@Autowired
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
		log.error("Exception oppstått i joarkhendelser: {} kafka record til topic: {}, partition: {}, offset: {}, UUID: {} feilmelding={}",
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