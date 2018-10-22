package no.nav.joarkjournalfoeringhendelser.consumer.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

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
