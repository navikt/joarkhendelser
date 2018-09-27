package no.nav.joarkinngaaendehendelser.consumer.kafka;

import static no.nav.joarkinngaaendehendelser.consumer.kafka.JoarkSchema.JOURNALPOST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;
import no.nav.joarkinngaaendehendelser.producer.inngaaendejournalpost.EndeligJournalfortPublisher;
import no.nav.joarkinngaaendehendelser.producer.inngaaendejournalpost.NyPublisher;
import no.nav.joarkinngaaendehendelser.producer.inngaaendejournalpost.TemaEndretPublisher;
import no.nav.joarkinngaaendehendelser.producer.inngaaendejournalpost.UtgarPublisher;

@RunWith(MockitoJUnitRunner.class)
public class JournalpostEndretListenerTest {

    @Mock
    private ConsumerRecord<?, Map> consumerRecordMock;

    @Mock
    private ConsumerRecordToJournalpostEndretConverter converterMock;

    @Mock
    Slf4j log;

    @InjectMocks
    private JournalpostEndretListener listener;

    @Mock
    private EndeligJournalfortPublisher endeligJournalfortPublisher;

    @Mock
    private TemaEndretPublisher temaEndretPublisher;

    @Mock
    private UtgarPublisher utgarPublisher;

    @Mock
    private NyPublisher nyPublisher;

    @Before
    public void before() throws Exception {
        HashSet<String> columnsChanged = new HashSet<>();
        columnsChanged.add(JOURNALPOST);

        when(converterMock.convert(any(ConsumerRecord.class))).thenReturn(
                JournalpostEndretEvent.builder()
                        .journalpostId(123L)
                        .fagomradeBefore("FOR")
                        .journalpostStatus("M")
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
    @Ignore
    public void onCreatedMessage() throws Exception {
        listener.onMessage(consumerRecordMock);
        verify(converterMock).convert(consumerRecordMock);
        verify(nyPublisher).publish(any(JournalpostEndretEvent.class));
    }

}