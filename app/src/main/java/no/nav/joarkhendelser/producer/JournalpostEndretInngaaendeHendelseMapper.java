package no.nav.joarkhendelser.producer;

import lombok.extern.slf4j.Slf4j;
import no.nav.joarkhendelser.consumer.JournalpostEndretEvent;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEvent;

import java.time.format.DateTimeFormatter;

import static no.nav.joarkhendelser.consumer.JournalpostType.INNGAAENDE;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.INSERT_OPERATION;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.UPDATE_OPERATION;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.ENDELIG_JOURNALFORT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.JOURNALPOST_MOTTATT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.JOURNALPOST_UTGATT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.TEMA_ENDRET;
import static no.nav.joarkhendelser.producer.JoarkJournalpostStatus.JOURNALFORT;
import static no.nav.joarkhendelser.producer.JoarkJournalpostStatus.MIDLERTIDIG;
import static no.nav.joarkhendelser.producer.JoarkJournalpostStatus.MOTTATT;
import static no.nav.joarkhendelser.producer.JoarkJournalpostStatus.OPPLASTINGDOKUMENT;
import static no.nav.joarkhendelser.producer.JoarkJournalpostStatus.UKJENTBRUKER;
import static no.nav.joarkhendelser.producer.JoarkJournalpostStatus.UTGAR;
import static org.apache.logging.log4j.util.Strings.isNotEmpty;

@Slf4j
public class JournalpostEndretInngaaendeHendelseMapper {

	private static final DateTimeFormatter formatterWhereSecondsArePreservedIfZero = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	public static InngaaendeHendelse map(JournalpostEndretEvent event, GoldenGateEvent goldenGateEvent) {
		InngaaendeHendelsesType hendelsestype = mapHendelsestype(event);

		if (hendelsestype == null) {
			log.info("Hendelse med operation={}, journalposttype={}, journalpoststatusBefore={}, journalpoststatusAfter={} er ikke en av dei fire relevante hendelsene.",
					event.getOperation(), event.getJournalpostType(), event.getJournalpostStatusBefore(), event.getJournalpostStatusAfter());

			return null;
		}

		return InngaaendeHendelse.builder()
				.hendelsesId(buildHendelseId(event, goldenGateEvent)) // journalpostId + operationTimestamp på ISO8601-format (yyyy-MM-ddTHH:mm:ss)
				.versjon(1)
				.temaNytt(event.getFagomradeAfter())
				.temaGammelt(event.getFagomradeBefore())
				.journalpostId(event.getJournalpostId())
				.kanalReferanseId(event.getKanalReferanseId())
				.mottaksKanal(event.getMottaksKanal())
				.behandlingsTema(event.getBehandlingsTema())
				.journalpostStatus(mapJournalstatus(event.getJournalpostStatusAfter()))
				.hendelsesType(hendelsestype.toString())
				.build();
	}

	private static String mapJournalstatus(String journalpostStatus) {

		return switch (journalpostStatus) {
			case JOURNALFORT -> JournalpostStatus.JOURNALFORT;
			case MIDLERTIDIG, MOTTATT -> JournalpostStatus.MOTTATT;
			case OPPLASTINGDOKUMENT -> JournalpostStatus.OPPLASTINGDOKUMENT;
			case UKJENTBRUKER -> JournalpostStatus.UKJENTBRUKER;
			case UTGAR -> JournalpostStatus.UTGAR;
			default -> null;
		};
	}

	private static InngaaendeHendelsesType mapHendelsestype(JournalpostEndretEvent event) {
		InngaaendeHendelsesType hendelsestype = null;

		if (!typeErInngaaende(event)) {
			return hendelsestype;
		}

		if (erJournalpostMottatt(event)) {
			hendelsestype = JOURNALPOST_MOTTATT;
		} else if (erEndeligJournalfort(event)) {
			hendelsestype = ENDELIG_JOURNALFORT;
		} else if (erJournalpostUtgatt(event)) {
			hendelsestype = JOURNALPOST_UTGATT;
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

	private static boolean erEndeligJournalfort(JournalpostEndretEvent event) {
		return (operasjonErInsert(event) || (operasjonErUpdate(event) && forrigeStatusVarMidlertidig(event))) && nyStatusErJournalfort(event);
	}

	private static boolean erJournalpostUtgatt(JournalpostEndretEvent event) {
		return operasjonErUpdate(event) && harEndretStatusTilUtgarEllerUkjentbruker(event);
	}

	private static boolean harEndretFagomraade(JournalpostEndretEvent event) {
		return isNotEmpty(event.getFagomradeBefore()) &&
			   isNotEmpty(event.getFagomradeAfter()) &&
			   !event.getFagomradeBefore().equalsIgnoreCase(event.getFagomradeAfter());
	}

	private static boolean harEndretStatusTilUtgarEllerUkjentbruker(JournalpostEndretEvent event) {
		return isNotEmpty(event.getJournalpostStatusBefore()) &&
			   (UTGAR.equalsIgnoreCase(event.getJournalpostStatusAfter()) || UKJENTBRUKER.equalsIgnoreCase(event.getJournalpostStatusAfter()));
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

	private static boolean nyStatusErJournalfort(JournalpostEndretEvent event) {
		return JOURNALFORT.equalsIgnoreCase(event.getJournalpostStatusAfter());
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
