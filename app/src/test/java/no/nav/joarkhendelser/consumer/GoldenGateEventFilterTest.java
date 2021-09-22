package no.nav.joarkhendelser.consumer;

import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEvent;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEventFilter;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GoldenGateEventFilterTest {

	@Test
	void shouldAllowRegularUpdate() {
		GoldenGateEvent event = GoldenGateEventUtils.createBasicEvent(GoldenGateOperations.UPDATE_OPERATION);
		event.setBefore(GoldenGateEventUtils.createBasicColumns());
		event.setAfter(GoldenGateEventUtils.createBasicColumns());

		Assertions.assertFalse(GoldenGateEventFilter.shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldAllowRegularInsert() {
		GoldenGateEvent event = GoldenGateEventUtils.createBasicEvent(GoldenGateOperations.INSERT_OPERATION);
		event.setAfter(GoldenGateEventUtils.createBasicColumns());

		Assertions.assertFalse(GoldenGateEventFilter.shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldStopDeleteOperation() {
		GoldenGateEvent event = GoldenGateEventUtils.createBasicEvent(GoldenGateOperations.DELETE_OPERATION);

		Assertions.assertTrue(GoldenGateEventFilter.shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldStopUnknownOperation() {
		GoldenGateEvent event = GoldenGateEventUtils.createBasicEvent("X");

		Assertions.assertTrue(GoldenGateEventFilter.shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldNotPassIfAfterIsMissingForInsert() {
		GoldenGateEvent event = GoldenGateEventUtils.createBasicEvent(GoldenGateOperations.INSERT_OPERATION);

		Assertions.assertTrue(GoldenGateEventFilter.shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldNotPassIfAfterIsMissingForUpdate() {
		GoldenGateEvent event = GoldenGateEventUtils.createBasicEvent(GoldenGateOperations.UPDATE_OPERATION);
		event.setBefore(GoldenGateEventUtils.createBasicColumns());

		Assertions.assertTrue(GoldenGateEventFilter.shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldNotPassIfBeforeIsMissingForUpdate() {
		GoldenGateEvent event = GoldenGateEventUtils.createBasicEvent(GoldenGateOperations.UPDATE_OPERATION);
		event.setAfter(GoldenGateEventUtils.createBasicColumns());

		Assertions.assertTrue(GoldenGateEventFilter.shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldStopJournalposttypeUlikInngaaende() {
		GoldenGateEvent event = GoldenGateEventUtils.createBasicEvent(GoldenGateOperations.UPDATE_OPERATION);
		event.setBefore(GoldenGateEventUtils.createBasicColumns());
		event.setAfter(GoldenGateEventUtils.createBasicColumns());
		event.getAfter().setJournalposttype("U");

		Assertions.assertTrue(GoldenGateEventFilter.shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}
}
