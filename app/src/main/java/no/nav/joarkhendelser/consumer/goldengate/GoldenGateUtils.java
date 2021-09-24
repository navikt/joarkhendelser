package no.nav.joarkhendelser.consumer.goldengate;

import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.DELETE_OPERATION;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.INSERT_OPERATION;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.UPDATE_OPERATION;

public class GoldenGateUtils {

	public static String prettyPrintOperationName(String operation) {
		switch (operation) {
			case INSERT_OPERATION:
				return "INSERT";
			case UPDATE_OPERATION:
				return "UPDATE";
			case DELETE_OPERATION:
				return "DELETE";
			default:
				return operation;
		}
	}
}
