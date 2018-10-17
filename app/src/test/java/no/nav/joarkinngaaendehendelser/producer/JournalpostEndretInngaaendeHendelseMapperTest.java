package no.nav.joarkinngaaendehendelser.producer;

import static no.nav.joarkinngaaendehendelser.producer.InngaaendeHendelsesType.ENDELIG_JOURNALFORT;
import static no.nav.joarkinngaaendehendelser.producer.InngaaendeHendelsesType.JOURNALPOST_UTGATT;
import static no.nav.joarkinngaaendehendelser.producer.InngaaendeHendelsesType.MIDLERTIDIG_JOURNALFORT;
import static no.nav.joarkinngaaendehendelser.producer.InngaaendeHendelsesType.TEMA_ENDRET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostEndretEvent;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.kafka.core.KafkaTemplate;

public class JournalpostEndretInngaaendeHendelseMapperTest {

    private JournalpostEndretEvent createJournalpostEndretEvent(String fagomradeBefore, String fagomradeAfter, String journalpostStatusBefore, String journalpostStatusAfter, String operation, String journalposttype) {
        return JournalpostEndretEvent.builder()
                .fagomradeBefore(fagomradeBefore)
                .fagomradeAfter(fagomradeAfter)
                .journalpostStatusBefore(journalpostStatusBefore)
                .journalpostStatusAfter(journalpostStatusAfter)
                .operation(operation)
                .journalpostType(journalposttype)
                .build();
    }

    @Mock
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @InjectMocks
    private JournalpostEndretInngaaendeHendelseMapper mapper = new JournalpostEndretInngaaendeHendelseMapper();

    @Test
    public void shouldFinnMidlertidigJournalførtHendelsesType() {
        JournalpostEndretEvent journalpostEndretEventMidlertidig = createJournalpostEndretEvent("DAG", "FOR", "M", "M", "I", "I");
        InngaaendeHendelse mapMidlertidig = mapper.map(journalpostEndretEventMidlertidig);
        JournalpostEndretEvent journalpostEndretEventMottatt = createJournalpostEndretEvent("DAG", "FOR", "MO", "MO", "I", "I");
        InngaaendeHendelse mapMottatt = mapper.map(journalpostEndretEventMottatt);
        assertEquals(MIDLERTIDIG_JOURNALFORT.toString(), mapMottatt.getHendelsesType());
        assertEquals(MIDLERTIDIG_JOURNALFORT.toString(), mapMidlertidig.getHendelsesType());
    }

    @Test
    public void shouldFinnTemaEndretHendelsesType() {
        JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("DAG", "FOR", "MO", "J", "U", "I");
        InngaaendeHendelse map = mapper.map(journalpostEndretEvent);
        assertEquals(TEMA_ENDRET.toString(), map.getHendelsesType());
    }

    @Test
    public void shouldFinnEndeligJournalførtHendelsesType() {
        JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("DAG", "FOR", "M", "J", "D", "I");
        InngaaendeHendelse map = mapper.map(journalpostEndretEvent);
        assertEquals(ENDELIG_JOURNALFORT.toString(), map.getHendelsesType());
    }

    @Test
    public void shouldFinnJournalfpostUgåttHendelsesType() {
        JournalpostEndretEvent journalpostEndretEventUtgatt = createJournalpostEndretEvent("", "", "MO", "U", "U", "I");
        JournalpostEndretEvent journalpostEndretEventUkjent = createJournalpostEndretEvent("", "", "MO", "UB", "U", "I");
        InngaaendeHendelse mapUtgatt = mapper.map(journalpostEndretEventUtgatt);
        InngaaendeHendelse mapUkjent = mapper.map(journalpostEndretEventUkjent);
        assertEquals(JOURNALPOST_UTGATT.toString(), mapUtgatt.getHendelsesType());
        assertEquals(JOURNALPOST_UTGATT.toString(), mapUkjent.getHendelsesType());
    }

    @Test
    public void shouldNotMapToHendelsesType() {
        JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("", "", "", "", "", "");
        assertNull(mapper.map(journalpostEndretEvent));
    }
}