package no.nav.joarkinngaaendehendelser.consumer.kafka;

import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.INNGAAENDE;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.JOURNALPOST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import no.nav.joarkinngaaendehendelser.producer.InngaaendeHendelse;
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
    Logger log;

    @InjectMocks
    private JournalpostEndretListener listener;

    @Before
    public void before() throws Exception {
        HashSet<String> columnsChanged = new HashSet<>();
        columnsChanged.add(JOURNALPOST);

        when(converterMock.convertRecordToEvent(any(ConsumerRecord.class))).thenReturn(
                JournalpostEndretEvent.builder()
                        .journalpostId(123L)
                        .fagomradeBefore("FOR")
                        .fagomradeAfter("BAR")
                        .journalpostStatusAfter("M")
                        .journalpostType(INNGAAENDE)
                        .operation("U")
                        .columnsChanged(columnsChanged)
                        .build());
    }

    @Test
    public void onUpdateMessage() throws Exception {
        listener.onMessage(consumerRecordMock);
        verify(converterMock).convertRecordToEvent(consumerRecordMock);
    }

    @Ignore
    @Test
    public void onCreatedMessage() throws Exception {
        listener.onMessage(consumerRecordMock);
        verify(converterMock).convertRecordToEvent(consumerRecordMock);
        verify(publisher).publish(any(InngaaendeHendelse.class));
        verify(log, times(1)).info(any());
    }

}