package no.nav.joarkhendelser.consumer;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEvent;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateColumns;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateUtils.prettyPrintOperationName;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.INSERT_OPERATION;
import static no.nav.joarkhendelser.consumer.goldengate.GoldenGateOperations.UPDATE_OPERATION;

@Component
@Slf4j
public class JournalpostEndretEventConverter {

	private final String template = "yyyy-MM-dd HH:mm:ss.SSSSSS";
	final SimpleDateFormat dateFormat = new SimpleDateFormat(template);

	public JournalpostEndretEvent convertToEvent(GoldenGateEvent goldenGateEvent, String topic, int partition, int offset) {

		String operation = goldenGateEvent.getOperation();
		if (!(UPDATE_OPERATION.equalsIgnoreCase(operation) || INSERT_OPERATION.equalsIgnoreCase(operation))) {
			log.warn("Received unknown operation {} for journalpost", prettyPrintOperationName(operation));
			return null;
		}

		String operationTimestamp = goldenGateEvent.getOperationTimestamp();
		String currentTimestamp = goldenGateEvent.getCurrentTimestamp();
		GoldenGateColumns after = goldenGateEvent.getAfter();
		GoldenGateColumns before = goldenGateEvent.getBefore();
		Integer journalpostId = after.getJournalpostId();

		log.info("Received {}-event for journalpost {} on topic: {} (Partition: {}, offset: {}) (op_ts: {}, current_ts: {})",
				prettyPrintOperationName(operation), journalpostId, topic, partition, offset, operationTimestamp, currentTimestamp);

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
					.operationTimestamp(convertOracleTimeStampToLong(operationTimestamp))
					.currentTimestamp(convertOracleTimeStampToLong(currentTimestamp))
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
					.operationTimestamp(convertOracleTimeStampToLong(operationTimestamp))
					.currentTimestamp(convertOracleTimeStampToLong(currentTimestamp))
					.build();
		}
		return null;
	}

	private Long convertOracleTimeStampToLong(String timestamp) {
		if (StringUtils.isNotEmpty(timestamp)) { // TODO Possibly a bug. Should check this with get current time on DB
			try {
				Date date = dateFormat.parse(timestamp);
				return date.getTime();
			} catch (ParseException e) {
				//fall gjennom til return
			}
		}
		return new Date().getTime();
	}

	private String getStringOrEmptyString(String value) {
		return StringUtils.isNotEmpty(value) ? value : "";
	}
}
