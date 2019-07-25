package no.nav.joarkjournalfoeringhendelser.producer;

import lombok.extern.slf4j.Slf4j;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import no.nav.joarkjournalfoeringhendelser.config.JoarkJournalfoeringHendelseTechnicalException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

/**
 * @author Martin Burheim Tingstad, Visma Consulting.
 */
@Slf4j
@Component
public class InngaaendeHendelsePublisher {

	@Autowired
	private KafkaTemplate<String, JournalfoeringHendelseRecord> kafkaTemplate;

	@Value("${journalfoeringHendelse-v1.topic}")
	private String topic;

	private static final long DURATION = Duration.ofSeconds(20).toMillis();

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
				hendelse.getTimestamp(),
				hendelse.getJournalpostId().toString(),
				record);

		ListenableFuture<SendResult<String, JournalfoeringHendelseRecord>> send =
				kafkaTemplate.send(producerRecord);

        try {
            SendResult<String, JournalfoeringHendelseRecord> sendResult = send.get();

            if(log.isDebugEnabled()) {
                log.info("Published to partittion " + sendResult.getRecordMetadata().partition());
                log.info("Published to offset " + sendResult.getRecordMetadata().offset());
                log.info("Published to offset " + sendResult.getRecordMetadata().topic());
            }
		} catch (ExecutionException e) {
        	if(e.getCause() != null && e.getCause() instanceof KafkaProducerException) {
				KafkaProducerException ee = (KafkaProducerException) e.getCause();
				if(ee.getCause() != null && ee.getCause() instanceof TopicAuthorizationException) {
					log.warn("Not authenticated to publish to topic '" + topic + "'", ee.getCause().getMessage());
					log.warn("Waiting 10 seconds to try again");
					try {
						Thread.sleep(DURATION);
					} catch (InterruptedException e1) {}
					throw new JoarkJournalfoeringHendelseTechnicalException("Not authenticated to publish to topic '" + topic + "'", ee.getCause());
				}
			}
        } catch (InterruptedException e) {
            log.warn("Failed to send message to kafka. Topic: " + topic, e.getMessage());
            throw new JoarkJournalfoeringHendelseTechnicalException("Failed to send message to kafka. Topic: " + topic, e);
		}
    }
}
