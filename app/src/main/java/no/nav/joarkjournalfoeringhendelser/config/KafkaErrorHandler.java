package no.nav.joarkjournalfoeringhendelser.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.springframework.kafka.listener.ContainerAwareErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin Burheim Tingstad, Visma Consulting
 */
@Component
@Slf4j
public class KafkaErrorHandler implements ContainerAwareErrorHandler {

    private final static int DURATION = 20;

    @Override
    public void handle(Exception e, List<ConsumerRecord<?, ?>> list,
                       Consumer<?, ?> consumer,
                       MessageListenerContainer messageListenerContainer) {
        if(list.size() == 1) {
            ConsumerRecord<?, ?> record = list.get(0);
            log.warn("Failed to commit offset {} on partition {}", record.offset(), record.partition());
        }

        log.warn("Thread {} sleeping {} seconds to try again", Thread.currentThread().getId(), DURATION);
        try {
            Thread.sleep(Duration.ofSeconds(DURATION).toMillis());
        } catch (InterruptedException ie) {
        }
    }
}
