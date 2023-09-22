package no.nav.joarkhendelser.producer;

import lombok.extern.slf4j.Slf4j;
import no.nav.joarkhendelser.consumer.JournalpostEndretEvent;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEvent;

import java.time.format.DateTimeFormatter;

import static no.nav.joarkhendelser.consumer.Journalposttype.INNGAAENDE;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.INSERT_OPERATION;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.UPDATE_OPERATION;
import static no.nav.joarkhendelser.producer.Hendelsestype.ENDELIG_JOURNALFOERT;
import static no.nav.joarkhendelser.producer.Hendelsestype.JOURNALPOST_MOTTATT;
import static no.nav.joarkhendelser.producer.Hendelsestype.JOURNALPOST_UTGAATT;
import static no.nav.joarkhendelser.producer.Hendelsestype.TEMA_ENDRET;
import static no.nav.joarkhendelser.producer.JoarkJournalpoststatus.JOURNALFOERT;
import static no.nav.joarkhendelser.producer.JoarkJournalpoststatus.MIDLERTIDIG;
import static no.nav.joarkhendelser.producer.JoarkJournalpoststatus.MOTTATT;
import static no.nav.joarkhendelser.producer.JoarkJournalpoststatus.OPPLASTINGDOKUMENT;
import static no.nav.joarkhendelser.producer.JoarkJournalpoststatus.UKJENTBRUKER;
import static no.nav.joarkhendelser.producer.JoarkJournalpoststatus.UTGAAR;
import static org.apache.logging.log4j.util.Strings.isNotEmpty;

@Slf4j
public class JoarkhendelseMapper {

	private static final DateTimeFormatter formatterWhereSecondsArePreservedIfZero = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	public static Joarkhendelse map(JournalpostEndretEvent event, GoldenGateEvent goldenGateEvent) {
		Hendelsestype hendelsestype = mapHendelsestype(event);

		if (hendelsestype == null) {
			log.info("Hendelse med operation={}, journalposttype={}, journalpoststatusBefore={}, journalpoststatusAfter={} er ikke en av dei fire relevante hendelsene.",
					event.getOperation(), event.getJournalpostType(), event.getJournalpostStatusBefore(), event.getJournalpostStatusAfter());

			return null;
		}

		return Joarkhendelse.builder()
				.hendelsesId(buildHendelseId(event, goldenGateEvent)) // journalpostId + operationTimestamp på ISO8601-format (yyyy-MM-ddTHH:mm:ss)
				.versjon(1)
				.temaNytt(event.getFagomradeAfter())
				.temaGammelt(event.getFagomradeBefore())
				.journalpostId(event.getJournalpostId())
				.kanalreferanseId(event.getKanalReferanseId())
				.mottakskanal(event.getMottaksKanal())
				.behandlingstema(event.getBehandlingsTema())
				.journalpoststatus(mapJournalstatus(event.getJournalpostStatusAfter()))
				.hendelsestype(hendelsestype.toString())
				.build();
	}

	private static String mapJournalstatus(String journalpostStatus) {

		return switch (journalpostStatus) {
			case JOURNALFOERT -> Journalpoststatus.JOURNALFOERT;
			case MIDLERTIDIG, MOTTATT -> Journalpoststatus.MOTTATT;
			case OPPLASTINGDOKUMENT -> Journalpoststatus.OPPLASTINGDOKUMENT;
			case UKJENTBRUKER -> Journalpoststatus.UKJENTBRUKER;
			case UTGAAR -> Journalpoststatus.UTGAAR;
			default -> null;
		};
	}

	private static Hendelsestype mapHendelsestype(JournalpostEndretEvent event) {
		Hendelsestype hendelsestype = null;

		if (!typeErInngaaende(event)) {
			return hendelsestype;
		}

		if (erJournalpostMottatt(event)) {
			hendelsestype = JOURNALPOST_MOTTATT;
		} else if (erEndeligJournalfoert(event)) {
			hendelsestype = ENDELIG_JOURNALFOERT;
		} else if (erJournalpostUtgaatt(event)) {
			hendelsestype = JOURNALPOST_UTGAATT;
		} else if (erTemaEndret(event)) {
			hendelsestype = TEMA_ENDRET;
		}

		return hendelsestype;
	}

	private static boolean erJournalpostMottatt(JournalpostEndretEvent event) {
		return nyStatusErMottattEllerMidlertidig(event) && (operasjonErInsert(event) || erStatusOppdatertFraOpplastingDokument(event));
	}

	private static boolean erStatusOppdatertFraOpplastingDokument(JournalpostEndretEvent event) {
		return operasjonErUpdate(event) && forrigeStatusVarOpplastingDokument(event);
	}

	private static boolean erTemaEndret(JournalpostEndretEvent event) {
		return nyStatusErMottattEllerMidlertidig(event) && operasjonErUpdate(event) && harEndretFagomraade(event);
	}

	private static boolean erEndeligJournalfoert(JournalpostEndretEvent event) {
		return (operasjonErInsert(event) || (operasjonErUpdate(event) && forrigeStatusVarMidlertidig(event))) && nyStatusErJournalfoert(event);
	}

	private static boolean erJournalpostUtgaatt(JournalpostEndretEvent event) {
		return operasjonErUpdate(event) && harEndretStatusTilUtgaarEllerUkjentbruker(event);
	}

	private static boolean harEndretFagomraade(JournalpostEndretEvent event) {
		return isNotEmpty(event.getFagomradeBefore()) &&
			   isNotEmpty(event.getFagomradeAfter()) &&
			   !event.getFagomradeBefore().equalsIgnoreCase(event.getFagomradeAfter());
	}

	private static boolean harEndretStatusTilUtgaarEllerUkjentbruker(JournalpostEndretEvent event) {
		return isNotEmpty(event.getJournalpostStatusBefore()) &&
			   (UTGAAR.equalsIgnoreCase(event.getJournalpostStatusAfter()) || UKJENTBRUKER.equalsIgnoreCase(event.getJournalpostStatusAfter()));
	}

	private static boolean operasjonErInsert(JournalpostEndretEvent event) {
		return INSERT_OPERATION.equalsIgnoreCase(event.getOperation());
	}

	private static boolean operasjonErUpdate(JournalpostEndretEvent event) {
		return UPDATE_OPERATION.equalsIgnoreCase(event.getOperation());
	}

	private static boolean typeErInngaaende(JournalpostEndretEvent event) {
		return INNGAAENDE.equalsIgnoreCase(event.getJournalpostType());
	}

	private static boolean nyStatusErJournalfoert(JournalpostEndretEvent event) {
		return JOURNALFOERT.equalsIgnoreCase(event.getJournalpostStatusAfter());
	}

	private static boolean nyStatusErMottattEllerMidlertidig(JournalpostEndretEvent event) {
		return MOTTATT.equalsIgnoreCase(event.getJournalpostStatusAfter())
				|| MIDLERTIDIG.equalsIgnoreCase(event.getJournalpostStatusAfter());
	}

	private static boolean forrigeStatusVarMidlertidig(JournalpostEndretEvent event) {
		return MIDLERTIDIG.equalsIgnoreCase(event.getJournalpostStatusBefore());
	}

	private static boolean forrigeStatusVarOpplastingDokument(JournalpostEndretEvent event) {
		return OPPLASTINGDOKUMENT.equalsIgnoreCase(event.getJournalpostStatusBefore());
	}

	private static String buildHendelseId(JournalpostEndretEvent event, GoldenGateEvent goldenGateEvent) {
		// Unngå at operationTimestamp-delen blir trunkert for 00-sekund (eksempelvis blir 2021-09-22 12:47:00.000000 trunkert til 2021-09-22T12:47)
		return event.getJournalpostId() + "-" + goldenGateEvent.getOperationTimestamp().format(formatterWhereSecondsArePreservedIfZero);
	}
}
