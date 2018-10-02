package no.nav.joarkinngaaendehendelser.producer;

import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import lombok.extern.slf4j.Slf4j;
import no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostEndretEvent;

@Slf4j
public class InngaaendeHendelsePublisher {

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${inngaaendeJournalpost.topic}")
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

    private InngaaendeHendelse map(JournalpostEndretEvent event) {
        return
                InngaaendeHendelse.builder()
                        .fagomradeAfter(event.getFagomradeAfter())
                        .fagomradeBefore(event.getFagomradeBefore())
                        .journalpostId(event.getJournalpostId())
                        .journalpostStatus(event.getJournalpostStatus()).build();
    }

}
