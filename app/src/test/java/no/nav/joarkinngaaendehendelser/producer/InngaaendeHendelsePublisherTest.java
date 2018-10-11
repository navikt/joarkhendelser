package no.nav.joarkinngaaendehendelser.producer;

import no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostEndretEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

public class InngaaendeHendelsePublisherTest {

    JournalpostEndretEvent journalpostEndretEvent;

    @Mock
    ProducerRecord<String, InngaaendeHendelseRecord> producerRecord;

    @Autowired
    InngaaendeHendelsePublisher publisher;

    @Mock
    private KafkaTemplate<String, InngaaendeHendelseRecord> kafkaTemplate;

    @Before
    public void setUp() throws Exception {
        journalpostEndretEvent = JournalpostEndretEvent.builder()
                .journalpostStatusAfter("MO")
                .journalpostStatusBefore("MO")
                .operation("I")
                .journalpostType("I")
                .build();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    @Ignore
    public void shouldPublish() throws Exception {
        InngaaendeHendelse map = JournalpostEndretInngaaendeHendelseMapper.map(journalpostEndretEvent);
        publisher.publish(map);

//        verify(kafkaTemplate).send(producerRecord);
        //assertEquals(MIDLERTIDIG_JOURNALFORT, map.getHendelsesType());
    }
}