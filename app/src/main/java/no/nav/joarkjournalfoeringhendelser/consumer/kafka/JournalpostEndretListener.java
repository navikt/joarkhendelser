package no.nav.joarkjournalfoeringhendelser.consumer.kafka;

import static no.nav.joarkjournalfoeringhendelser.consumer.kafka.JournalpostStatus.INNGAAENDE;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.joarkjournalfoeringhendelser.config.JoarkJournalfoeringHendelseTechnicalException;
import no.nav.joarkjournalfoeringhendelser.metrics.Metrics;
import no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelse;
import no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelsePublisher;
import no.nav.joarkjournalfoeringhendelser.producer.JournalpostEndretInngaaendeHendelseMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;

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

	private static final long DURATION = Duration.ofSeconds(20).toMillis();

	@KafkaListener(topics = "${journalpostEndret.topic}")
	@Metrics(value = "dok_request", percentiles = {0.5, 0.95})
	@Transactional
	public void onMessage(final ConsumerRecord<?, ?> record) {
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
		} catch (JoarkJournalfoeringHendelseTechnicalException e) {
			if(e.getCause() != null && e.getCause() instanceof TopicAuthorizationException) {
				log.warn("Failed to commit offset {} on partition {}", record.offset(), record.partition());
				log.warn("Waiting 20 seconds to try again");
				try {
					Thread.sleep(DURATION);
				} catch (InterruptedException e1) {}
				throw (TopicAuthorizationException) e.getCause();
			}
			throw e;
		} catch (Exception e) {
			log.error(String.format("Feil ved prosessering av endringsmelding: %s. Melding: %s", e.getMessage(), record), e);
		}
		log.debug("handling took " + (System.currentTimeMillis() - start) + " ms");
	}

}
