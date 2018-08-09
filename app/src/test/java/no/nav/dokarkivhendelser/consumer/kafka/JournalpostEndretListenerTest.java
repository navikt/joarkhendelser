package no.nav.dokarkivhendelser.consumer.kafka;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
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

    @Mock
    private JournalpostEndretEvent eventMock;


    @Before
    public void before() throws Exception {
        when(consumerRecordMock.topic()).thenReturn("topic");

        Headers headers = mock(Headers.class);
        when(consumerRecordMock.headers()).thenReturn(headers);

        Header tracking = mock(Header.class);
        when(tracking.value()).thenReturn("tracking".getBytes(UTF_8));
        when(headers.lastHeader("trackingId")).thenReturn(tracking);

        when(converterMock.convert(consumerRecordMock)).thenReturn(eventMock);
    }

    public void tearDown() throws Exception {
    }

    @Test
    public void onMessage() throws Exception {
        listener.onMessage(consumerRecordMock);
        verify(converterMock).convert(consumerRecordMock);
    }

}