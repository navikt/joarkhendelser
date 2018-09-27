package no.nav.joarkinngaaendehendelser.producer;

import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import lombok.extern.slf4j.Slf4j;
import no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostEndretEvent;
import no.nav.joarkinngaaendehendelser.producer.inngaaendejournalpost.Hendelse;

@Slf4j
public abstract class CommonInngaaendeEventProducer {

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    public abstract void publish(JournalpostEndretEvent event);

    protected void sendEventToTopic(JournalpostEndretEvent event, String topic) {
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

    protected Hendelse map(JournalpostEndretEvent event) {
        return
                Hendelse.builder()
                        .fagomradeAfter(event.getFagomradeAfter())
                        .fagomradeBefore(event.getFagomradeBefore())
                        .journalpostId(event.getJournalpostId())
                        .journalpostStatus(event.getJournalpostStatus()).build();
    }

}
