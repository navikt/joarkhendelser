package no.nav.joarkhendelser.producer;

import lombok.extern.slf4j.Slf4j;
import no.nav.joarkhendelser.exception.AuthenticationFailedException;
import no.nav.joarkhendelser.exception.JoarkhendelserTechnicalException;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class InngaaendeHendelseProducer {

	private final String topic;
	private final KafkaTemplate<String, JournalfoeringHendelseRecord> kafkaTemplate;

	public InngaaendeHendelseProducer(
			KafkaTemplate<String, JournalfoeringHendelseRecord> kafkaTemplate,
			@Value("${journalfoeringhendelse.topic}") String topic
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.topic = topic;
	}

	@Transactional
	public void publish(InngaaendeHendelse hendelse) throws JoarkhendelserTechnicalException {
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

		log.info("Utgående melding med data: hendelsesId={}, versjon={}, hendelsesType={}, journalpostId={}, journalpostStatus={}, " +
						"temaGammelt={}, temaNytt={}, mottaksKanal={}, kanalReferanseId={}, behandlingsTema={}",
				record.getHendelsesId(), record.getVersjon(), record.getHendelsesType(), record.getJournalpostId(), record.getJournalpostStatus(),
				record.getTemaGammelt(), record.getTemaNytt(), record.getMottaksKanal(), record.getKanalReferanseId(), record.getBehandlingstema());

		ProducerRecord<String, JournalfoeringHendelseRecord> producerRecord = new ProducerRecord<>(topic, hendelse.getJournalpostId().toString(), record);

		CompletableFuture<SendResult<String, JournalfoeringHendelseRecord>> send = kafkaTemplate.send(producerRecord);

		try {
			SendResult<String, JournalfoeringHendelseRecord> sendResult = send.get();

			log.info("Publiserte melding til partition={}, offset={}, topic={}",
					sendResult.getRecordMetadata().partition(),
					sendResult.getRecordMetadata().offset(),
					sendResult.getRecordMetadata().topic()
			);
		} catch (TopicAuthorizationException e) {
			throw new AuthenticationFailedException("Ikke autentisert for å publisere til topic=" + topic, e.getCause());
		} catch (InterruptedException | ExecutionException e) {
			throw new JoarkhendelserTechnicalException("Feilet sending av Kafka-melding til topic=" + topic, e);
		}
	}
}
