package no.nav.dokarkivhendelser.consumer.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JournalpostEndretListenerTest {

    @Mock
    private ConsumerRecord<?, Map> consumerRecordMock;

    @Mock
    private ConsumerRecordToJournalpostEndretConverter converterMock;

    @InjectMocks
    private JournalpostEndretListener listener;

    @Before
    public void before() throws Exception {
        HashSet<String> columnsChanged = new HashSet<>();
        columnsChanged.add("T_JOURNALPOST");

        when(converterMock.convert(any(ConsumerRecord.class))).thenReturn(
                JournalpostEndretEvent.builder()
                        .journalpostId(123L)
                        .journalpostType("N")
                        .fagomrade("FOR")
                        .journalpostStatus("M")
                        .operation("U")
                        .columnsChanged(columnsChanged)
                        .build());
    }

    @Test
    public void onMessage() throws Exception {
        listener.onMessage(consumerRecordMock);
        verify(converterMock).convert(consumerRecordMock);
    }

}