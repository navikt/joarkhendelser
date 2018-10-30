package no.nav.joarkjournalfoeringhendelser.producer;

import lombok.extern.slf4j.Slf4j;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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

	public void publish(InngaaendeHendelse hendelse) {
		JournalfoeringHendelseRecord record = new JournalfoeringHendelseRecord(
				hendelse.getHendelsesId(),
				hendelse.getVersjon(),
				hendelse.getHendelsesType(),
				hendelse.getJournalpostId(),
				hendelse.getJournalpostStatus(),
				hendelse.getTemaGammelt(),
				hendelse.getTemaNytt(),
				hendelse.getMottaksKanal(),
				hendelse.getKanalReferanseId()
		);

		ProducerRecord<String, JournalfoeringHendelseRecord> producerRecord = new ProducerRecord<>(
				topic,
				null,
				hendelse.getTimestamp(),
				hendelse.getJournalpostId().toString(),
				record);

		try {
			kafkaTemplate.send(producerRecord).get();
		} catch (InterruptedException | ExecutionException e) {
			log.warn("Failed to send message to kafka. Topic: " + topic, e.getMessage());
		}

	}

}
