package no.nav.joarkhendelser.consumer;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEvent;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEventMapper;
import no.nav.joarkhendelser.producer.InngaaendeHendelse;
import no.nav.joarkhendelser.producer.InngaaendeHendelseProducer;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateEventFilter.shouldStopProcessingOfMessage;
import static no.nav.joarkhendelser.producer.JournalpostEndretInngaaendeHendelseMapper.map;
import static org.springframework.kafka.support.KafkaHeaders.OFFSET;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_PARTITION;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
public class JournalpostEndretConsumer {

	private final JournalpostEndretEventMapper mapper;
	private final InngaaendeHendelseProducer publisher;
	private final MeterRegistry meterRegistry;
	private final GoldenGateEventMapper goldenGateEventMapper;

	public JournalpostEndretConsumer(
			JournalpostEndretEventMapper mapper,
			InngaaendeHendelseProducer publisher,
			MeterRegistry meterRegistry,
			GoldenGateEventMapper goldenGateEventMapper
	) {
		this.mapper = mapper;
		this.publisher = publisher;
		this.meterRegistry = meterRegistry;
		this.goldenGateEventMapper = goldenGateEventMapper;
	}

	@KafkaListener(topics = "${journalpostendret.topic}")
	@Transactional
	public void onMessage(
			@Payload String message,
			@Header(RECEIVED_TOPIC) String topic,
			@Header(RECEIVED_PARTITION) int partition,
			@Header(OFFSET) int offset
	) {
		MDC.put("callId", UUID.randomUUID().toString());
		log.info("Golden Gate-melding mottatt fra topic={}, partition={}, offset={}", topic, partition, offset);

		GoldenGateEvent goldenGateEvent = goldenGateEventMapper.mapToEvent(message);
		if (goldenGateEvent == null) return;
		if (shouldStopProcessingOfMessage(goldenGateEvent)) return;

		JournalpostEndretEvent journalpostEndretEvent = mapper.mapToJournalpostEndretEvent(goldenGateEvent);

		if (journalpostEndretEvent != null) {
			InngaaendeHendelse hendelse = map(journalpostEndretEvent, goldenGateEvent);

			if (hendelse != null) {
				publisher.publish(hendelse);

				loggTypeTemaOgMottakskanal(hendelse);
				log.info("Har publisert hendelse med hendelsestype={} for journalpostId={} med kanalreferanseId={} og mottakskanal={}.",
						hendelse.getHendelsesType(),
						hendelse.getJournalpostId(),
						hendelse.getKanalReferanseId(),
						hendelse.getMottaksKanal());
			}

			loggTidsbrukFraJoarkTilPublisering(goldenGateEvent, journalpostEndretEvent);
		}
	}

	private void loggTypeTemaOgMottakskanal(InngaaendeHendelse hendelse) {
		meterRegistry.counter("joarkhendelse",
				"type", hendelse.getHendelsesType(),
				"tema", isEmpty(hendelse.getTemaNytt()) ? "UKJENT" : hendelse.getTemaNytt(),
				"mottakskanal", isEmpty(hendelse.getMottaksKanal()) ? "UKJENT" : hendelse.getMottaksKanal()).increment();
	}

	private void loggTidsbrukFraJoarkTilPublisering(GoldenGateEvent goldenGateEvent, JournalpostEndretEvent journalpostEndretEvent) {
		var tema = journalpostEndretEvent.getFagomradeBefore();
		var mottakskanal = journalpostEndretEvent.getMottaksKanal();
		long tidspunktForJoarkendring = goldenGateEvent.getOperationTimestamp().atZone(systemDefault()).toInstant().toEpochMilli();
		long tidspunktForPublisering = now().toEpochMilli();

		long tidMellomJoarkendringOgPublisering = tidspunktForPublisering - tidspunktForJoarkendring;

		meterRegistry.timer("tid_brukt_fra_endring_i_joark_til_joarkhendelser_publiserer_hendelse",
				"tema", isEmpty(tema) ? "UKJENT" : tema,
				"mottakskanal", isEmpty(mottakskanal) ? "UKJENT" : mottakskanal
		).record(tidMellomJoarkendringOgPublisering, MILLISECONDS);
	}
}
