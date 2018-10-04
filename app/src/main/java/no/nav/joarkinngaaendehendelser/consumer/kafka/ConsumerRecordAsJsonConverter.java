package no.nav.joarkinngaaendehendelser.consumer.kafka;

import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.INNGAAENDE;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.JOURNALPOST_ID;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.KANAL_REFERANSE_ID;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.K_FAGOMRADE;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.K_JOURNALPOST_T;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.K_JOURNAL_S;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.K_MOTTAKS_KANAL;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.OracleSchema.INSERT_OPERATION;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.OracleSchema.OPERATION_TIMESTAMP;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.OracleSchema.OPERATION_TYPE;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.OracleSchema.UPDATE_OPERATION;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Martin Burheim Tingstad, Visma Consulting.
 */
@Component
@Slf4j
public class ConsumerRecordAsJsonConverter {

    private String template = "yyyy-MM-dd HH:mm:ss.SSSSSS";
    final SimpleDateFormat dateFormat = new SimpleDateFormat(template);

    public JournalpostEndretEvent convert(ConsumerRecord<?, ?> record) {
        LinkedHashMap values = (LinkedHashMap) record.value();
        LinkedHashMap after = (LinkedHashMap) values.get("after");

        String operation = get((LinkedHashMap<String,String>) values, OPERATION_TYPE);
        String timestamp = get((LinkedHashMap<String,String>) values, OPERATION_TIMESTAMP);

        Long timeStamp = convertOracleTimeStampToLong(timestamp);

        Set<String> columns_changed = new HashSet<String>(after.keySet());
        Integer journalpostId = (Integer)(after.get(JOURNALPOST_ID));

        // Only for UPDATE-operations
        if(UPDATE_OPERATION.equalsIgnoreCase(operation)) {
            LinkedHashMap before = (LinkedHashMap) values.get("before");

            // Not relevant for us
            if(!INNGAAENDE.equalsIgnoreCase(hentVerdi(before, K_JOURNALPOST_T))) {
                return null;
            }

            columns_changed.retainAll(before.keySet());

            return JournalpostEndretEvent.builder()
                    .journalpostId(journalpostId.longValue())
                    .operation(operation)
                    .fagomradeBefore(hentVerdi(before, K_FAGOMRADE))
                    .fagomradeAfter(hentUpdatedVerdi(columns_changed, after, before, K_FAGOMRADE))
                    .journalpostStatusBefore(hentVerdi(before, K_JOURNAL_S))
                    .journalpostStatusAfter(hentUpdatedVerdi(columns_changed, after, before, K_JOURNAL_S))
                    .journalpostType(hentUpdatedVerdi(columns_changed, after, before, K_JOURNALPOST_T))
                    .mottaksKanal(hentUpdatedVerdi(columns_changed, after, before,K_MOTTAKS_KANAL))
                    .kanalReferanseId(hentUpdatedVerdi(columns_changed, after, before, KANAL_REFERANSE_ID))
                    .columnsChanged(columns_changed)
                    .timestamp(timeStamp)
                    .build();
        }

        if(INSERT_OPERATION.equalsIgnoreCase(operation)) {
            return JournalpostEndretEvent.builder()
                    .journalpostId(journalpostId.longValue())
                    .operation(operation)
                    .fagomradeBefore("")
                    .fagomradeAfter(hentVerdi(after, K_FAGOMRADE))
                    .journalpostStatusBefore("")
                    .journalpostStatusAfter(hentVerdi(after,K_JOURNAL_S))
                    .journalpostType(hentVerdi(after,K_JOURNALPOST_T))
                    .mottaksKanal(hentVerdi(after,K_MOTTAKS_KANAL))
                    .kanalReferanseId(hentVerdi(after,KANAL_REFERANSE_ID))
                    .columnsChanged(columns_changed)
                    .timestamp(timeStamp)
                    .build();
        }
        log.warn("Received unknown operation for journalpost "+journalpostId);
        return JournalpostEndretEvent.builder()
                .journalpostId(journalpostId.longValue())
                .build();
    }

    private Long convertOracleTimeStampToLong(String timestamp) {
        Long timeStamp;
        if(StringUtils.isNotEmpty(timestamp)) {
            try {
                Date date = dateFormat.parse(timestamp);
                timeStamp = date.getTime();
            } catch (ParseException e) {
                timeStamp = new Date().getTime();
            }
        }
        else {
            timeStamp = new Date().getTime();
        }
        return timeStamp;
    }

    private String hentUpdatedVerdi(Set<String> columnsChanged, LinkedHashMap after, LinkedHashMap before, String key) {
        if(columnsChanged.contains(key)) {
            return hentVerdi(after, key);
        }
        else {
            return hentVerdi(before, key);
        }
    }

    private String hentVerdi(LinkedHashMap map, String key) {
        String value = get((LinkedHashMap<String,String>) map, key);
        return StringUtils.isNotEmpty(value) ? value : "";
    }

    private <T> T get(LinkedHashMap<?, ? extends T> map, String key){
        if(map != null && map.containsKey(key) && map.get(key) != null) {
            return map.get(key);
        }
        return null;
    }

}
