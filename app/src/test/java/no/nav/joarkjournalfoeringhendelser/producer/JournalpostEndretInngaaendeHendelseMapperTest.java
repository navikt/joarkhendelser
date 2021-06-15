package no.nav.joarkjournalfoeringhendelser.producer;

import static no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelsesType.ENDELIG_JOURNALFORT;
import static no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelsesType.JOURNALPOST_UTGATT;
import static no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelsesType.MIDLERTIDIG_JOURNALFORT;
import static no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelsesType.TEMA_ENDRET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import no.nav.joarkjournalfoeringhendelser.consumer.kafka.JournalpostEndretEvent;
import org.junit.jupiter.api.Test;

public class JournalpostEndretInngaaendeHendelseMapperTest {

	private JournalpostEndretInngaaendeHendelseMapper mapper = new JournalpostEndretInngaaendeHendelseMapper();

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

	@Test
	public void shouldNotMapToMidlertidigJournalførtHendelsesType() {
		JournalpostEndretEvent journalpostEndretEventMidlertidig = createJournalpostEndretEvent("DAG", "DAG", "MO", "M", "U", "I");
		InngaaendeHendelse mapMidlertidig = mapper.map(journalpostEndretEventMidlertidig);
		assertEquals(null, mapMidlertidig);
	}

	@Test
	public void shouldMapToMidlertidigJournalførtHendelsesType() {
		JournalpostEndretEvent journalpostEndretEventMidlertidig = createJournalpostEndretEvent("DAG", "DAG", "OD", "M", "U", "I");
		InngaaendeHendelse mapMidlertidig = mapper.map(journalpostEndretEventMidlertidig);
		JournalpostEndretEvent journalpostEndretEventMottatt = createJournalpostEndretEvent(null, "FOR", null, "MO", "I", "I");
		InngaaendeHendelse mapMottatt = mapper.map(journalpostEndretEventMottatt);
		assertEquals(MIDLERTIDIG_JOURNALFORT.toString(), mapMottatt.getHendelsesType());
		assertEquals(MIDLERTIDIG_JOURNALFORT.toString(), mapMidlertidig.getHendelsesType());
	}

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
		JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("DAG", "FOR", "MO", "M", "U", "I");
		InngaaendeHendelse map = mapper.map(journalpostEndretEvent);
		assertEquals(TEMA_ENDRET.toString(), map.getHendelsesType());
	}

	@Test
	public void shouldFinnEndeligJournalførtHendelsesType() {
		JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("DAG", "FOR", "M", "J", "U", "I");
		InngaaendeHendelse map = mapper.map(journalpostEndretEvent);
		assertEquals(ENDELIG_JOURNALFORT.toString(), map.getHendelsesType());
	}

	@Test
	public void shouldFinnJournalpostUtgaattHendelsesType() {
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