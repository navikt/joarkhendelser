package no.nav.joarkhendelser.consumer.goldengate;

public class GoldenGateOperations {
	public static final String INSERT_OPERATION = "I";
	public static final String UPDATE_OPERATION = "U";
	public static final String DELETE_OPERATION = "D";

	public static String prettyPrintOperationName(String operation) {
		return switch (operation) {
			case INSERT_OPERATION -> "INSERT";
			case UPDATE_OPERATION -> "UPDATE";
			case DELETE_OPERATION -> "DELETE";
			default -> operation;
		};
	}
}
