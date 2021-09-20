package no.nav.joarkhendelser.consumer.kafka;

import no.nav.joarkhendelser.consumer.kafka.goldengate.GoldenGateEvent;
import org.junit.jupiter.api.Test;

import static no.nav.joarkhendelser.consumer.kafka.GoldenGateEventUtils.createBasicColumns;
import static no.nav.joarkhendelser.consumer.kafka.GoldenGateEventUtils.createBasicEvent;
import static no.nav.joarkhendelser.consumer.kafka.OracleSchema.DELETE_OPERATION;
import static no.nav.joarkhendelser.consumer.kafka.OracleSchema.INSERT_OPERATION;
import static no.nav.joarkhendelser.consumer.kafka.OracleSchema.UPDATE_OPERATION;
import static no.nav.joarkhendelser.consumer.kafka.goldengate.GoldenGateEventFilter.shouldStopProcessingOfMessage;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GoldenGateEventFilterTest {

	@Test
	void shouldAllowRegularUpdate() {
		GoldenGateEvent event = createBasicEvent(UPDATE_OPERATION);
		event.setBefore(createBasicColumns());
		event.setAfter(createBasicColumns());

		assertFalse(shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldAllowRegularInsert() {
		GoldenGateEvent event = createBasicEvent(INSERT_OPERATION);
		event.setAfter(createBasicColumns());

		assertFalse(shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldStopDeleteOperation() {
		GoldenGateEvent event = createBasicEvent(DELETE_OPERATION);

		assertTrue(shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldStopUnknownOperation() {
		GoldenGateEvent event = createBasicEvent("X");

		assertTrue(shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldNotPassIfAfterIsMissingForInsert() {
		GoldenGateEvent event = createBasicEvent(INSERT_OPERATION);

		assertTrue(shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldNotPassIfAfterIsMissingForUpdate() {
		GoldenGateEvent event = createBasicEvent(UPDATE_OPERATION);
		event.setBefore(createBasicColumns());

		assertTrue(shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldNotPassIfBeforeIsMissingForUpdate() {
		GoldenGateEvent event = createBasicEvent(UPDATE_OPERATION);
		event.setAfter(createBasicColumns());

		assertTrue(shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}

	@Test
	void shouldStopJournalposttypeUlikInngaaende() {
		GoldenGateEvent event = createBasicEvent(UPDATE_OPERATION);
		event.setBefore(createBasicColumns());
		event.setAfter(createBasicColumns());
		event.getAfter().setJournalposttype("U");

		assertTrue(shouldStopProcessingOfMessage(event, "test-topic-inn", 0, 1337));
	}
}
