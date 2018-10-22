package no.nav.joarkjournalfoeringhendelser.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InngaaendeHendelse {
	String hendelsesId;
	Integer versjon;
	String hendelsesType;
	Long journalpostId;
	String journalpostStatus;
	String temaGammelt;
	String temaNytt;
	String mottaksKanal;
	String kanalReferanseId;
	Long timestamp;
}
