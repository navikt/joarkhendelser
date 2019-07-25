package no.nav.joarkjournalfoeringhendelser.config;

import org.springframework.kafka.listener.DefaultAfterRollbackProcessor;

public class InfiniteRollbackProcessor extends DefaultAfterRollbackProcessor {

    public InfiniteRollbackProcessor() {
        super(-1);
    }
}
