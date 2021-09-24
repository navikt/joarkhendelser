package no.nav.joarkhendelser.producer;

import lombok.extern.slf4j.Slf4j;
import no.nav.joarkhendelser.exception.AuthenticationFailedExecption;
import no.nav.joarkhendelser.exception.JoarkJournalfoeringHendelseTechnicalException;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class InngaaendeHendelseProducer {

	private final String topic;
	private final KafkaTemplate<String, JournalfoeringHendelseRecord> kafkaTemplate;

	@Autowired
	public InngaaendeHendelseProducer(
			KafkaTemplate<String, JournalfoeringHendelseRecord> kafkaTemplate,
			@Value("${journalfoeringhendelse.topic}")
			String topic
	) {
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
				hendelse.getJournalpostId().toString(),
				record
		);

		ListenableFuture<SendResult<String, JournalfoeringHendelseRecord>> send = kafkaTemplate.send(producerRecord);

		try {
			SendResult<String, JournalfoeringHendelseRecord> sendResult = send.get();

			log.info("Publiserte til partition={}, offset={}, topic={}",
					sendResult.getRecordMetadata().partition(),
					sendResult.getRecordMetadata().offset(),
					sendResult.getRecordMetadata().topic()
			);
		} catch (ExecutionException e) {
			if (e.getCause() != null && e.getCause() instanceof KafkaProducerException) {
				KafkaProducerException ee = (KafkaProducerException) e.getCause();
				if (ee.getCause() != null && ee.getCause() instanceof TopicAuthorizationException) {
					throw new AuthenticationFailedExecption("Ikke autentisert for å publisere til topic=" + topic, ee.getCause());
				}
			}
		} catch (InterruptedException e) {
			throw new JoarkJournalfoeringHendelseTechnicalException("Feilet sending av Kafka-melding til topic=" + topic, e);
		}
	}
}
