package no.nav.joarkhendelser.producer;

import no.nav.joarkhendelser.consumer.JournalpostEndretEvent;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.LocalDateTime.now;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.ENDELIG_JOURNALFORT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.JOURNALPOST_MOTTATT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.JOURNALPOST_UTGATT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.TEMA_ENDRET;
import static no.nav.joarkhendelser.producer.JournalpostEndretInngaaendeHendelseMapper.map;
import static no.nav.joarkhendelser.producer.JournalpostStatus.JOURNALFORT;
import static no.nav.joarkhendelser.producer.JournalpostStatus.MOTTATT;
import static no.nav.joarkhendelser.producer.JournalpostStatus.UKJENTBRUKER;
import static no.nav.joarkhendelser.producer.JournalpostStatus.UTGAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JournalpostEndretInngaaendeHendelseMapperTest {


	@Test
	public void shouldNotMapToJournalpostMottatt() {
		JournalpostEndretEvent journalpostEndretEventMidlertidig = createJournalpostEndretEvent("DAG", "DAG", "MO", "M", "U", "I");
		InngaaendeHendelse mapMidlertidig = map(journalpostEndretEventMidlertidig, buildGoldenGateEvent(now()));

		assertNull(mapMidlertidig);
	}

	@Test
	public void shouldMapToJournalpostMottatt() {
		JournalpostEndretEvent journalpostEndretEventMidlertidig = createJournalpostEndretEvent("DAG", "FOR", "M", "M", "I", "I");
		InngaaendeHendelse mapMidlertidig = map(journalpostEndretEventMidlertidig, buildGoldenGateEvent(now()));
		JournalpostEndretEvent journalpostEndretEventMottatt = createJournalpostEndretEvent("DAG", "FOR", "MO", "MO", "I", "I");
		InngaaendeHendelse mapMottatt = map(journalpostEndretEventMottatt, buildGoldenGateEvent(now()));

		assertEquals(JOURNALPOST_MOTTATT.toString(), mapMottatt.getHendelsesType());
		assertEquals(JOURNALPOST_MOTTATT.toString(), mapMidlertidig.getHendelsesType());
		assertEquals(MOTTATT, mapMidlertidig.getJournalpostStatus());
		assertEquals(MOTTATT, mapMottatt.getJournalpostStatus());
	}

	@Test
	public void shouldMapToJournalpostMottattIfUpdateFromOpplastingDokument() {
		JournalpostEndretEvent journalpostEndretEventMidlertidig = createJournalpostEndretEvent("DAG", "DAG", "OD", "M", "U", "I");
		InngaaendeHendelse mapMidlertidig = map(journalpostEndretEventMidlertidig, buildGoldenGateEvent(now()));
		JournalpostEndretEvent journalpostEndretEventMottatt = createJournalpostEndretEvent(null, "FOR", null, "MO", "I", "I");
		InngaaendeHendelse mapMottatt = map(journalpostEndretEventMottatt, buildGoldenGateEvent(now()));

		assertEquals(JOURNALPOST_MOTTATT.toString(), mapMottatt.getHendelsesType());
		assertEquals(JOURNALPOST_MOTTATT.toString(), mapMidlertidig.getHendelsesType());
		assertEquals(MOTTATT, mapMidlertidig.getJournalpostStatus());
		assertEquals(MOTTATT, mapMottatt.getJournalpostStatus());
	}

	@Test
	public void shouldMapToEndeligJournalfort() {
		JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("DAG", "FOR", "M", "J", "U", "I");
		InngaaendeHendelse map = map(journalpostEndretEvent, buildGoldenGateEvent(now()));

		assertEquals(ENDELIG_JOURNALFORT.toString(), map.getHendelsesType());
		assertEquals(JOURNALFORT, map.getJournalpostStatus());

	}

	@Test
	public void shouldMapToJournalpostUtgaatt() {
		JournalpostEndretEvent journalpostEndretEventUtgatt = createJournalpostEndretEvent("", "", "MO", "U", "U", "I");
		JournalpostEndretEvent journalpostEndretEventUkjent = createJournalpostEndretEvent("", "", "MO", "UB", "U", "I");
		InngaaendeHendelse mapUtgatt = map(journalpostEndretEventUtgatt, buildGoldenGateEvent(now()));
		InngaaendeHendelse mapUkjent = map(journalpostEndretEventUkjent, buildGoldenGateEvent(now()));

		assertEquals(JOURNALPOST_UTGATT.toString(), mapUtgatt.getHendelsesType());
		assertEquals(JOURNALPOST_UTGATT.toString(), mapUkjent.getHendelsesType());
		assertEquals(UTGAR, mapUtgatt.getJournalpostStatus());
		assertEquals(UKJENTBRUKER, mapUkjent.getJournalpostStatus());
	}

	@Test
	public void shouldMapToTemaEndret() {
		JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("DAG", "FOR", "MO", "M", "U", "I");
		InngaaendeHendelse map = map(journalpostEndretEvent, buildGoldenGateEvent(now()));

		assertEquals(TEMA_ENDRET.toString(), map.getHendelsesType());
		assertEquals(MOTTATT, map.getJournalpostStatus());
	}

	@Test
	public void shouldNotMapToHendelsesType() {
		JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("", "", "", "", "", "");

		assertNull(map(journalpostEndretEvent, buildGoldenGateEvent(now())));
	}

	@Test
	public void shouldMapHendelseId() {
		// hendelseId = journalpostId + op_ts som ISO8601-tid (yyyy-MM-ddTHH:mm:ss)
		//	format på op_ts: yyyy-MM-dd HH:mm:ss.SSSSSS
		//	eksempelverdi på op_ts: 2021-09-22 12:47:02.000000
		String operationTimestamp = "2021-09-22 12:47:02.000000";

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
		LocalDateTime dateTime = LocalDateTime.parse(operationTimestamp, formatter);

		GoldenGateEvent goldenGateEvent = buildGoldenGateEvent(dateTime);
		JournalpostEndretEvent event = createJournalpostEndretEvent("DAG", "FOR", "M", "M", "I", "I");
		InngaaendeHendelse mapMidlertidig = map(event, goldenGateEvent);

		String iso8610Time = "2021-09-22T12:47:02";
		assertEquals("1234567-" + iso8610Time, mapMidlertidig.getHendelsesId());
	}

	private JournalpostEndretEvent createJournalpostEndretEvent(String fagomradeBefore, String fagomradeAfter, String journalpostStatusBefore, String journalpostStatusAfter, String operation, String journalposttype) {
		return JournalpostEndretEvent.builder()
				.journalpostId(1234567L)
				.fagomradeBefore(fagomradeBefore)
				.fagomradeAfter(fagomradeAfter)
				.journalpostStatusBefore(journalpostStatusBefore)
				.journalpostStatusAfter(journalpostStatusAfter)
				.operation(operation)
				.journalpostType(journalposttype)
				.build();
	}

	private GoldenGateEvent buildGoldenGateEvent(LocalDateTime time) {
		GoldenGateEvent goldenGateEvent = new GoldenGateEvent();
		goldenGateEvent.setOperationTimestamp(time);

		return goldenGateEvent;
	}
}