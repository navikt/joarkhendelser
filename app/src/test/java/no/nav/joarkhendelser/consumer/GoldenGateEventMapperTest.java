package no.nav.joarkhendelser.consumer;

import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEvent;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEventMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static no.nav.joarkhendelser.consumer.GoldenGateEventUtils.dateTimeFormatter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class GoldenGateEventMapperTest {

	private final GoldenGateEventMapper goldenGateEventMapper = new GoldenGateEventMapper();

	@Test
	void shouldMapInsertMelding() throws Exception {
		String pathInsertMelding = "src/test/resources/__files/endelig_jf.json";
		String json = readFileAsString(pathInsertMelding);
		GoldenGateEvent goldenGateEvent = goldenGateEventMapper.mapToEvent(json);

		LocalDateTime operationTimestamp = LocalDateTime.parse("2018-10-09 11:06:02.123456", dateTimeFormatter);
		assertEquals(operationTimestamp, goldenGateEvent.getOperationTimestamp());

		assertEquals("I", goldenGateEvent.getOperation());
		assertEquals(1, goldenGateEvent.getAfter().getJournalpostId());
		assertEquals("SAK", goldenGateEvent.getAfter().getFagomraade());
		assertEquals("J", goldenGateEvent.getAfter().getJournalpoststatus());
		assertEquals("I", goldenGateEvent.getAfter().getJournalposttype());
		assertEquals("SKAN_NETS", goldenGateEvent.getAfter().getMottakskanal());
		assertNull(goldenGateEvent.getAfter().getKanalreferanseId());
		assertNull(goldenGateEvent.getAfter().getBehandlingstema());
	}

	@Test
	void shouldMapUpdateMelding() throws Exception {
		String pathUpdateMelding = "src/test/resources/__files/update.json";
		String json = readFileAsString(pathUpdateMelding);
		GoldenGateEvent goldenGateEvent = goldenGateEventMapper.mapToEvent(json);

		assertEquals("U", goldenGateEvent.getOperation());
		LocalDateTime operationTimestamp = LocalDateTime.parse("2021-09-21 15:21:53.000000", dateTimeFormatter);
		assertEquals(operationTimestamp, goldenGateEvent.getOperationTimestamp());

		assertEquals(453655940, goldenGateEvent.getBefore().getJournalpostId());
		assertEquals("MED", goldenGateEvent.getBefore().getFagomraade());
		assertEquals("M", goldenGateEvent.getBefore().getJournalpoststatus());
		assertEquals("I", goldenGateEvent.getBefore().getJournalposttype());
		assertEquals("EESSI", goldenGateEvent.getBefore().getMottakskanal());
		assertEquals("1395459_dc20eeeae6d54a3a8957f8e82d99041c_1", goldenGateEvent.getBefore().getKanalreferanseId());
		assertEquals("ab0269", goldenGateEvent.getBefore().getBehandlingstema());

		assertEquals(453655940, goldenGateEvent.getAfter().getJournalpostId());
		assertEquals("MED", goldenGateEvent.getAfter().getFagomraade());
		assertEquals("J", goldenGateEvent.getAfter().getJournalpoststatus());
		assertEquals("I", goldenGateEvent.getAfter().getJournalposttype());
		assertEquals("EESSI", goldenGateEvent.getAfter().getMottakskanal());
		assertEquals("1395459_dc20eeeae6d54a3a8957f8e82d99041c_1", goldenGateEvent.getBefore().getKanalreferanseId());
		assertEquals("ab0269", goldenGateEvent.getBefore().getBehandlingstema());
	}

	public static String readFileAsString(String file) throws Exception {
		return new String(Files.readAllBytes(Paths.get(file)));
	}
}
