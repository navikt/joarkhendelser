package no.nav.joarkinngaaendehendelser.producer;

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
    String versjon;
    InngaaendeHendelsesType hendelsesType;
    String journalpostId;
    String journalpostStatus;
    String temaGammelt;
    String temaNytt;
    String journalpostType;
    String mottaksKanal;
    String kanalReferanseId;
    Long timestamp;
}
