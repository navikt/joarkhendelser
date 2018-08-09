package no.nav.dokarkivhendelser.consumer.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JournalpostEndretListener {

    @Autowired
    private ConsumerRecordToJournalpostEndretConverter converter;


    @KafkaListener(topics = "${journalpostEndret.topic}")
    public void onMessage(ConsumerRecord<?, byte[]> record) {
        long start = System.currentTimeMillis();
        try {
            log.info("Received event from topic: [{}]", record.topic());
            JournalpostEndretEvent event = converter.convert(record);
            log.info("Got event for journalpost: [{}]", event.getJournalpostId());
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("handling took " + (System.currentTimeMillis() - start) + " ms");
    }
}
