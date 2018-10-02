package no.nav.joarkinngaaendehendelser.consumer.kafka;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

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
public class ConsumerRecordToJournalpostEndretConverterTest {

    LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
    LinkedHashMap<Object, Object> after = new LinkedHashMap<>();
    LinkedHashMap<Object, Object> before = new LinkedHashMap<>();

    private Long JOURNALPOST_ID = 123L;

    @Mock
    private ConsumerRecord<?, Map> consumerRecordMock;

    @InjectMocks
    private ConsumerRecordToJournalpostEndretConverter converter;

    @Before
    public void before() throws Exception {

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

    private LinkedHashMap<String, Object> createbeforeValues() {
        LinkedHashMap<String, Object> valuesAfter = new LinkedHashMap<>();

        valuesAfter.put("JOURNALPOST_ID", Math.toIntExact(JOURNALPOST_ID));
        valuesAfter.put("K_JOURNALPOST_T", "I");
        valuesAfter.put("K_FAGOMRADE", "FOR");
        valuesAfter.put("K_JOURNAL_S", "M");

        return valuesAfter;
    }

    @Test
    public void convertUpdateOperation() throws Exception {
        values.clear();
        values.put("op_type", "U");

        after.put("JOURNALPOST_ID", Math.toIntExact(JOURNALPOST_ID));
        values.put("before", createbeforeValues());
        values.put("after", createAfterValues());

        when(consumerRecordMock.value()).thenReturn(values);
        JournalpostEndretEvent event = converter.convert(consumerRecordMock);
        assertEquals(123L, (long) event.getJournalpostId());
        assertEquals(4, event.columnsChanged.size());
        assertEquals("U", event.getOperation());
        assertEquals("M", event.getJournalpostStatus());
        assertEquals("FOR", event.getFagomradeBefore());
        assertEquals("DAG", event.getFagomradeAfter());
    }

    @Test
    public void convertCreateOperation() throws Exception {
        values.clear();
        values.put("op_type", "C");

        values.put("after", createAfterValues());

        when(consumerRecordMock.value()).thenReturn(values);
        JournalpostEndretEvent event = converter.convert(consumerRecordMock);
        assertEquals(123L, (long) event.getJournalpostId());
        assertEquals(4, event.columnsChanged.size());
        assertEquals("C", event.getOperation());
        assertEquals("M", event.getJournalpostStatus());
        assertEquals("DAG", event.getFagomradeAfter());
        assertEquals("", event.getFagomradeBefore());
    }
}