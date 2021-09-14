package no.nav.joarkhendelser.producer;

import no.nav.joarkhendelser.consumer.kafka.JoarkJournalpostStatus;
import no.nav.joarkhendelser.consumer.kafka.JournalpostEndretEvent;
import no.nav.joarkhendelser.consumer.kafka.JournalpostStatus;

import java.util.UUID;

import static net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils.isNotEmpty;
import static no.nav.joarkhendelser.consumer.kafka.JournalpostRetning.INNGAAENDE;
import static no.nav.joarkhendelser.consumer.kafka.OracleSchema.INSERT_OPERATION;
import static no.nav.joarkhendelser.consumer.kafka.OracleSchema.UPDATE_OPERATION;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.ENDELIG_JOURNALFORT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.JOURNALPOST_MOTTATT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.JOURNALPOST_UTGATT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.TEMA_ENDRET;

/**
 * @author Martin Burheim Tingstad, Visma Consulting.
 */
public class JournalpostEndretInngaaendeHendelseMapper {
	public static InngaaendeHendelse map(JournalpostEndretEvent event) {
		InngaaendeHendelsesType inngaaendeHendelsesType = finnHendelsesType(event);

		return inngaaendeHendelsesType != null ?
				InngaaendeHendelse.builder()
						.hendelsesId(UUID.randomUUID().toString())
						.versjon(1)
						.temaNytt(event.getFagomradeAfter())
						.temaGammelt(event.getFagomradeBefore())
						.journalpostId(event.getJournalpostId())
						.kanalReferanseId(event.getKanalReferanseId())
						.mottaksKanal(event.getMottaksKanal())
						.behandlingsTema(event.getBehandlingsTema())
						.journalpostStatus(mapJournalstatus(event.getJournalpostStatusAfter()))
						.hendelsesType(inngaaendeHendelsesType.toString())
						.operationTimestamp(event.getOperationTimestamp())
						.currentTimestamp(event.getCurrentTimestamp())
						.build() : null;
	}

	private static String mapJournalstatus(String journalpostStatus) {
		if(JoarkJournalpostStatus.JOURNALFORT.equalsIgnoreCase(journalpostStatus)) {
			return JournalpostStatus.JOURNALFORT;
		} else if (JoarkJournalpostStatus.MIDLERTIDIG.equalsIgnoreCase(journalpostStatus) || JoarkJournalpostStatus.MOTTATT.equalsIgnoreCase(journalpostStatus)) {
			return JournalpostStatus.MOTTATT;
		} else if (JoarkJournalpostStatus.OPPLASTINGDOKUMENT.equalsIgnoreCase(journalpostStatus)) {
			return JournalpostStatus.OPPLASTINGDOKUMENT;
		} else if (JoarkJournalpostStatus.UKJENTBRUKER.equalsIgnoreCase(journalpostStatus)) {
			return JournalpostStatus.UKJENTBRUKER;
		} else if (JoarkJournalpostStatus.UTGAR.equalsIgnoreCase(journalpostStatus)) {
			return JournalpostStatus.UTGAR;
		}
		return null;
	}

	private static InngaaendeHendelsesType finnHendelsesType(JournalpostEndretEvent event) {
		InngaaendeHendelsesType hendelsesType = null;

		if (!isInngaaende(event)) {
			hendelsesType = null;
		} else if (isMidlertidigJournalfort(event)) {
			hendelsesType = JOURNALPOST_MOTTATT;
		} else if (isEndeligJournalfort(event)) {
			hendelsesType = ENDELIG_JOURNALFORT;
		} else if (isJournalpostUtgatt(event)) {
			hendelsesType = JOURNALPOST_UTGATT;
		} else if (isTemaEndret(event)) {
			hendelsesType = TEMA_ENDRET;
		}
		return hendelsesType;
	}

	private static boolean isMidlertidigJournalfort(JournalpostEndretEvent event) {
		return (isMottatt(event) || isMidlertidig(event)) &&
				(isInsertOperation(event) || isUpdateFromOpplastingDokument(event));
	}

	private static boolean isUpdateFromOpplastingDokument(JournalpostEndretEvent event) {
		return isUpdateOperation(event) && wasOpplastingDokument(event);
	}

	private static boolean isTemaEndret(JournalpostEndretEvent event) {
		return isUpdateOperation(event) && hasChangedFagomrade(event) &&
				(isMottatt(event) || isMidlertidig(event));
	}

	private static boolean isEndeligJournalfort(JournalpostEndretEvent event) {
		return isJournalfort(event) &&
				((isInsertOperation(event)) || (isUpdateOperation(event) && wasMidlertidig(event)));
	}

	private static boolean isJournalpostUtgatt(JournalpostEndretEvent event) {
		return isUpdateOperation(event) &&
				hasChangedJournalpostStatusToUtgarOrUkjentbruker(event);
	}

	private static boolean hasChangedFagomrade(JournalpostEndretEvent event) {
		return isNotEmpty(event.getFagomradeBefore()) &&
				isNotEmpty(event.getFagomradeAfter()) &&
				!event.getFagomradeBefore().equalsIgnoreCase(event.getFagomradeAfter());
	}

	private static boolean hasChangedJournalpostStatusToUtgarOrUkjentbruker(JournalpostEndretEvent event) {
		return isNotEmpty(event.getJournalpostStatusBefore()) &&
				(JoarkJournalpostStatus.UTGAR.equalsIgnoreCase(event.getJournalpostStatusAfter()) || JoarkJournalpostStatus.UKJENTBRUKER.equalsIgnoreCase(event.getJournalpostStatusAfter()));
	}

	private static boolean isInsertOperation(JournalpostEndretEvent event) {
		return INSERT_OPERATION.equalsIgnoreCase(event.getOperation());
	}

	private static boolean isUpdateOperation(JournalpostEndretEvent event) {
		return UPDATE_OPERATION.equalsIgnoreCase(event.getOperation());
	}

	private static boolean isInngaaende(JournalpostEndretEvent event) {
		return INNGAAENDE.equalsIgnoreCase(event.getJournalpostType());
	}

	private static boolean isJournalfort(JournalpostEndretEvent event) {
		return JoarkJournalpostStatus.JOURNALFORT.equalsIgnoreCase(event.getJournalpostStatusAfter());
	}

	private static boolean isMottatt(JournalpostEndretEvent event) {
		return JoarkJournalpostStatus.MOTTATT.equalsIgnoreCase(event.getJournalpostStatusAfter());
	}

	private static boolean isMidlertidig(JournalpostEndretEvent event) {
		return JoarkJournalpostStatus.MIDLERTIDIG.equalsIgnoreCase(event.getJournalpostStatusAfter());
	}

	private static boolean wasMidlertidig(JournalpostEndretEvent event) {
		return JoarkJournalpostStatus.MIDLERTIDIG.equalsIgnoreCase(event.getJournalpostStatusBefore());
	}

	private static boolean wasOpplastingDokument(JournalpostEndretEvent event) {
		return JoarkJournalpostStatus.OPPLASTINGDOKUMENT.equalsIgnoreCase(event.getJournalpostStatusBefore());
	}
}
