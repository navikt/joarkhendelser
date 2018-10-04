package no.nav.joarkinngaaendehendelser.consumer.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.*;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.INNGAAENDE;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.OracleSchema.*;

/**
 * @author Martin Burheim Tingstad, Visma Consulting.
 */
@Component
@Slf4j
public class ConsumerRecordAsJsonConverter {

    public JournalpostEndretEvent convert(ConsumerRecord<?, ?> record) {
        LinkedHashMap values = (LinkedHashMap) record.value();
        LinkedHashMap after = (LinkedHashMap) values.get("after");

        // Not relevant for us
        if (!INNGAAENDE.equalsIgnoreCase(hentVerdi(after, K_JOURNALPOST_T))) {
            return null;
        }

        String operation = get((LinkedHashMap<String, String>) values, OPERATION_TYPE);
        String timestamp = get((LinkedHashMap<String, String>) values, OPERATION_TIMESTAMP);

        Integer journalpostId = (Integer) (after.get(JOURNALPOST_ID));

        // Only for UPDATE-operations
        if (UPDATE_OPERATION.equalsIgnoreCase(operation)) {
            LinkedHashMap before = (LinkedHashMap) values.get("before");
            Set<String> columns_changed = getChangedColumns(before, after);

            return JournalpostEndretEvent.builder()
                    .journalpostId(journalpostId.toString())
                    .operation(operation)
                    .fagomradeBefore(hentVerdi(before, K_FAGOMRADE))
                    .fagomradeAfter(hentVerdi(after, K_FAGOMRADE))
                    .journalpostStatusBefore(hentVerdi(before, K_JOURNAL_S))
                    .journalpostStatusAfter(hentVerdi(after, K_JOURNAL_S))
                    .journalposttype(hentVerdi(after, K_JOURNALPOST_T))
                    .mottaksKanal(hentVerdi(after, K_MOTTAKS_KANAL))
                    .kanalReferanseId(hentVerdi(after, KANAL_REFERANSE_ID))
                    .columnsChanged(columns_changed)
                    .timestamp(timestamp)
                    .build();
        }

        if (INSERT_OPERATION.equalsIgnoreCase(operation)) {
            Set<String> columns_changed = new HashSet<String>(after.keySet());
            return JournalpostEndretEvent.builder()
                    .journalpostId(journalpostId.toString())
                    .operation(operation)
                    .fagomradeBefore("")
                    .fagomradeAfter(hentVerdi(after, K_FAGOMRADE))
                    .journalpostStatusBefore("")
                    .journalpostStatusAfter(hentVerdi(after, K_JOURNAL_S))
                    .journalposttype(hentVerdi(after, K_JOURNALPOST_T))
                    .mottaksKanal(hentVerdi(after, K_MOTTAKS_KANAL))
                    .kanalReferanseId(hentVerdi(after, KANAL_REFERANSE_ID))
                    .columnsChanged(columns_changed)
                    .timestamp(timestamp)
                    .build();
        }
        log.warn("Received unknown operation for journalpost " + journalpostId);
        return JournalpostEndretEvent.builder()
                .journalpostId(journalpostId.toString())
                .build();
    }

    private String hentVerdi(LinkedHashMap map, String key) {
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
        Set<String> columns_changed = new HashSet<String>(after.keySet());

        for (Object key : before.keySet()) {
            if (after.get(key).equals(before.get(key))) {
                columns_changed.remove(key);
            }
        }
        return columns_changed;
    }

}
