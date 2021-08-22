package no.nav.joarkhendelser.producer;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.joarkhendelser.exception.AuthenticationFailedExecption;
import no.nav.joarkhendelser.exception.JoarkJournalfoeringHendelseTechnicalException;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class InngaaendeHendelseProducer {

	private String topic;
	private KafkaTemplate<String, JournalfoeringHendelseRecord> kafkaTemplate;
	private MeterRegistry meterRegistry;

	@Inject
	public InngaaendeHendelseProducer(
			MeterRegistry meterRegistry,
			KafkaTemplate<String, JournalfoeringHendelseRecord> kafkaTemplate,
			@Value("${journalfoeringHendelse-v1.topic}")
			String topic
	) {
		this.meterRegistry = meterRegistry;
		this.kafkaTemplate = kafkaTemplate;
		this.topic = topic;
	}

	@Transactional
	public void publish(InngaaendeHendelse hendelse) throws JoarkJournalfoeringHendelseTechnicalException {
		JournalfoeringHendelseRecord record = new JournalfoeringHendelseRecord(
				hendelse.getHendelsesId(),
				hendelse.getVersjon(),
				hendelse.getHendelsesType(),
				hendelse.getJournalpostId(),
				hendelse.getJournalpostStatus(),
				hendelse.getTemaGammelt(),
				hendelse.getTemaNytt(),
				hendelse.getMottaksKanal(),
				hendelse.getKanalReferanseId(),
				hendelse.getBehandlingsTema()
		);

		ProducerRecord<String, JournalfoeringHendelseRecord> producerRecord = new ProducerRecord<>(
				topic,
				null,
				hendelse.getOperationTimestamp(),
				hendelse.getJournalpostId().toString(),
				record
		);

		ListenableFuture<SendResult<String, JournalfoeringHendelseRecord>> send = kafkaTemplate.send(producerRecord);

		try {
			SendResult<String, JournalfoeringHendelseRecord> sendResult = send.get();
			meterRegistry.timer(
					"journalfoeringhendelse_timer","tema",
					StringUtils.isEmpty(hendelse.getTemaNytt()) ? "UKJENT" : hendelse.getTemaNytt(),
					"mottaksKanal",
					StringUtils.isEmpty(hendelse.getMottaksKanal()) ? "UKJENT" : hendelse.getMottaksKanal()
			).record(
					hendelse.getOperationTimestamp() == null ? 0 : hendelse.getCurrentTimestamp() - hendelse.getOperationTimestamp(),
					TimeUnit.MILLISECONDS
			);

			log.info("Published to partition {}, offset {}, topic {}",
					sendResult.getRecordMetadata().partition(),
					sendResult.getRecordMetadata().offset(),
					sendResult.getRecordMetadata().topic()
			);
		} catch (ExecutionException e) {
			if (e.getCause() != null && e.getCause() instanceof KafkaProducerException) {
				KafkaProducerException ee = (KafkaProducerException) e.getCause();
				if (ee.getCause() != null && ee.getCause() instanceof TopicAuthorizationException) {
					throw new AuthenticationFailedExecption("Not authenticated to publish to topic '" + topic + "'", ee.getCause());
				}
			}
		} catch (InterruptedException e) {
			throw new JoarkJournalfoeringHendelseTechnicalException("Failed to send message to kafka. Topic: " + topic, e);
		}
	}
}
