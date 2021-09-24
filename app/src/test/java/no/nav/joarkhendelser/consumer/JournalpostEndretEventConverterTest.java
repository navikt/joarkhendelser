package no.nav.joarkhendelser.consumer;

import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEvent;
import org.junit.jupiter.api.Test;

import static no.nav.joarkhendelser.consumer.GoldenGateEventUtils.createBasicColumns;
import static no.nav.joarkhendelser.consumer.GoldenGateEventUtils.createBasicColumnsWithTema;
import static no.nav.joarkhendelser.consumer.GoldenGateEventUtils.createBasicEvent;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.DELETE_OPERATION;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.INSERT_OPERATION;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.UPDATE_OPERATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JournalpostEndretEventConverterTest {

	private final JournalpostEndretEventConverter converter = new JournalpostEndretEventConverter();

	@Test
	public void shouldConvertUpdateOperation() {
		GoldenGateEvent event = createBasicEvent(UPDATE_OPERATION);
		event.setBefore(createBasicColumnsWithTema("FOR"));
		event.setAfter(createBasicColumnsWithTema("DAG"));

		JournalpostEndretEvent journalpostEndretEvent = converter.convertToEvent(event);
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

		JournalpostEndretEvent journalpostEndretEvent = converter.convertToEvent(event);

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

		JournalpostEndretEvent journalpostEndretEvent = converter.convertToEvent(event);
		assertNull(journalpostEndretEvent);
	}
}