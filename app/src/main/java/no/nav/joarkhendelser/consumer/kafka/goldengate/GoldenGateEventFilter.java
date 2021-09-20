package no.nav.joarkhendelser.consumer.kafka.goldengate;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

import static no.nav.joarkhendelser.consumer.kafka.goldengate.GoldenGateUtils.prettyPrintOperationName;
import static no.nav.joarkhendelser.consumer.kafka.JournalpostType.INNGAAENDE;
import static no.nav.joarkhendelser.consumer.kafka.OracleSchema.INSERT_OPERATION;
import static no.nav.joarkhendelser.consumer.kafka.OracleSchema.UPDATE_OPERATION;

@Slf4j
public class GoldenGateEventFilter {

	private static final List<String> allowedOperations = Arrays.asList(INSERT_OPERATION, UPDATE_OPERATION);

	public static boolean shouldStopProcessingOfMessage(GoldenGateEvent goldenGateEvent, String topic, int partition, int offset) {

		String operation = goldenGateEvent.getOperation();

		if (!allowedOperations.contains(operation)) {
			log.info("Golden Gate-message with operation {} is ignored in filter with topic: {}, partition: {}, offset: {}", prettyPrintOperationName(operation), topic, partition, offset);
			return true;
		}

		if ((INSERT_OPERATION.equalsIgnoreCase(operation) || UPDATE_OPERATION.equalsIgnoreCase(operation)) && goldenGateEvent.getAfter() == null) {
			log.warn("Golden Gate-message missing after values with topic: {}, partition: {}, offset: {}", topic, partition, offset);
			return true;
		}

		if (UPDATE_OPERATION.equalsIgnoreCase(operation) && goldenGateEvent.getBefore() == null) {
			log.warn("Golden Gate-message missing before values with topic: {}, partition: {}, offset: {}", topic, partition, offset);
			return true;
		}

		if (!INNGAAENDE.equalsIgnoreCase((goldenGateEvent.getAfter().getJournalposttype()))) {
			log.info("Journalpost event with journalposttype not equal to I (inngaaende) is ignored in filter with topic: {}, partition: {}, offset: {}", topic, partition, offset);
			return true;
		}

		return false;
	}
}
