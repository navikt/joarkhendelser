package no.nav.joarkhendelser.consumer;

import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEvent;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations;
import org.junit.jupiter.api.Test;

import static no.nav.joarkhendelser.consumer.GoldenGateEventUtils.createBasicColumns;
import static no.nav.joarkhendelser.consumer.GoldenGateEventUtils.createBasicEvent;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateEventFilter.shouldStopProcessingOfMessage;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GoldenGateEventFilterTest {

	@Test
	void shouldAllowRegularUpdate() {
		GoldenGateEvent event = createBasicEvent(GoldenGateOperations.UPDATE_OPERATION);
		event.setBefore(createBasicColumns());
		event.setAfter(createBasicColumns());

		assertFalse(shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldAllowRegularInsert() {
		GoldenGateEvent event = createBasicEvent(GoldenGateOperations.INSERT_OPERATION);
		event.setAfter(createBasicColumns());

		assertFalse(shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldStopDeleteOperation() {
		GoldenGateEvent event = createBasicEvent(GoldenGateOperations.DELETE_OPERATION);

		assertTrue(shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldStopUnknownOperation() {
		GoldenGateEvent event = createBasicEvent("X");

		assertTrue(shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldNotPassIfAfterIsMissingForInsert() {
		GoldenGateEvent event = createBasicEvent(GoldenGateOperations.INSERT_OPERATION);

		assertTrue(shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldNotPassIfAfterIsMissingForUpdate() {
		GoldenGateEvent event = createBasicEvent(GoldenGateOperations.UPDATE_OPERATION);
		event.setBefore(createBasicColumns());

		assertTrue(shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldNotPassIfBeforeIsMissingForUpdate() {
		GoldenGateEvent event = createBasicEvent(GoldenGateOperations.UPDATE_OPERATION);
		event.setAfter(createBasicColumns());

		assertTrue(shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldStopJournalposttypeUlikInngaaende() {
		GoldenGateEvent event = createBasicEvent(GoldenGateOperations.UPDATE_OPERATION);
		event.setBefore(createBasicColumns());
		event.setAfter(createBasicColumns());
		event.getAfter().setJournalposttype("U");

		assertTrue(shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}
}
