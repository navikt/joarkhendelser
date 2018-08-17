package no.nav.dokarkivhendelser.consumer.kafka;

import static no.nav.dokarkivhendelser.metrics.MetricLabels.DOK_ARKIV_HENDELSE_JOURNALPOST_ENDRET;

import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivhendelser.metrics.Metrics;

@Slf4j
@Component
public class JournalpostEndretListener {

    @Autowired
    private ConsumerRecordToJournalpostEndretConverter converter;


    @KafkaListener(topics = "${journalpostEndret.topic}")
    @Metrics(value = DOK_ARKIV_HENDELSE_JOURNALPOST_ENDRET, percentiles = {0.5, 0.95}, logExceptions = false)
    public void onMessage(ConsumerRecord<?, Map> record) {
        long start = System.currentTimeMillis();
        try {
            log.debug("Received event from topic: [{}]", record.topic());
            JournalpostEndretEvent event = converter.convert(record);
            log.info("Got {}-operation for journalpostId:{}. Fagomrade {} og arkivtype {}",
                    event.getOperation(), event.getJournalpostId(), event.getFagomrade(), event.getJournalpostType());
            log.info("Columns changed: {}", event.getColumnsChanged().toString());
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.debug("handling took " + (System.currentTimeMillis() - start) + " ms");
    }
}
