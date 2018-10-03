package no.nav.joarkinngaaendehendelser.producer;

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
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${inngaaendeJournalpostEndret.topic}")
    private String topic;

    public void publish(InngaaendeHendelse hendelse) {
        ProducerRecord<Object, Object> producerRecord = new ProducerRecord<>(
                topic,
                null,
                hendelse.getTimestamp(),
                hendelse.getJournalpostId(),
                hendelse);
        try {
            kafkaTemplate.send(producerRecord).get();
        }
        catch (InterruptedException | ExecutionException e) {
            log.warn("Failed to send message to kafka. Topic: " + topic, e.getMessage());
        }

    }

}
