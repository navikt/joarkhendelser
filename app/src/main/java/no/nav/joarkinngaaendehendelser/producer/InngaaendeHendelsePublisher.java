package no.nav.joarkinngaaendehendelser.producer;

import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.INNGAAENDE;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.JOURNALFORT;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.MIDLERTIDIG;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.MOTTATT;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.UKJENTBRUKER;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.UTGAR;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.OracleSchema.INSERT_OPERATION;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.OracleSchema.UPDATE_OPERATION;

import java.util.concurrent.ExecutionException;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostEndretEvent;

@Slf4j
@Component
public class InngaaendeHendelsePublisher {

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${inngaaendeJournalpostEndret.topic}")
    private String topic;

    public void publish(JournalpostEndretEvent event) {
        ProducerRecord<Object, Object> producerRecord = new ProducerRecord<>(
                topic,
                null,
                System.currentTimeMillis(),
                event.getJournalpostId(),
                map(event));
        try {
            kafkaTemplate.send(producerRecord).get();
        }
        catch (InterruptedException | ExecutionException e) {
            log.warn("Failed to send message to kafka. Topic: " + topic, e.getMessage());
        }

    }

    public InngaaendeHendelse map(JournalpostEndretEvent event) {
        InngaaendeHendelsesType inngaaendeHendelsesType = finnHendelsesType(event);

        return inngaaendeHendelsesType != null ?
                InngaaendeHendelse.builder()
                        .fagomradeAfter(event.getFagomradeAfter())
                        .fagomradeBefore(event.getFagomradeBefore())
                        .journalpostId(event.getJournalpostId())
                        .kanalReferanseId(event.getKanalReferanseId())
                        .mottaksKanal(event.getMottaksKanal())
                        .journalpostStatus(event.getJournalpostStatusAfter())
                        .hendelsesType(inngaaendeHendelsesType)
                        .build() : null;
    }

    private InngaaendeHendelsesType finnHendelsesType(JournalpostEndretEvent event) {
        if(INSERT_OPERATION.equalsIgnoreCase(event.getOperation()) &&
                INNGAAENDE.equalsIgnoreCase(event.getJournalposttype()) &&
                (MOTTATT.equalsIgnoreCase(event.getJournalpostStatusAfter()) || MIDLERTIDIG.equalsIgnoreCase(event.getJournalpostStatusAfter()))) {
            return InngaaendeHendelsesType.MIDLERTIDIG_JOURNALFORT;
        }
        if(UPDATE_OPERATION.equalsIgnoreCase(event.getOperation()) &&
                INNGAAENDE.equalsIgnoreCase(event.getJournalposttype()) &&
                StringUtils.isNotEmpty(event.getFagomradeBefore()) &&
                StringUtils.isNotEmpty(event.getFagomradeAfter()) &&
             !event.getFagomradeBefore().equalsIgnoreCase(event.getFagomradeAfter())) {
            return InngaaendeHendelsesType.TEMA_ENDRET;
        }
        if(INNGAAENDE.equalsIgnoreCase(event.getJournalposttype()) &&
                MIDLERTIDIG.equalsIgnoreCase(event.getJournalpostStatusBefore()) &&
                JOURNALFORT.equalsIgnoreCase(event.getJournalpostStatusAfter())) {
            return InngaaendeHendelsesType.ENDELIG_JOURNALFORT;
        }
        if(UPDATE_OPERATION.equalsIgnoreCase(event.getOperation()) &&
                INNGAAENDE.equalsIgnoreCase(event.getJournalposttype()) &&
                StringUtils.isNotEmpty(event.getJournalpostStatusBefore()) &&
                (UTGAR.equalsIgnoreCase(event.getJournalpostStatusAfter()) || UKJENTBRUKER.equalsIgnoreCase(event.getJournalpostStatusAfter()))) {
            return InngaaendeHendelsesType.JOURNALPOST_UTGATT;
        }
        return null;
    }

}
