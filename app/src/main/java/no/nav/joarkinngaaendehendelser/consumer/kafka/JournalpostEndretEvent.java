package no.nav.joarkinngaaendehendelser.consumer.kafka;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalpostEndretEvent {
    String operation;
    Long journalpostId;
    String fagomradeBefore;
    String fagomradeAfter;
    String journalpostStatusBefore;
    String journalpostStatusAfter;
    String journalpostType;
    String mottaksKanal;
    String kanalReferanseId;
    Set<String> columnsChanged;
    Long timestamp;
}
