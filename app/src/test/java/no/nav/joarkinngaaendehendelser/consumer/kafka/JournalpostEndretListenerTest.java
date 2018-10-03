package no.nav.joarkinngaaendehendelser.consumer.kafka;

import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.INNGAAENDE;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.JOURNALPOST;
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

import lombok.extern.slf4j.Slf4j;
import no.nav.joarkinngaaendehendelser.producer.InngaaendeHendelsePublisher;

@RunWith(MockitoJUnitRunner.class)
public class JournalpostEndretListenerTest {

    @Mock
    private ConsumerRecord<?, byte[]> consumerRecordMock;

    @Mock
    private ConsumerRecordAsJsonConverter converterMock;

    @Mock
    private InngaaendeHendelsePublisher publisher;

    @Mock
    Slf4j log;

    @InjectMocks
    private JournalpostEndretListener listener;


    @Before
    public void before() throws Exception {
        HashSet<String> columnsChanged = new HashSet<>();
        columnsChanged.add(JOURNALPOST);

        when(converterMock.convert(any(ConsumerRecord.class))).thenReturn(
                JournalpostEndretEvent.builder()
                        .journalpostId("123")
                        .fagomradeBefore("FOR")
                        .journalpostStatusAfter("M")
                        .journalposttype(INNGAAENDE)
                        .operation("U")
                        .columnsChanged(columnsChanged)
                        .build());
        when(consumerRecordMock.topic()).thenReturn("privat-dok-journalpostEndret-v1-t6");
    }

    @Test
    public void onUpdateMessage() throws Exception {
        listener.onMessage(consumerRecordMock);
        verify(converterMock).convert(consumerRecordMock);
    }

    @Test
    public void onCreatedMessage() throws Exception {
        listener.onMessage(consumerRecordMock);
        verify(converterMock).convert(consumerRecordMock);
        verify(publisher).publish(any(JournalpostEndretEvent.class));
    }

}