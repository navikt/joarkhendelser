package no.nav.joarkjournalfoeringhendelser.consumer.kafka;

import static no.nav.joarkjournalfoeringhendelser.consumer.kafka.JournalpostStatus.INNGAAENDE;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.joarkjournalfoeringhendelser.metrics.Metrics;
import no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelse;
import no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelsePublisher;
import no.nav.joarkjournalfoeringhendelser.producer.JournalpostEndretInngaaendeHendelseMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Martin Burheim Tingstad, Visma Consulting.
 */
@Slf4j
@Component
public class JournalpostEndretListener {

	@Autowired
	private ConsumerRecordAsJsonConverter converter;

	@Autowired
	private InngaaendeHendelsePublisher publisher;

	@Autowired
	private MeterRegistry meterRegistry;

	@KafkaListener(topics = "${journalpostEndret.topic}")
	@Metrics(value = "dok_request", percentiles = {0.5, 0.95})
	public void onMessage(ConsumerRecord<?, ?> record) {
		long start = System.currentTimeMillis();
		try {
			JournalpostEndretEvent event = converter.convertRecordToEvent(record);

			if (event != null && INNGAAENDE.equalsIgnoreCase(event.getJournalpostType())) {
				InngaaendeHendelse hendelse = JournalpostEndretInngaaendeHendelseMapper.map(event);
				if (hendelse != null) {
					publisher.publish(hendelse);
					meterRegistry.counter("Inngaaendehendelser", "type", hendelse.getHendelsesType()).increment();
					log.info("Publisert hendelse " + hendelse.getHendelsesType() +
							" for journalpost " + hendelse.getJournalpostId() +
							(
								StringUtils.isEmpty(hendelse.getKanalReferanseId()) ? "" :
								(", kanalReferanseId " + hendelse.getKanalReferanseId())
							) +
							(
								StringUtils.isEmpty(hendelse.getMottaksKanal()) ? "" :
								(", mottaksKanal " + hendelse.getMottaksKanal())
							) +
							"."
					);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		log.debug("handling took " + (System.currentTimeMillis() - start) + " ms");
	}

}
