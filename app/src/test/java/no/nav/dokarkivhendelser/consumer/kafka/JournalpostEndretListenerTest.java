package no.nav.dokarkivhendelser.consumer.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private ConsumerRecord<?, byte[]> consumerRecordMock;

    @Mock
    private ConsumerRecordToJournalpostEndretConverter converterMock;

    @InjectMocks
    private JournalpostEndretListener listener;

    @Before
    public void before() throws Exception {
        when(converterMock.convert(any(ConsumerRecord.class))).thenReturn(
                JournalpostEndretEvent.builder().journalpostId(123L).innhold(new byte[]{1}).build());
    }

    @Test
    public void onMessage() throws Exception {
        listener.onMessage(consumerRecordMock);
        verify(converterMock).convert(consumerRecordMock);
    }

}