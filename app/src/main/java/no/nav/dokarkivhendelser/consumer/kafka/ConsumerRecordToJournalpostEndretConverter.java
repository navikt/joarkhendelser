package no.nav.dokarkivhendelser.consumer.kafka;

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

    public JournalpostEndretEvent convert(ConsumerRecord<?, ?> source) {
        values = (LinkedHashMap) source.value();
        before = (LinkedHashMap) values.get("before");
        after = (LinkedHashMap) values.get("after");
        columns_changed = new HashSet<String>(after.keySet());
        columns_changed.retainAll(before.keySet());

        Long journalpostId = getJournalpostId();
        String operation = get((LinkedHashMap<String,String>) values, "op_type");

        return JournalpostEndretEvent.builder()
                .journalpostId(journalpostId)
                .operation(operation)
                .fagomrade(hentKolonne("K_FAGOMRADE"))
                .journalpostType(hentKolonne("K_JOURNALPOST_T"))
                .columnsChanged(columns_changed)
                .build();
    }

    private Long getJournalpostId() {
        String JOURNALPOST_ID = "JOURNALPOST_ID";
        if(before.containsKey(JOURNALPOST_ID)) {
            Object o = before.get(JOURNALPOST_ID);
            if(o instanceof Integer) {
                return Long.valueOf((Integer)o);
            }
        }
        return null;
    }

    private String hentKolonne(String key) {
        if(columns_changed.contains(key)) {
            return get((LinkedHashMap<String,String>) after, key);
        }
        return get((LinkedHashMap<String,String>) before, key);

    }

    private <T> T get(LinkedHashMap<?, ? extends T> map, String key){
        if(map.containsKey(key) && map.get(key) != null) {
            return map.get(key);
        }
        return null;
    }

}
