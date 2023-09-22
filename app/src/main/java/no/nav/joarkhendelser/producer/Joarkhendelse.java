package no.nav.joarkhendelser.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Joarkhendelse {
	String hendelsesId;
	Integer versjon;
	String hendelsestype;
	Long journalpostId;
	String journalpoststatus;
	String temaGammelt;
	String temaNytt;
	String mottakskanal;
	String kanalreferanseId;
	String behandlingstema;
}
