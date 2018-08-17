package no.nav.dokarkivhendelser.consumer.kafka;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerRecordToJournalpostEndretConverterTest {

    @Mock
    private ConsumerRecord<?, Map> consumerRecordMock;

    @InjectMocks
    private ConsumerRecordToJournalpostEndretConverter converter;

    @Before
    public void before() throws Exception {
        LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
        LinkedHashMap<Object, Object> after = new LinkedHashMap<>();
        LinkedHashMap<Object, Object> before = new LinkedHashMap<>();

        after.put("JOURNALPOST_ID", Integer.valueOf(123));
        values.put("after", after);

        before.put("JOURNALPOST_ID", Integer.valueOf(123));
        values.put("before", before);

        when(consumerRecordMock.value()).thenReturn(values);

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void convert() throws Exception {
        JournalpostEndretEvent event = converter.convert(consumerRecordMock);
        assertEquals(123L, (long) event.getJournalpostId());
        assertEquals(1, event.columnsChanged.size());
    }

}