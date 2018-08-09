package no.nav.dokarkivhendelser.consumer.kafka;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
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
    private ConsumerRecord<?, byte[]> consumerRecordMock;

    @InjectMocks
    private ConsumerRecordToJournalpostEndretConverter converter;

    @Before
    public void before() throws Exception {
        Header tracking = mock(Header.class);
        when(tracking.value()).thenReturn("123".getBytes(UTF_8));
        Headers headers = mock(Headers.class);
        when(headers.lastHeader("journalpostId")).thenReturn(tracking);

        when(consumerRecordMock.headers()).thenReturn(headers);
        when(consumerRecordMock.value()).thenReturn(new byte[]{0});

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void convert() throws Exception {
        JournalpostEndretEvent event = converter.convert(consumerRecordMock);
        assertEquals(123L, (long) event.getJournalpostId());
    }

}