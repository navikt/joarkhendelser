package no.nav.joarkhendelser.consumer;

import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEvent;
import org.junit.jupiter.api.Test;

import static no.nav.joarkhendelser.consumer.GoldenGateEventUtils.createBasicColumns;
import static no.nav.joarkhendelser.consumer.GoldenGateEventUtils.createBasicEvent;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateEventFilter.shouldStopProcessingOfMessage;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.DELETE_OPERATION;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.INSERT_OPERATION;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.UPDATE_OPERATION;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GoldenGateEventFilterTest {

	@Test
	void shouldAllowRegularUpdate() {
		GoldenGateEvent event = createBasicEvent(UPDATE_OPERATION);
		event.setBefore(createBasicColumns());
		event.setAfter(createBasicColumns());

		assertFalse(shouldStopProcessingOfMessage(event));
	}

	@Test
	void shouldAllowRegularInsert() {
		GoldenGateEvent event = createBasicEvent(INSERT_OPERATION);
		event.setAfter(createBasicColumns());

		assertFalse(shouldStopProcessingOfMessage(event));
	}

	@Test
	void shouldStopDeleteOperation() {
		GoldenGateEvent event = createBasicEvent(DELETE_OPERATION);

		assertTrue(shouldStopProcessingOfMessage(event));
	}

	@Test
	void shouldStopUnknownOperation() {
		GoldenGateEvent event = createBasicEvent("X");

		assertTrue(shouldStopProcessingOfMessage(event));
	}

	@Test
	void shouldNotPassIfAfterIsMissingForInsert() {
		GoldenGateEvent event = createBasicEvent(INSERT_OPERATION);

		assertTrue(shouldStopProcessingOfMessage(event));
	}

	@Test
	void shouldNotPassIfAfterIsMissingForUpdate() {
		GoldenGateEvent event = createBasicEvent(UPDATE_OPERATION);
		event.setBefore(createBasicColumns());

		assertTrue(shouldStopProcessingOfMessage(event));
	}

	@Test
	void shouldNotPassIfBeforeIsMissingForUpdate() {
		GoldenGateEvent event = createBasicEvent(UPDATE_OPERATION);
		event.setAfter(createBasicColumns());

		assertTrue(shouldStopProcessingOfMessage(event));
	}

	@Test
	void shouldStopJournalposttypeUlikInngaaende() {
		GoldenGateEvent event = createBasicEvent(UPDATE_OPERATION);
		event.setBefore(createBasicColumns());
		event.setAfter(createBasicColumns());
		event.getAfter().setJournalposttype("U");

		assertTrue(shouldStopProcessingOfMessage(event));
	}
}
