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
    Long journalpostId;
    String fagomradeBefore;
    String fagomradeAfter;
    String journalpostStatus;
    String journalpostType;
}
