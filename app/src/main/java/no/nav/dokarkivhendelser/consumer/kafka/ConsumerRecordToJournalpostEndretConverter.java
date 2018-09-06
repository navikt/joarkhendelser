package no.nav.dokarkivhendelser.consumer.kafka;

import static no.nav.dokarkivhendelser.consumer.kafka.JoarkSchema.JOURNALPOST_ID;
import static no.nav.dokarkivhendelser.consumer.kafka.JoarkSchema.K_FAGOMRADE;
import static no.nav.dokarkivhendelser.consumer.kafka.JoarkSchema.K_JOURNALPOST_T;
import static no.nav.dokarkivhendelser.consumer.kafka.JoarkSchema.K_JOURNAL_S;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Component
public class ConsumerRecordToJournalpostEndretConverter {
    private LinkedHashMap values;
    private LinkedHashMap before;
    private LinkedHashMap after;
    private Set<String> columns_changed;

    private String OPERATION_TYPE = "op_type";
    private String CREATE_OPERATION = "C";
    private String UPDATE_OPERATION = "U";

    public JournalpostEndretEvent convert(ConsumerRecord<?, ?> source) {
        values = (LinkedHashMap) source.value();
        String operation = get((LinkedHashMap<String,String>) values, OPERATION_TYPE);

        after = (LinkedHashMap) values.get("after");
        columns_changed = new HashSet<String>(after.keySet());

        // Only for UPDATE-operations
        if(UPDATE_OPERATION.equalsIgnoreCase(operation)) {
            before = (LinkedHashMap) values.get("before");
            columns_changed.retainAll(before.keySet());
        }

        Long journalpostId = getJournalpostId();

        return JournalpostEndretEvent.builder()
                .journalpostId(journalpostId)
                .operation(operation)
                .fagomrade(hentKolonne(K_FAGOMRADE))
                .journalpostType(hentKolonne(K_JOURNALPOST_T))
                .journalpostStatus(hentKolonne(K_JOURNAL_S))
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

    private String hentKolonne(String key) {
        if(columns_changed != null && columns_changed.contains(key)) {
            return get((LinkedHashMap<String,String>) after, key);
        }
        return get((LinkedHashMap<String,String>) before, key);
    }

    private <T> T get(LinkedHashMap<?, ? extends T> map, String key){
        if(map != null && map.containsKey(key) && map.get(key) != null) {
            return map.get(key);
        }
        return null;
    }

}
