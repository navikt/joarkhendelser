package no.nav.joarkhendelser.producer;

import no.nav.joarkhendelser.consumer.JournalpostEndretEvent;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static no.nav.joarkhendelser.producer.JournalpostStatus.JOURNALFORT;
import static no.nav.joarkhendelser.producer.JournalpostStatus.MOTTATT;
import static no.nav.joarkhendelser.producer.JournalpostStatus.UKJENTBRUKER;
import static no.nav.joarkhendelser.producer.JournalpostStatus.UTGAR;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.ENDELIG_JOURNALFORT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.JOURNALPOST_MOTTATT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.JOURNALPOST_UTGATT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.TEMA_ENDRET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JournalpostEndretInngaaendeHendelseMapperTest {

	private final JournalpostEndretInngaaendeHendelseMapper mapper = new JournalpostEndretInngaaendeHendelseMapper();

	@Test
	public void shouldNotMapToJournalpostMottatt() {
		JournalpostEndretEvent journalpostEndretEventMidlertidig = createJournalpostEndretEvent("DAG", "DAG", "MO", "M", "U", "I");
		InngaaendeHendelse mapMidlertidig = mapper.map(journalpostEndretEventMidlertidig, this.buildGoldenGate());

		assertNull(mapMidlertidig);
	}

	@Test
	public void shouldMapToJournalpostMottatt() {
		JournalpostEndretEvent journalpostEndretEventMidlertidig = createJournalpostEndretEvent("DAG", "FOR", "M", "M", "I", "I");
		InngaaendeHendelse mapMidlertidig = mapper.map(journalpostEndretEventMidlertidig, this.buildGoldenGate());
		JournalpostEndretEvent journalpostEndretEventMottatt = createJournalpostEndretEvent("DAG", "FOR", "MO", "MO", "I", "I");
		InngaaendeHendelse mapMottatt = mapper.map(journalpostEndretEventMottatt, this.buildGoldenGate());

		assertEquals(JOURNALPOST_MOTTATT.toString(), mapMottatt.getHendelsesType());
		assertEquals(JOURNALPOST_MOTTATT.toString(), mapMidlertidig.getHendelsesType());
		assertEquals(MOTTATT, mapMidlertidig.getJournalpostStatus());
		assertEquals(MOTTATT, mapMottatt.getJournalpostStatus());
	}

	@Test
	public void shouldMapToJournalpostMottattIfUpdateFromOpplastingDokument() {
		JournalpostEndretEvent journalpostEndretEventMidlertidig = createJournalpostEndretEvent("DAG", "DAG", "OD", "M", "U", "I");
		InngaaendeHendelse mapMidlertidig = mapper.map(journalpostEndretEventMidlertidig, this.buildGoldenGate());
		JournalpostEndretEvent journalpostEndretEventMottatt = createJournalpostEndretEvent(null, "FOR", null, "MO", "I", "I");
		InngaaendeHendelse mapMottatt = mapper.map(journalpostEndretEventMottatt, this.buildGoldenGate());

		assertEquals(JOURNALPOST_MOTTATT.toString(), mapMottatt.getHendelsesType());
		assertEquals(JOURNALPOST_MOTTATT.toString(), mapMidlertidig.getHendelsesType());
		assertEquals(MOTTATT, mapMidlertidig.getJournalpostStatus());
		assertEquals(MOTTATT, mapMottatt.getJournalpostStatus());
	}

	@Test
	public void shouldMapToEndeligJournalfort() {
		JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("DAG", "FOR", "M", "J", "U", "I");
		InngaaendeHendelse map = mapper.map(journalpostEndretEvent, this.buildGoldenGate());

		assertEquals(ENDELIG_JOURNALFORT.toString(), map.getHendelsesType());
		assertEquals(JOURNALFORT, map.getJournalpostStatus());

	}

	@Test
	public void shouldMapToJournalpostUtgaatt() {
		JournalpostEndretEvent journalpostEndretEventUtgatt = createJournalpostEndretEvent("", "", "MO", "U", "U", "I");
		JournalpostEndretEvent journalpostEndretEventUkjent = createJournalpostEndretEvent("", "", "MO", "UB", "U", "I");
		InngaaendeHendelse mapUtgatt = mapper.map(journalpostEndretEventUtgatt, this.buildGoldenGate());
		InngaaendeHendelse mapUkjent = mapper.map(journalpostEndretEventUkjent, this.buildGoldenGate());

		assertEquals(JOURNALPOST_UTGATT.toString(), mapUtgatt.getHendelsesType());
		assertEquals(JOURNALPOST_UTGATT.toString(), mapUkjent.getHendelsesType());
		assertEquals(UTGAR, mapUtgatt.getJournalpostStatus());
		assertEquals(UKJENTBRUKER, mapUkjent.getJournalpostStatus());
	}

	@Test
	public void shouldMapToTemaEndret() {
		JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("DAG", "FOR", "MO", "M", "U", "I");
		InngaaendeHendelse map = mapper.map(journalpostEndretEvent, this.buildGoldenGate());

		assertEquals(TEMA_ENDRET.toString(), map.getHendelsesType());
		assertEquals(MOTTATT, map.getJournalpostStatus());
	}

	@Test
	public void shouldNotMapToHendelsesType() {
		JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("", "", "", "", "", "");

		assertNull(mapper.map(journalpostEndretEvent, this.buildGoldenGate()));
	}

	@Test
	public void shouldMapToJournalpostMottatt22() {
		GoldenGateEvent goldenGateEvent = this.buildGoldenGate();
		JournalpostEndretEvent journalpostEndretEventMidlertidig = createJournalpostEndretEvent("DAG", "FOR", "M", "M", "I", "I");
		InngaaendeHendelse mapMidlertidig = mapper.map(journalpostEndretEventMidlertidig, goldenGateEvent);

		assertEquals("1234567-" + goldenGateEvent.getOperationTimestamp(),
				mapMidlertidig.getHendelsesId());
	}

	private JournalpostEndretEvent createJournalpostEndretEvent(String fagomradeBefore, String fagomradeAfter, String journalpostStatusBefore, String journalpostStatusAfter, String operation, String journalposttype) {
		return JournalpostEndretEvent.builder()
				.journalpostId(1234567l)
				.fagomradeBefore(fagomradeBefore)
				.fagomradeAfter(fagomradeAfter)
				.journalpostStatusBefore(journalpostStatusBefore)
				.journalpostStatusAfter(journalpostStatusAfter)
				.operation(operation)
				.journalpostType(journalposttype)
				.build();
	}

	private GoldenGateEvent buildGoldenGate() {
		GoldenGateEvent goldenGateEvent = new GoldenGateEvent();
		goldenGateEvent.setOperationTimestamp(LocalDateTime.now());

		return goldenGateEvent;
	}
}