package no.nav.joarkhendelser.consumer.kafka.goldengate;

public class GoldenGateUtils {

	public static String prettyPrintOperationName(String operation) {
		switch (operation) {
			case "I":
				return "INSERT";
			case "U":
				return "UPDATE";
			case "D":
				return "DELETE";
			default:
				return operation;
		}
	}
}
