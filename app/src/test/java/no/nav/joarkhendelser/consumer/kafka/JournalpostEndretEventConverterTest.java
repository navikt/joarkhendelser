package no.nav.joarkhendelser.consumer.kafka;

import no.nav.joarkhendelser.consumer.kafka.goldengate.GoldenGateEvent;
import org.junit.jupiter.api.Test;

import static no.nav.joarkhendelser.consumer.kafka.GoldenGateEventUtils.createBasicColumns;
import static no.nav.joarkhendelser.consumer.kafka.GoldenGateEventUtils.createBasicColumnsWithTema;
import static no.nav.joarkhendelser.consumer.kafka.GoldenGateEventUtils.createBasicEvent;
import static no.nav.joarkhendelser.consumer.kafka.OracleSchema.DELETE_OPERATION;
import static no.nav.joarkhendelser.consumer.kafka.OracleSchema.INSERT_OPERATION;
import static no.nav.joarkhendelser.consumer.kafka.OracleSchema.UPDATE_OPERATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JournalpostEndretEventConverterTest {

	private final JournalpostEndretEventConverter converter = new JournalpostEndretEventConverter();

	@Test
	public void shouldConvertUpdateOperation() {
		GoldenGateEvent event = createBasicEvent(UPDATE_OPERATION);
		event.setBefore(createBasicColumnsWithTema("FOR"));
		event.setAfter(createBasicColumnsWithTema("DAG"));

		JournalpostEndretEvent journalpostEndretEvent = converter.convertToEvent(event, "test-topic-inn", 0, 1337);
		assertEquals(453655940L, journalpostEndretEvent.getJournalpostId());
		assertEquals("U", journalpostEndretEvent.getOperation());
		assertEquals("M", journalpostEndretEvent.getJournalpostStatusAfter());
		assertEquals("FOR", journalpostEndretEvent.getFagomradeBefore());
		assertEquals("DAG", journalpostEndretEvent.getFagomradeAfter());
	}

	@Test
	public void shouldConvertInsertOperation() {
		GoldenGateEvent event = createBasicEvent(INSERT_OPERATION);
		event.setAfter(createBasicColumnsWithTema("DAG"));

		JournalpostEndretEvent journalpostEndretEvent = converter.convertToEvent(event, "test-topic-inn", 0, 1337);

		assertEquals(453655940L, journalpostEndretEvent.getJournalpostId());
		assertEquals("I", journalpostEndretEvent.getOperation());
		assertEquals("M", journalpostEndretEvent.getJournalpostStatusAfter());
		assertEquals("DAG", journalpostEndretEvent.getFagomradeAfter());
		assertEquals("", journalpostEndretEvent.getFagomradeBefore());
	}

	@Test
	public void shouldReturnForOperationsBesidesInsertAndUpdate() {
		GoldenGateEvent event = createBasicEvent(DELETE_OPERATION);
		event.setBefore(createBasicColumns());

		JournalpostEndretEvent journalpostEndretEvent = converter.convertToEvent(event, "test-topic-inn", 0, 1337);
		assertNull(journalpostEndretEvent);
	}
}