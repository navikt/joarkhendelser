package no.nav.joarkhendelser.producer;

import no.nav.joarkhendelser.consumer.JournalpostEndretEvent;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.LocalDateTime.now;
import static no.nav.joarkhendelser.producer.Hendelsestype.ENDELIG_JOURNALFOERT;
import static no.nav.joarkhendelser.producer.Hendelsestype.JOURNALPOST_MOTTATT;
import static no.nav.joarkhendelser.producer.Hendelsestype.JOURNALPOST_UTGAATT;
import static no.nav.joarkhendelser.producer.Hendelsestype.TEMA_ENDRET;
import static no.nav.joarkhendelser.producer.JoarkhendelseMapper.map;
import static no.nav.joarkhendelser.producer.Journalpoststatus.JOURNALFOERT;
import static no.nav.joarkhendelser.producer.Journalpoststatus.MOTTATT;
import static no.nav.joarkhendelser.producer.Journalpoststatus.UKJENTBRUKER;
import static no.nav.joarkhendelser.producer.Journalpoststatus.UTGAAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JoarkhendelseMapperTest {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

	@Test
	public void shouldNotMapToJournalpostMottatt() {
		JournalpostEndretEvent journalpostEndretEventMidlertidig = createJournalpostEndretEvent("DAG", "DAG", "MO", "M", "U", "I");
		Joarkhendelse mapMidlertidig = map(journalpostEndretEventMidlertidig, buildGoldenGateEvent(now()));

		assertNull(mapMidlertidig);
	}

	@Test
	public void shouldMapToJournalpostMottatt() {
		JournalpostEndretEvent journalpostEndretEventMidlertidig = createJournalpostEndretEvent("DAG", "FOR", "M", "M", "I", "I");
		Joarkhendelse mapMidlertidig = map(journalpostEndretEventMidlertidig, buildGoldenGateEvent(now()));
		JournalpostEndretEvent journalpostEndretEventMottatt = createJournalpostEndretEvent("DAG", "FOR", "MO", "MO", "I", "I");
		Joarkhendelse mapMottatt = map(journalpostEndretEventMottatt, buildGoldenGateEvent(now()));

		assertEquals(JOURNALPOST_MOTTATT.toString(), mapMottatt.getHendelsestype());
		assertEquals(JOURNALPOST_MOTTATT.toString(), mapMidlertidig.getHendelsestype());
		assertEquals(MOTTATT, mapMidlertidig.getJournalpoststatus());
		assertEquals(MOTTATT, mapMottatt.getJournalpoststatus());
	}

	@Test
	public void shouldMapToJournalpostMottattIfUpdateFromOpplastingDokument() {
		JournalpostEndretEvent journalpostEndretEventMidlertidig = createJournalpostEndretEvent("DAG", "DAG", "OD", "M", "U", "I");
		Joarkhendelse mapMidlertidig = map(journalpostEndretEventMidlertidig, buildGoldenGateEvent(now()));
		JournalpostEndretEvent journalpostEndretEventMottatt = createJournalpostEndretEvent(null, "FOR", null, "MO", "I", "I");
		Joarkhendelse mapMottatt = map(journalpostEndretEventMottatt, buildGoldenGateEvent(now()));

		assertEquals(JOURNALPOST_MOTTATT.toString(), mapMottatt.getHendelsestype());
		assertEquals(JOURNALPOST_MOTTATT.toString(), mapMidlertidig.getHendelsestype());
		assertEquals(MOTTATT, mapMidlertidig.getJournalpoststatus());
		assertEquals(MOTTATT, mapMottatt.getJournalpoststatus());
	}

	@Test
	public void shouldMapToEndeligJournalfort() {
		JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("DAG", "FOR", "M", "J", "U", "I");
		Joarkhendelse map = map(journalpostEndretEvent, buildGoldenGateEvent(now()));

		assertEquals(ENDELIG_JOURNALFOERT.toString(), map.getHendelsestype());
		assertEquals(JOURNALFOERT, map.getJournalpoststatus());
	}

	@Test
	public void shouldMapToJournalpostUtgaatt() {
		JournalpostEndretEvent journalpostEndretEventUtgatt = createJournalpostEndretEvent("", "", "MO", "U", "U", "I");
		JournalpostEndretEvent journalpostEndretEventUkjent = createJournalpostEndretEvent("", "", "MO", "UB", "U", "I");
		Joarkhendelse mapUtgatt = map(journalpostEndretEventUtgatt, buildGoldenGateEvent(now()));
		Joarkhendelse mapUkjent = map(journalpostEndretEventUkjent, buildGoldenGateEvent(now()));

		assertEquals(JOURNALPOST_UTGAATT.toString(), mapUtgatt.getHendelsestype());
		assertEquals(JOURNALPOST_UTGAATT.toString(), mapUkjent.getHendelsestype());
		assertEquals(UTGAAR, mapUtgatt.getJournalpoststatus());
		assertEquals(UKJENTBRUKER, mapUkjent.getJournalpoststatus());
	}

	@Test
	public void shouldMapToTemaEndret() {
		JournalpostEndretEvent journalpostEndretEvent = createJournalpostEndretEvent("DAG", "FOR", "MO", "M", "U", "I");
		Joarkhendelse map = map(journalpostEndretEvent, buildGoldenGateEvent(now()));

		assertEquals(TEMA_ENDRET.toString(), map.getHendelsestype());
		assertEquals(MOTTATT, map.getJournalpoststatus());
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
		LocalDateTime dateTime = LocalDateTime.parse(operationTimestamp, formatter);

		GoldenGateEvent goldenGateEvent = buildGoldenGateEvent(dateTime);
		JournalpostEndretEvent event = createJournalpostEndretEvent("DAG", "FOR", "M", "M", "I", "I");
		Joarkhendelse mapMidlertidig = map(event, goldenGateEvent);

		String iso8610Time = "2021-09-22T12:47:02";
		assertEquals("1234567-" + iso8610Time, mapMidlertidig.getHendelsesId());
	}

	@Test
	public void shouldNotTruncateHendelseIdWhichHasZeroSeconds() {
		// hendelseId = journalpostId + op_ts som ISO8601-tid (yyyy-MM-ddTHH:mm:ss)
		//	format på op_ts: yyyy-MM-dd HH:mm:ss.SSSSSS
		//	eksempelverdi på op_ts: 2021-09-22 12:47:00.000000
		String operationTimestamp = "2021-09-22 12:47:00.000000";
		LocalDateTime dateTime = LocalDateTime.parse(operationTimestamp, formatter);

		GoldenGateEvent goldenGateEvent = buildGoldenGateEvent(dateTime);
		JournalpostEndretEvent event = createJournalpostEndretEvent("DAG", "FOR", "M", "M", "I", "I");
		Joarkhendelse mapMidlertidig = map(event, goldenGateEvent);

		String iso8610Time = "2021-09-22T12:47:00";
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