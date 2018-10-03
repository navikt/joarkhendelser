package no.nav.joarkinngaaendehendelser.producer;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static no.nav.joarkinngaaendehendelser.producer.InngaaendeHendelsesType.MIDLERTIDIG_JOURNALFORT;
import static org.junit.Assert.*;

import no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostEndretEvent;

public class InngaaendeHendelsePublisherTest {

    JournalpostEndretEvent journalpostEndretEvent;
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
    public void map() throws Exception {
        InngaaendeHendelse map = JournalpostEndretInngaaendeHendelseMapper.map(journalpostEndretEvent);
        assertEquals(MIDLERTIDIG_JOURNALFORT, map.getHendelsesType());
    }

    @Test
    @Ignore
    public void shouldFinnMidlertidigJournalførtHendelsesType() {
        // TODO
    }

    @Test
    @Ignore
    public void shouldFinnTemaEndretHendelsesType() {
        // TODO
    }

    @Test
    @Ignore
    public void shouldFinnEndeligJournalførtHendelsesType() {
        // TODO
    }

    @Test
    @Ignore
    public void shouldFinnJournalfpostUgåttHendelsesType() {
        // TODO
    }

    @Test
    @Ignore
    public void shouldNotMapToHendelsesType() {
        // TODO
    }

    @Test
    @Ignore
    public void shouldNotPublishWhenEventTypeIsNull() {
        // TODO
    }


}