package no.nav.joarkinngaaendehendelser.consumer.kafka;

import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.INNGAAENDE;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.JOURNALPOST_ID;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.K_FAGOMRADE;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.K_JOURNALPOST_T;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.K_JOURNAL_S;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.OracleSchema.OPERATION_TYPE;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.OracleSchema.UPDATE_OPERATION;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Component
public class ConsumerRecordToJournalpostEndretConverter {
    private LinkedHashMap values;
    private LinkedHashMap before;
    private LinkedHashMap after;
    private Set<String> columns_changed;
    private String operation;

    private boolean prepareAndCheckInngaaendeEvent(ConsumerRecord<?, ?> record) {
        values = (LinkedHashMap) record.value();
        operation = get((LinkedHashMap<String,String>) values, OPERATION_TYPE);

        after = (LinkedHashMap) values.get("after");
        if(!INNGAAENDE.equalsIgnoreCase(get((LinkedHashMap<String,String>) after, K_JOURNALPOST_T))) {
            return false;
        }

        columns_changed = new HashSet<String>(after.keySet());

        // Only for UPDATE-operations
        if(UPDATE_OPERATION.equalsIgnoreCase(operation)) {
            before = (LinkedHashMap) values.get("before");
            columns_changed.retainAll(before.keySet());
        }
        return true;
    }

    public JournalpostEndretEvent convert(ConsumerRecord<?, ?> record) {
        if(!prepareAndCheckInngaaendeEvent(record)) {
            return null;
        }

        Long journalpostId = getJournalpostId();

        return JournalpostEndretEvent.builder()
                .journalpostId(journalpostId)
                .operation(operation)
                .fagomradeBefore(hentKolonneVerdier(K_FAGOMRADE).get(0))
                .fagomradeAfter(columns_changed.contains(K_FAGOMRADE) ?
                        hentKolonneVerdier(K_FAGOMRADE).get(1) :
                        hentKolonneVerdier(K_FAGOMRADE).get(0))
                .journalpostStatus(hentKolonneVerdier(K_JOURNAL_S).get(0))
                .columnsChanged(columns_changed)
                .build();
    }

    private Long getJournalpostId() {
        if(after.containsKey(JOURNALPOST_ID)) {
            Object o = after.get(JOURNALPOST_ID);
            if(o instanceof Integer) {
                return Long.valueOf((Integer)o);
            }
        }
        else if(before.containsKey(JOURNALPOST_ID)) {
            Object o = before.get(JOURNALPOST_ID);
            if(o instanceof Integer) {
                return Long.valueOf((Integer)o);
            }
        }
        return null;
    }

    private List<String> hentKolonneVerdier(String key) {
        if(columns_changed != null && columns_changed.contains(key)) {
            List verdier = new ArrayList<String>();
            String value = get((LinkedHashMap<String,String>) before, key);
            if(StringUtils.isNotEmpty(value)) {
                verdier.add(value);
            }
            value = get((LinkedHashMap<String,String>) after, key);
            if(StringUtils.isNotEmpty(value)) {
                verdier.add(value);
            }
            return verdier;
        }
        List verdier = new ArrayList<String>();
        verdier.add(get((LinkedHashMap<String,String>) before, key));
        return verdier;
    }

    private <T> T get(LinkedHashMap<?, ? extends T> map, String key){
        if(map != null && map.containsKey(key) && map.get(key) != null) {
            return map.get(key);
        }
        return null;
    }

}
