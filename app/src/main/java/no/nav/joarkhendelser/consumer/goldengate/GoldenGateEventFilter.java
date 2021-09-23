package no.nav.joarkhendelser.consumer.goldengate;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateUtils.prettyPrintOperationName;
import static no.nav.joarkhendelser.consumer.JournalpostType.INNGAAENDE;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.INSERT_OPERATION;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.UPDATE_OPERATION;

@Slf4j
public class GoldenGateEventFilter {

	private static final List<String> allowedOperations = Arrays.asList(INSERT_OPERATION, UPDATE_OPERATION);

	public static boolean shouldStopProcessingOfMessage(GoldenGateEvent goldenGateEvent) {

		String operation = goldenGateEvent.getOperation();

		if (!allowedOperations.contains(operation)) {
			log.info("Forkaster Golden Gate-melding med operasjon {}", prettyPrintOperationName(operation));
			return true;
		}

		if ((INSERT_OPERATION.equalsIgnoreCase(operation) || UPDATE_OPERATION.equalsIgnoreCase(operation)) && goldenGateEvent.getAfter() == null) {
			log.warn("Forkaster Golden Gate-melding med operasjon {} som mangler after-feltet", prettyPrintOperationName(operation));
			return true;
		}

		if (UPDATE_OPERATION.equalsIgnoreCase(operation) && goldenGateEvent.getBefore() == null) {
			log.warn("Forkaster Golden Gate-melding med operasjon {} som mangler before-feltet", prettyPrintOperationName(operation));
			return true;
		}

		if (!INNGAAENDE.equalsIgnoreCase((goldenGateEvent.getAfter().getJournalposttype()))) {
			log.info("Forkaster Golden Gate-melding med journalposttype ulik I (inngaaende)");
			return true;
		}

		return false;
	}
}
