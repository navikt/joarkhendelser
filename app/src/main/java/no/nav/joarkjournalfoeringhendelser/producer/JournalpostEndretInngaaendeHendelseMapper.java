package no.nav.joarkjournalfoeringhendelser.producer;

import static no.nav.joarkjournalfoeringhendelser.consumer.kafka.JournalpostStatus.INNGAAENDE;
import static no.nav.joarkjournalfoeringhendelser.consumer.kafka.JournalpostStatus.JOURNALFORT;
import static no.nav.joarkjournalfoeringhendelser.consumer.kafka.JournalpostStatus.MIDLERTIDIG;
import static no.nav.joarkjournalfoeringhendelser.consumer.kafka.JournalpostStatus.MOTTATT;
import static no.nav.joarkjournalfoeringhendelser.consumer.kafka.JournalpostStatus.UKJENTBRUKER;
import static no.nav.joarkjournalfoeringhendelser.consumer.kafka.JournalpostStatus.UTGAR;
import static no.nav.joarkjournalfoeringhendelser.consumer.kafka.OracleSchema.INSERT_OPERATION;
import static no.nav.joarkjournalfoeringhendelser.consumer.kafka.OracleSchema.UPDATE_OPERATION;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import no.nav.joarkjournalfoeringhendelser.consumer.kafka.JournalpostEndretEvent;

import java.util.UUID;

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
						.journalpostStatus(event.getJournalpostStatusAfter())
						.hendelsesType(inngaaendeHendelsesType.toString())
						.timestamp(event.getTimestamp())
						.build() : null;
	}

	private static InngaaendeHendelsesType finnHendelsesType(JournalpostEndretEvent event) {
		InngaaendeHendelsesType hendelsesType = null;

		if (!isInngaaende(event)) {
			hendelsesType = null;
		} else if (isMidlertidigJournalfort(event)) {
			hendelsesType = InngaaendeHendelsesType.MIDLERTIDIG_JOURNALFORT;
		} else if (isTemaEndret(event)) {
			hendelsesType = InngaaendeHendelsesType.TEMA_ENDRET;
		} else if (isEndeligJournalfort(event)) {
			hendelsesType = InngaaendeHendelsesType.ENDELIG_JOURNALFORT;
		} else if (isJournalpostUtgatt(event)) {
			hendelsesType = InngaaendeHendelsesType.JOURNALPOST_UTGATT;
		}

		return hendelsesType;
	}

	private static boolean isMidlertidigJournalfort(JournalpostEndretEvent event) {
		return isInsertOperation(event) &&
				(isMottatt(event) || isMidlertidig(event));
	}

	private static boolean isTemaEndret(JournalpostEndretEvent event) {
		return isUpdateOperation(event) &&
				hasChangedFagomrade(event);
	}

	private static boolean isEndeligJournalfort(JournalpostEndretEvent event) {
		return wasMidlertidig(event) &&
				isJournalfort(event);
	}

	private static boolean isJournalpostUtgatt(JournalpostEndretEvent event) {
		return isUpdateOperation(event) &&
				hasChangedJournalpostStatusToUtgarOrUkjentbruker(event);
	}

	private static boolean hasChangedFagomrade(JournalpostEndretEvent event) {
		return StringUtils.isNotEmpty(event.getFagomradeBefore()) &&
				StringUtils.isNotEmpty(event.getFagomradeAfter()) &&
				!event.getFagomradeBefore().equalsIgnoreCase(event.getFagomradeAfter());
	}

	private static boolean hasChangedJournalpostStatusToUtgarOrUkjentbruker(JournalpostEndretEvent event) {
		return StringUtils.isNotEmpty(event.getJournalpostStatusBefore()) &&
				(UTGAR.equalsIgnoreCase(event.getJournalpostStatusAfter()) || UKJENTBRUKER.equalsIgnoreCase(event.getJournalpostStatusAfter()));
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
		return JOURNALFORT.equalsIgnoreCase(event.getJournalpostStatusAfter());
	}

	private static boolean isMottatt(JournalpostEndretEvent event) {
		return MOTTATT.equalsIgnoreCase(event.getJournalpostStatusAfter());
	}

	private static boolean isMidlertidig(JournalpostEndretEvent event) {
		return MIDLERTIDIG.equalsIgnoreCase(event.getJournalpostStatusAfter());
	}

	private static boolean wasMidlertidig(JournalpostEndretEvent event) {
		return MIDLERTIDIG.equalsIgnoreCase(event.getJournalpostStatusBefore());
	}
}
