package no.nav.joarkinngaaendehendelser.producer;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Martin Burheim Tingstad, Visma Consulting.
 */
@Slf4j
@Component
public class InngaaendeHendelsePublisher {

    @Autowired
    private KafkaTemplate<String, InngaaendeHendelse> kafkaTemplate;

    @Value("${inngaaendeJournalpostEndret.topic}")
    private String topic;

    public void publish(InngaaendeHendelse hendelse) {
        ProducerRecord<String, InngaaendeHendelse> producerRecord = new ProducerRecord<>(
                topic,
                null,
                hendelse.getTimestamp(),
                hendelse.getJournalpostId(),
                hendelse);
        producerRecord.headers().add("hendelsesId", hendelse.getJournalpostId().getBytes(StandardCharsets.UTF_8));
        producerRecord.headers().add("versjon", hendelse.getVersjon().getBytes(StandardCharsets.UTF_8));
        try {
            kafkaTemplate.send(producerRecord).get();
        }
        catch (InterruptedException | ExecutionException e) {
            log.warn("Failed to send message to kafka. Topic: " + topic, e.getMessage());
        }

    }

}
