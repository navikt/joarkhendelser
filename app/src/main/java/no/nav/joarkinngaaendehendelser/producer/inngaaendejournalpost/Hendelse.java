package no.nav.joarkinngaaendehendelser.producer.inngaaendejournalpost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hendelse {
    Long journalpostId;
    String fagomradeBefore;
    String fagomradeAfter;
    String journalpostStatus;
    String journalpostType;
}
