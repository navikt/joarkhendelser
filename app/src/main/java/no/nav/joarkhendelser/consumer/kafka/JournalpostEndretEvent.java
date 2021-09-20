package no.nav.joarkhendelser.consumer.kafka;

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
	String behandlingsTema;
	Long operationTimestamp;
	Long currentTimestamp;
}
