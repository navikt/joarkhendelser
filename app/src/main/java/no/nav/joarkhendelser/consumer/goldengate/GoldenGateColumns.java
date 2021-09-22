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
public class GoldenGateColumns {

	@JsonProperty("JOURNALPOST_ID")
	private Integer journalpostId;

	@JsonProperty("K_FAGOMRADE")
	private String fagomraade;

	@JsonProperty("K_JOURNAL_S")
	private String journalpoststatus;

	@JsonProperty("K_JOURNALPOST_T")
	private String journalposttype;

	@JsonProperty("K_MOTTAKS_KANAL")
	private String mottakskanal;

	@JsonProperty("KANAL_REFERANSE_ID")
	private String kanalreferanseId;

	@JsonProperty("K_BEHANDLINGSTEMA")
	private String behandlingstema;
}
