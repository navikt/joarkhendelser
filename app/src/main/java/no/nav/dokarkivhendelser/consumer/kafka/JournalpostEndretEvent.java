package no.nav.dokarkivhendelser.consumer.kafka;

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
    Long journalpostId;
    String fagomrade;
    String journalpostStatus;
    String journalpostType;
    String operation;
    Set<String> columnsChanged;
}

