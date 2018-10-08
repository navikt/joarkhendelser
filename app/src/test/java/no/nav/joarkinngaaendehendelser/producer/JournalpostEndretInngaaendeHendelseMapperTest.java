package no.nav.joarkinngaaendehendelser.producer;

import no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostEndretEvent;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.kafka.core.KafkaTemplate;

import static no.nav.joarkinngaaendehendelser.producer.InngaaendeHendelsesType.ENDELIG_JOURNALFORT;
import static no.nav.joarkinngaaendehendelser.producer.InngaaendeHendelsesType.JOURNALPOST_UTGATT;
import static no.nav.joarkinngaaendehendelser.producer.InngaaendeHendelsesType.MIDLERTIDIG_JOURNALFORT;
import static no.nav.joarkinngaaendehendelser.producer.InngaaendeHendelsesType.TEMA_ENDRET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JournalpostEndretInngaaendeHendelseMapperTest {

    private JournalpostEndretEvent createJournalpostEndretEvent(String fagomradeBefore, String fagomradeAfter, String journalpostStatusBefore, String journalpostStatusAfter, String operation, String journalposttype) {
        return JournalpostEndretEvent.builder()
                .fagomradeBefore(fagomradeBefore)
                .fagomradeAfter(fagomradeAfter)
                .journalpostStatusAfter(journalpostStatusAfter)
                .journalpostStatusBefore(journalpostStatusBefore)
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
        JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("DAG", "FOR", "MO", "MO", "I", "I");
        InngaaendeHendelse map = mapper.map(journalpostEndretEvent);
        assertEquals(MIDLERTIDIG_JOURNALFORT, map.getHendelsesType());
    }

    @Test
    public void shouldFinnTemaEndretHendelsesType() {
        JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("DAG", "FOR", "MO", "J", "U", "I");
        InngaaendeHendelse map = mapper.map(journalpostEndretEvent);
        assertEquals(TEMA_ENDRET, map.getHendelsesType());
    }

    @Test
    public void shouldFinnEndeligJournalførtHendelsesType() {
        JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("DAG", "FOR", "M", "J", "D", "I");
        InngaaendeHendelse map = mapper.map(journalpostEndretEvent);
        assertEquals(ENDELIG_JOURNALFORT, map.getHendelsesType());
    }

    @Test
    public void shouldFinnJournalfpostUgåttHendelsesType() {
        JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("", "", "MO", "U", "U", "I");
        InngaaendeHendelse map = mapper.map(journalpostEndretEvent);
        assertEquals(JOURNALPOST_UTGATT, map.getHendelsesType());
    }

    @Test
    public void shouldNotMapToHendelsesType() {
        JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("", "", "", "", "", "");
        assertNull(mapper.map(journalpostEndretEvent));
    }
}