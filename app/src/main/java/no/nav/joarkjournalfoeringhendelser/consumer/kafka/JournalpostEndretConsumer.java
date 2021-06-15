package no.nav.joarkjournalfoeringhendelser.consumer.kafka;

import static no.nav.joarkjournalfoeringhendelser.consumer.kafka.JournalpostStatus.INNGAAENDE;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.joarkjournalfoeringhendelser.metrics.Metrics;
import no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelse;
import no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelseProducer;
import no.nav.joarkjournalfoeringhendelser.producer.JournalpostEndretInngaaendeHendelseMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Martin Burheim Tingstad, Visma Consulting.
 */
@Slf4j
@Component
public class JournalpostEndretConsumer {

	@Autowired
	private ConsumerRecordAsJsonConverter converter;

	@Autowired
	private InngaaendeHendelseProducer publisher;

	@Autowired
	private MeterRegistry meterRegistry;

	@KafkaListener(topics = "${journalpostEndret.topic}")
	@Metrics(value = "dok_request", percentiles = {0.5, 0.95})
	@Transactional
	public void onMessage(final ConsumerRecord<?, ?> record) {
		log.info("Innkommende kafka record til topic: {}, partition: {}, offset: {}", record.topic(), record.partition(), record.offset());
		MDC.put("callId", UUID.randomUUID().toString());
		long start = System.currentTimeMillis();
		JournalpostEndretEvent event = converter.convertRecordToEvent(record);

		if (event != null && INNGAAENDE.equalsIgnoreCase(event.getJournalpostType())) {
			InngaaendeHendelse hendelse = JournalpostEndretInngaaendeHendelseMapper.map(event);
			if (hendelse != null) {
				publisher.publish(hendelse);
				meterRegistry.counter("Inngaaendehendelser", "type", hendelse.getHendelsesType(),
						"tema", Strings.isEmpty(hendelse.getTemaNytt()) ? "UKJENT" : hendelse.getTemaNytt(),
						"mottakskanal", Strings.isEmpty(hendelse.getMottaksKanal()) ? "UKJENT" : hendelse.getMottaksKanal()).increment();

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
			journalfoeringHendelseTimer("databaseoppdateringer_goldengate_timer", event.getFagomradeBefore(),event.getMottaksKanal(),
					event.getOperationTimestamp(), event.getCurrentTimestamp());
		}
		log.debug("handling took " + (System.currentTimeMillis() - start) + " ms");
	}

	private void journalfoeringHendelseTimer(String timerNavn, String tema, String mottaksKanal, Long startTime, Long endTime) {
		Long duration = (endTime == null || startTime == null) ? 0L : endTime - startTime;
		meterRegistry.timer(timerNavn, "tema", StringUtils.isEmpty(tema) ? "UKJENT" :
				tema,"mottaksKannal",StringUtils.isEmpty(mottaksKanal)?"UKJENT":mottaksKanal)
				.record(duration, TimeUnit.MILLISECONDS);
	}
}
