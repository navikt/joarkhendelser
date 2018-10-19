package no.nav.joarkjournalfoeringhendelser.consumer.kafka;

/**
 * @author Martin Burheim Tingstad, Visma Consulting.
 */
public class OracleSchema {
	public static final String INSERT_OPERATION = "I";
	public static final String UPDATE_OPERATION = "U";
	public static final String DELETE_OPERATION = "D";
	static final String OPERATION_TYPE = "op_type";
	static final String OPERATION_TIMESTAMP = "op_ts";
}
