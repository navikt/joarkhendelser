package no.nav.joarkhendelser.consumer;

import lombok.extern.slf4j.Slf4j;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateColumns;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEvent;
import org.springframework.stereotype.Component;

import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.INSERT_OPERATION;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.UPDATE_OPERATION;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateUtils.prettyPrintOperationName;
import static org.apache.logging.log4j.util.Strings.isNotEmpty;

@Component
@Slf4j
public class JournalpostEndretEventConverter {

	public JournalpostEndretEvent convertToEvent(GoldenGateEvent goldenGateEvent) {

		String operation = goldenGateEvent.getOperation();
		if (!(UPDATE_OPERATION.equalsIgnoreCase(operation) || INSERT_OPERATION.equalsIgnoreCase(operation))) {
			log.warn("Mottok ukjent operasjon={} for journalpost. Avslutter behandling av hendelse", prettyPrintOperationName(operation));
			return null;
		}

		GoldenGateColumns after = goldenGateEvent.getAfter();
		GoldenGateColumns before = goldenGateEvent.getBefore();
		Integer journalpostId = after.getJournalpostId();

		log.info("Mottok {}-event for journalpostId={}", prettyPrintOperationName(operation), journalpostId);

		if (UPDATE_OPERATION.equalsIgnoreCase(operation)) {
			return JournalpostEndretEvent.builder()
					.journalpostId(journalpostId.longValue())
					.operation(operation)
					.fagomradeBefore(getStringOrEmptyString(before.getFagomraade()))
					.fagomradeAfter(getStringOrEmptyString(after.getFagomraade()))
					.journalpostStatusBefore(getStringOrEmptyString(before.getJournalpoststatus()))
					.journalpostStatusAfter(getStringOrEmptyString(after.getJournalpoststatus()))
					.journalpostType(getStringOrEmptyString(after.getJournalposttype()))
					.mottaksKanal(getStringOrEmptyString(after.getMottakskanal()))
					.kanalReferanseId(getStringOrEmptyString(after.getKanalreferanseId()))
					.behandlingsTema(getStringOrEmptyString(after.getBehandlingstema()))
					.build();
		} else if (INSERT_OPERATION.equalsIgnoreCase(operation)) {
			return JournalpostEndretEvent.builder()
					.journalpostId(journalpostId.longValue())
					.operation(operation)
					.fagomradeBefore("")
					.fagomradeAfter(getStringOrEmptyString(after.getFagomraade()))
					.journalpostStatusBefore("")
					.journalpostStatusAfter(getStringOrEmptyString(after.getJournalpoststatus()))
					.journalpostType(getStringOrEmptyString(after.getJournalposttype()))
					.mottaksKanal(getStringOrEmptyString(after.getMottakskanal()))
					.kanalReferanseId(getStringOrEmptyString(after.getKanalreferanseId()))
					.behandlingsTema(getStringOrEmptyString(after.getBehandlingstema()))
					.build();
		}
		return null;
	}

	private String getStringOrEmptyString(String value) {
		return isNotEmpty(value) ? value : "";
	}
}
