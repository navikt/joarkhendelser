package no.nav.joarkhendelser.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEvent;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEventFilter;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEventMapper;
import no.nav.joarkhendelser.producer.InngaaendeHendelse;
import no.nav.joarkhendelser.producer.InngaaendeHendelseProducer;
import no.nav.joarkhendelser.producer.JournalpostEndretInngaaendeHendelseMapper;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.springframework.kafka.support.KafkaHeaders.OFFSET;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_PARTITION_ID;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
public class JournalpostEndretConsumer {

	private final JournalpostEndretEventConverter converter;
	private final InngaaendeHendelseProducer publisher;
	private final MeterRegistry meterRegistry;

	@Inject
	public JournalpostEndretConsumer(
			JournalpostEndretEventConverter converter,
			InngaaendeHendelseProducer publisher,
			MeterRegistry meterRegistry
	) {
		this.converter = converter;
		this.publisher = publisher;
		this.meterRegistry = meterRegistry;
	}

	@KafkaListener(topics = "${journalpostendret.topic}")
	@Transactional
	public void onMessage(
			@Payload String message,
			@Header(RECEIVED_TOPIC) String topic,
			@Header(RECEIVED_PARTITION_ID) int partition,
			@Header(OFFSET) int offset
	) throws JsonProcessingException {
		MDC.put("callId", UUID.randomUUID().toString());
		log.info("Innkommende Golden Gate-melding fra topic={}, partition={}, offset={}", topic, partition, offset);
		GoldenGateEvent goldenGateEvent = GoldenGateEventMapper.mapToEvent(message);

		if (GoldenGateEventFilter.shouldStopProcessingOfMessage(goldenGateEvent, topic, partition, offset)) {
			return;
		}

		JournalpostEndretEvent journalpostEndretEvent = converter.convertToEvent(goldenGateEvent, topic, partition, offset);

		if (journalpostEndretEvent != null) {
			InngaaendeHendelse hendelse = JournalpostEndretInngaaendeHendelseMapper.map(journalpostEndretEvent);
			if (hendelse != null) {
				publisher.publish(hendelse);
				meterRegistry.counter(
						"Inngaaendehendelser",
						"type", hendelse.getHendelsesType(),
						"tema", isEmpty(hendelse.getTemaNytt()) ? "UKJENT" : hendelse.getTemaNytt(),
						"mottakskanal", isEmpty(hendelse.getMottaksKanal()) ? "UKJENT" : hendelse.getMottaksKanal()).increment();

				log.info("Publisert hendelse={} for journalpostId={}, kanalreferanseId={}, og mottakskanal={}.",
						hendelse.getHendelsesType(),
						hendelse.getJournalpostId(),
						hendelse.getKanalReferanseId(),
						hendelse.getMottaksKanal());
			}
			journalfoeringHendelseTimer("databaseoppdateringer_goldengate_timer", journalpostEndretEvent.getFagomradeBefore(), journalpostEndretEvent.getMottaksKanal(),
					journalpostEndretEvent.getOperationTimestamp(), journalpostEndretEvent.getCurrentTimestamp());
		}
	}

	private void journalfoeringHendelseTimer(String timerNavn, String tema, String mottaksKanal, Long startTime, Long endTime) {
		long duration = (endTime == null || startTime == null) ? 0L : endTime - startTime;
		meterRegistry.timer(timerNavn,
				"tema", isEmpty(tema) ? "UKJENT" : tema, "mottaksKannal",
				isEmpty(mottaksKanal) ? "UKJENT" : mottaksKanal
		).record(duration, TimeUnit.MILLISECONDS);
	}
}
