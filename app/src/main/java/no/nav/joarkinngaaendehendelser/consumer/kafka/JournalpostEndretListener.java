package no.nav.joarkinngaaendehendelser.consumer.kafka;

import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.INNGAAENDE;
import static no.nav.joarkinngaaendehendelser.metrics.MetricLabels.JOARK_INNGAAENDE_HENDELSE_JOURNALPOST_ENDRET;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import no.nav.joarkinngaaendehendelser.producer.InngaaendeHendelse;
import no.nav.joarkinngaaendehendelser.producer.InngaaendeHendelsePublisher;

import no.nav.joarkinngaaendehendelser.metrics.Metrics;
import no.nav.joarkinngaaendehendelser.producer.JournalpostEndretInngaaendeHendelseMapper;

/**
 * @author Martin Burheim Tingstad, Visma Consulting.
 */
@Slf4j
@Component
public class JournalpostEndretListener {

    @Autowired
    private ConsumerRecordAsJsonConverter converter;

    @Autowired
    private InngaaendeHendelsePublisher publisher;

    @KafkaListener(topics = "${journalpostEndret.topic}")
    @Metrics(value = JOARK_INNGAAENDE_HENDELSE_JOURNALPOST_ENDRET, percentiles = {0.5, 0.95}, logExceptions = false)
    public void onMessage(ConsumerRecord<?, ?> record) {
        long start = System.currentTimeMillis();
        try {
            JournalpostEndretEvent event = converter.convert(record);

            if(event != null && INNGAAENDE.equalsIgnoreCase(event.getJournalpostType())) {
                InngaaendeHendelse hendelse = JournalpostEndretInngaaendeHendelseMapper.map(event);
                if(hendelse != null) {
                    publisher.publish(hendelse);
                    log.info("Publisert hendelse "+hendelse.getHendelsesType()+" for journalpost "+hendelse.getJournalpostId());
                }
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.debug("handling took " + (System.currentTimeMillis() - start) + " ms");
    }

}
