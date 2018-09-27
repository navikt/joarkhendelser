package no.nav.joarkinngaaendehendelser.consumer.kafka;

import static no.nav.joarkinngaaendehendelser.consumer.kafka.OracleSchema.CREATE_OPERATION;
import static no.nav.joarkinngaaendehendelser.metrics.MetricLabels.JOARK_INNGAAENDE_HENDELSE_JOURNALPOST_ENDRET;

import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import no.nav.joarkinngaaendehendelser.producer.inngaaendejournalpost.EndeligJournalfortPublisher;
import no.nav.joarkinngaaendehendelser.producer.inngaaendejournalpost.TemaEndretPublisher;
import no.nav.joarkinngaaendehendelser.producer.inngaaendejournalpost.UtgarPublisher;
import no.nav.joarkinngaaendehendelser.producer.inngaaendejournalpost.NyPublisher;

import no.nav.joarkinngaaendehendelser.metrics.Metrics;

@Slf4j
@Component
public class JournalpostEndretListener {

    @Autowired
    private ConsumerRecordToJournalpostEndretConverter converter;

    @Autowired
    private EndeligJournalfortPublisher endeligJournalfortPublisher;

    @Autowired
    private TemaEndretPublisher temaEndretPublisher;

    @Autowired
    private UtgarPublisher utgarPublisher;

    @Autowired
    private NyPublisher nyPublisher;

    @KafkaListener(topics = "${journalpostEndret.topic}")
    @Metrics(value = JOARK_INNGAAENDE_HENDELSE_JOURNALPOST_ENDRET, percentiles = {0.5, 0.95}, logExceptions = false)
    public void onMessage(ConsumerRecord<?, Map> record) {
        long start = System.currentTimeMillis();
        try {
            log.debug("Received event from topic: [{}]", record.topic());
            JournalpostEndretEvent event = converter.convert(record);
            if(event != null) {
                log.info("Got {}-operation for journalpostId:{}. Fagomrader {}",
                        event.getOperation(), event.getJournalpostId(),
                        event.getFagomradeBefore() + "/" + event
                                .getFagomradeAfter());
                log.info("Columns changed: {}",
                        event.getColumnsChanged().toString());
                publish(event);
            }
            else {
                log.info("Event is not inngaaende");
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.debug("handling took " + (System.currentTimeMillis() - start) + " ms");
    }

    /**
     *
     * @param event Finner rett topic for event, og publiserer en hendelse dit
     */
    private void publish(JournalpostEndretEvent event) {
        if(JoarkSchema.JOURNALFORT.equalsIgnoreCase(event.getJournalpostStatus())){
            endeligJournalfortPublisher.publish(event);
        }
        if(CREATE_OPERATION.equalsIgnoreCase(event.getOperation())) {
            nyPublisher.publish(event);
        }
        if(!event.getFagomradeBefore().equalsIgnoreCase(event.getFagomradeAfter())) {
            temaEndretPublisher.publish(event);
        }
        if(JoarkSchema.UTGAR.equalsIgnoreCase(event.getJournalpostStatus())) {
            utgarPublisher.publish(event);
        }
    }
}
