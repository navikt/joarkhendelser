package no.nav.joarkhendelser.producer;

import no.nav.joarkhendelser.consumer.kafka.JournalpostEndretEvent;
import org.junit.jupiter.api.Test;

import static no.nav.joarkhendelser.consumer.kafka.JournalpostStatus.JOURNALFORT;
import static no.nav.joarkhendelser.consumer.kafka.JournalpostStatus.MOTTATT;
import static no.nav.joarkhendelser.consumer.kafka.JournalpostStatus.UKJENTBRUKER;
import static no.nav.joarkhendelser.consumer.kafka.JournalpostStatus.UTGAR;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.ENDELIG_JOURNALFORT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.JOURNALPOST_MOTTATT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.JOURNALPOST_UTGATT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.TEMA_ENDRET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
	public void shouldMapToMidlertidigJournalfortHendelsesType() {
		JournalpostEndretEvent journalpostEndretEventMidlertidig = createJournalpostEndretEvent("DAG", "DAG", "OD", "M", "U", "I");
		InngaaendeHendelse mapMidlertidig = mapper.map(journalpostEndretEventMidlertidig);
		JournalpostEndretEvent journalpostEndretEventMottatt = createJournalpostEndretEvent(null, "FOR", null, "MO", "I", "I");
		InngaaendeHendelse mapMottatt = mapper.map(journalpostEndretEventMottatt);
		assertEquals(JOURNALPOST_MOTTATT.toString(), mapMottatt.getHendelsesType());
		assertEquals(JOURNALPOST_MOTTATT.toString(), mapMidlertidig.getHendelsesType());
		assertEquals(MOTTATT, mapMidlertidig.getJournalpostStatus());
		assertEquals(MOTTATT, mapMottatt.getJournalpostStatus());
	}

	@Test
	public void shouldFinnMidlertidigJournalfortHendelsesType() {
		JournalpostEndretEvent journalpostEndretEventMidlertidig = createJournalpostEndretEvent("DAG", "FOR", "M", "M", "I", "I");
		InngaaendeHendelse mapMidlertidig = mapper.map(journalpostEndretEventMidlertidig);
		JournalpostEndretEvent journalpostEndretEventMottatt = createJournalpostEndretEvent("DAG", "FOR", "MO", "MO", "I", "I");
		InngaaendeHendelse mapMottatt = mapper.map(journalpostEndretEventMottatt);
		assertEquals(JOURNALPOST_MOTTATT.toString(), mapMottatt.getHendelsesType());
		assertEquals(JOURNALPOST_MOTTATT.toString(), mapMidlertidig.getHendelsesType());
		assertEquals(MOTTATT, mapMidlertidig.getJournalpostStatus());
		assertEquals(MOTTATT, mapMottatt.getJournalpostStatus());
	}

	@Test
	public void shouldFinnTemaEndretHendelsesType() {
		JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("DAG", "FOR", "MO", "M", "U", "I");
		InngaaendeHendelse map = mapper.map(journalpostEndretEvent);
		assertEquals(TEMA_ENDRET.toString(), map.getHendelsesType());
		assertEquals(MOTTATT, map.getJournalpostStatus());
	}

	@Test
	public void shouldFinnEndeligJournalfortHendelsesType() {
		JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("DAG", "FOR", "M", "J", "U", "I");
		InngaaendeHendelse map = mapper.map(journalpostEndretEvent);
		assertEquals(ENDELIG_JOURNALFORT.toString(), map.getHendelsesType());
		assertEquals(JOURNALFORT, map.getJournalpostStatus());

	}

	@Test
	public void shouldFinnJournalpostUtgaattHendelsesType() {
		JournalpostEndretEvent journalpostEndretEventUtgatt = createJournalpostEndretEvent("", "", "MO", "U", "U", "I");
		JournalpostEndretEvent journalpostEndretEventUkjent = createJournalpostEndretEvent("", "", "MO", "UB", "U", "I");
		InngaaendeHendelse mapUtgatt = mapper.map(journalpostEndretEventUtgatt);
		InngaaendeHendelse mapUkjent = mapper.map(journalpostEndretEventUkjent);
		assertEquals(JOURNALPOST_UTGATT.toString(), mapUtgatt.getHendelsesType());
		assertEquals(JOURNALPOST_UTGATT.toString(), mapUkjent.getHendelsesType());
		assertEquals(UTGAR, mapUtgatt.getJournalpostStatus());
		assertEquals(UKJENTBRUKER, mapUkjent.getJournalpostStatus());
	}

	@Test
	public void shouldNotMapToHendelsesType() {
		JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("", "", "", "", "", "");
		assertNull(mapper.map(journalpostEndretEvent));
	}
}