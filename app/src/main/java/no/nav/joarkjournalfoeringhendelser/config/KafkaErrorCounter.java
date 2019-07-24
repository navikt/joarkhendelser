package no.nav.joarkjournalfoeringhendelser.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Martin Burheim Tingstad, Visma Consulting
 */
@Service
@Slf4j
@ApplicationScope
public class KafkaErrorCounter {

    private static AtomicInteger counter = new AtomicInteger(0);

    public int incrementAndGet() {
        log.info("{} supplying a new count value", this.toString());
        return counter.incrementAndGet();
    }

    public void reset() {
        counter.set(0);
    }
}
