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
        log.info("Instanciating a new KafkaErrorCounter");
        if(counter == null) {
            log.info("Instanciating a new AtomicInteger");
            counter = new AtomicInteger(0);
        }
    }

    public int incrementAndGet() {
        log.info("{} supplying a new count value", this.toString());
        log.info("{} supplying a new count value", counter.toString());
        log.info("Before increment: {}", counter.get());
        int newValue = counter.incrementAndGet();
        log.info("After increment: {}", counter.get());
        return newValue;
    }

    public void reset() {
        counter.set(0);
    }
}
