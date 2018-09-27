package no.nav.joarkinngaaendehendelser.producer.inngaaendejournalpost;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostEndretEvent;
import no.nav.joarkinngaaendehendelser.producer.CommonInngaaendeEventProducer;

@Component
public class EndeligJournalfortPublisher extends CommonInngaaendeEventProducer {

    @Value("${inngaaendeJournalpost.endeligJournalfort.topic}")
    private String topic;

    @Override
    public void publish(JournalpostEndretEvent event) {
        sendEventToTopic(event, topic);
    }

}
