package no.nav.joarkhendelser.consumer.goldengate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoldenGateEvent {

	@JsonProperty("table")
	private String table;

	@JsonProperty("op_type")
	private String operation;

	@JsonProperty("op_ts")
	private String operationTimestamp;

	@JsonProperty("current_ts")
	private String currentTimestamp;

	@JsonProperty("pos")
	private String position;

	@JsonProperty("before")
	private GoldenGateColumns before;

	@JsonProperty("after")
	private GoldenGateColumns after;
}