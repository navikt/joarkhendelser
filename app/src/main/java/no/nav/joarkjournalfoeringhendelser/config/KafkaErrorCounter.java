package no.nav.joarkjournalfoeringhendelser.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Martin Burheim Tingstad, Visma Consulting
 */
@Component
@Slf4j
@ApplicationScope
public class KafkaErrorCounter {

    private static AtomicInteger counter;

    public KafkaErrorCounter() {
        if(counter == null) {
            counter = new AtomicInteger(0);
        }
    }

    public int incrementAndGet() {
        return counter.incrementAndGet();
    }

    public void reset() {
        counter.set(0);
    }
}
