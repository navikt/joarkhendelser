package no.nav.joarkjournalfoeringhendelser.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.springframework.kafka.listener.ContainerAwareErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin Burheim Tingstad, Visma Consulting
 */
@Component
@Slf4j
public class KafkaErrorHandler implements ContainerAwareErrorHandler {

    @Override
    public void handle(Exception e, List<ConsumerRecord<?, ?>> list,
                       Consumer<?, ?> consumer,
                       MessageListenerContainer messageListenerContainer) {
        if(e instanceof TopicAuthorizationException) {
            TopicAuthorizationException tae = (TopicAuthorizationException) e;
            ArrayList<String> topicNames = new ArrayList<>(tae.unauthorizedTopics());

            for (String s : topicNames) {
                log.warn("Could not authorize to topic {}", s);
            }
            log.warn("Waiting 20 seconds to try again");
            try {
                Thread.sleep(20000);
            } catch (InterruptedException ie) {
            }
        }
    }
}
