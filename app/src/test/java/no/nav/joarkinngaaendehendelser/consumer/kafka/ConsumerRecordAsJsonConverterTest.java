package no.nav.joarkinngaaendehendelser.consumer.kafka;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerRecordAsJsonConverterTest {

    LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
    LinkedHashMap<Object, Object> after = new LinkedHashMap<>();
    LinkedHashMap<Object, Object> before = new LinkedHashMap<>();

    private Long JOURNALPOST_ID = 123L;
    private String timestamp;

    @Mock
    private ConsumerRecord<?, Map> consumerRecordMock;

    @InjectMocks
    private ConsumerRecordAsJsonConverter converter;

    @Before
    public void before() throws Exception {
        timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(
                DateTimeFormatter.ISO_INSTANT);
    }

    @After
    public void tearDown() throws Exception {
    }

    private LinkedHashMap<String, Object> createAfterValues() {
        LinkedHashMap<String, Object> valuesAfter = new LinkedHashMap<>();

        valuesAfter.put("JOURNALPOST_ID", Math.toIntExact(JOURNALPOST_ID));
        valuesAfter.put("K_JOURNALPOST_T", "I");
        valuesAfter.put("K_FAGOMRADE", "DAG");
        valuesAfter.put("K_JOURNAL_S", "M");

        return valuesAfter;
    }

    private LinkedHashMap<String, Object> createBeforeValues() {
        LinkedHashMap<String, Object> valuesAfter = new LinkedHashMap<>();

        valuesAfter.put("JOURNALPOST_ID", Math.toIntExact(JOURNALPOST_ID));
        valuesAfter.put("K_JOURNALPOST_T", "I");
        valuesAfter.put("K_FAGOMRADE", "FOR");
        valuesAfter.put("K_JOURNAL_S", "M");

        return valuesAfter;
    }

    @Test
    public void shouldConvertUpdateOperation() throws Exception {
        values.clear();
        values.put("op_type", "U");
        values.put("op_ts", timestamp);

        after.put("JOURNALPOST_ID", Math.toIntExact(JOURNALPOST_ID));
        values.put("before", createBeforeValues());
        values.put("after", createAfterValues());

        when(consumerRecordMock.value()).thenReturn(values);
        JournalpostEndretEvent event = converter.convert(consumerRecordMock);
        assertEquals("123", event.getJournalpostId());
        assertEquals(4, event.columnsChanged.size());
        assertEquals("U", event.getOperation());
        assertEquals("M", event.getJournalpostStatusAfter());
        assertEquals("FOR", event.getFagomradeBefore());
        assertEquals("DAG", event.getFagomradeAfter());
    }

    @Test
    public void shouldConvertCreateOperation() throws Exception {
        values.clear();
        values.put("op_type", "I");
        values.put("op_ts", timestamp);

        values.put("after", createAfterValues());

        when(consumerRecordMock.value()).thenReturn(values);
        JournalpostEndretEvent event = converter.convert(consumerRecordMock);
        assertEquals("123", event.getJournalpostId());
        assertEquals(4, event.columnsChanged.size());
        assertEquals("I", event.getOperation());
        assertEquals("M", event.getJournalpostStatusAfter());
        assertEquals("DAG", event.getFagomradeAfter());
        assertEquals("", event.getFagomradeBefore());
    }

    @Test
    @Ignore
    public void shouldProduceCorrectNumberOfColumnsChanged() throws Exception {
        values.clear();
        values.put("op_type", "U");

        after.put("JOURNALPOST_ID", Math.toIntExact(JOURNALPOST_ID));

        // Endre disse til å ha nesten like data før og etter, bortsett fra 1 eller 2 kolonner for å verifisere at vi før rett verdi i assertEquals
        //values.put("before", createBeforeValues());
        //values.put("after", createAfterValues());
        when(consumerRecordMock.value()).thenReturn(values);
        JournalpostEndretEvent event = converter.convert(consumerRecordMock);
        assertEquals(3, event.columnsChanged.size());

    }
}