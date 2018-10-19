package no.nav.joarkinngaaendehendelser.consumer.kafka;

import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.JOURNALPOST_ID;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.KANAL_REFERANSE_ID;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.K_FAGOMRADE;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.K_JOURNALPOST_T;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.K_JOURNAL_S;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.K_MOTTAKS_KANAL;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.INNGAAENDE;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.OracleSchema.INSERT_OPERATION;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.OracleSchema.OPERATION_TIMESTAMP;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.OracleSchema.OPERATION_TYPE;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.OracleSchema.UPDATE_OPERATION;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author Martin Burheim Tingstad, Visma Consulting.
 */
@Component
@Slf4j
public class ConsumerRecordAsJsonConverter {

	private String template = "yyyy-MM-dd HH:mm:ss.SSSSSS";
	final SimpleDateFormat dateFormat = new SimpleDateFormat(template);

	public JournalpostEndretEvent convertRecordToEvent(ConsumerRecord<?, ?> record) {
		LinkedHashMap values = (LinkedHashMap) record.value();
		LinkedHashMap after = (LinkedHashMap) values.get("after");

		String operation = get((LinkedHashMap<String, String>) values, OPERATION_TYPE);
		String timestamp = get((LinkedHashMap<String, String>) values, OPERATION_TIMESTAMP);

		Long timeStamp = convertOracleTimeStampToLong(timestamp);

		Integer journalpostId = (Integer) after.get(JOURNALPOST_ID);

		log.info("Received {}-event for journalpost {} on topic: {}", operation, journalpostId, record.topic());

		JournalpostEndretEvent event;

		// Only for UPDATE-operations
		if (UPDATE_OPERATION.equalsIgnoreCase(operation)) {
			LinkedHashMap before = (LinkedHashMap) values.get("before");

			// Not relevant for us
			if (!INNGAAENDE.equalsIgnoreCase(getVerdi(before, K_JOURNALPOST_T))) {
				event = null;
			} else {
				Set<String> columnsChanged = getChangedColumns(before, after);
				columnsChanged.retainAll(before.keySet());

				event = JournalpostEndretEvent.builder()
						.journalpostId(journalpostId.longValue())
						.operation(operation)
						.fagomradeBefore(getVerdi(before, K_FAGOMRADE))
						.fagomradeAfter(getUpdatedVerdi(columnsChanged, after, before, K_FAGOMRADE))
						.journalpostStatusBefore(getVerdi(before, K_JOURNAL_S))
						.journalpostStatusAfter(getUpdatedVerdi(columnsChanged, after, before, K_JOURNAL_S))
						.journalpostType(getUpdatedVerdi(columnsChanged, after, before, K_JOURNALPOST_T))
						.mottaksKanal(getUpdatedVerdi(columnsChanged, after, before, K_MOTTAKS_KANAL))
						.kanalReferanseId(getUpdatedVerdi(columnsChanged, after, before, KANAL_REFERANSE_ID))
						.columnsChanged(columnsChanged)
						.timestamp(timeStamp)
						.build();
			}
		} else if (INSERT_OPERATION.equalsIgnoreCase(operation)) {
			Set<String> columnsChanged = new HashSet<>(after.keySet());
			event = JournalpostEndretEvent.builder()
					.journalpostId(journalpostId.longValue())
					.operation(operation)
					.fagomradeBefore("")
					.fagomradeAfter(getVerdi(after, K_FAGOMRADE))
					.journalpostStatusBefore("")
					.journalpostStatusAfter(getVerdi(after, K_JOURNAL_S))
					.journalpostType(getVerdi(after, K_JOURNALPOST_T))
					.mottaksKanal(getVerdi(after, K_MOTTAKS_KANAL))
					.kanalReferanseId(getVerdi(after, KANAL_REFERANSE_ID))
					.columnsChanged(columnsChanged)
					.timestamp(timeStamp)
					.build();
		} else {
			log.warn("Received unknown operation for journalpost " + journalpostId);
			event = JournalpostEndretEvent.builder()
					.journalpostId(journalpostId.longValue())
					.build();
		}

		return event;

	}

	private Long convertOracleTimeStampToLong(String timestamp) {
		Long timeStamp;
		if (StringUtils.isNotEmpty(timestamp)) {
			try {
				Date date = dateFormat.parse(timestamp);
				timeStamp = date.getTime();
			} catch (ParseException e) {
				timeStamp = new Date().getTime();
			}
		} else {
			timeStamp = new Date().getTime();
		}
		return timeStamp;
	}

	private String getUpdatedVerdi(Set<String> columnsChanged, LinkedHashMap after, LinkedHashMap before, String key) {
		if (columnsChanged.contains(key)) {
			return getVerdi(after, key);
		} else {
			return getVerdi(before, key);
		}
	}

	private String getVerdi(LinkedHashMap map, String key) {
		String value = get((LinkedHashMap<String, String>) map, key);
		return StringUtils.isNotEmpty(value) ? value : "";
	}

	private <T> T get(LinkedHashMap<?, ? extends T> map, String key) {
		if (map != null && map.containsKey(key) && map.get(key) != null) {
			return map.get(key);
		}
		return null;
	}

	private Set<String> getChangedColumns(LinkedHashMap before, LinkedHashMap after) {
		if (before.size() != after.size()) {
			return after.keySet();
		}
		Set<String> columnsChanged = new HashSet<>(before.keySet());

		for (Object key : before.keySet()) {
			if (after.get(key).equals(before.get(key))) {
				columnsChanged.remove(key);
			}
		}
		return columnsChanged;
	}
}
