package no.nav.joarkhendelser.consumer.goldengate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoldenGateEvent {

	@JsonProperty("op_type")
	private String operation;

	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss.SSSSSS")
	@JsonProperty("op_ts")
	private LocalDateTime operationTimestamp;

	@JsonProperty("before")
	private GoldenGateColumns before;

	@JsonProperty("after")
	private GoldenGateColumns after;
}