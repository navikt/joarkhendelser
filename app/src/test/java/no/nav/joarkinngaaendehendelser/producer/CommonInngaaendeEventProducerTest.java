package no.nav.joarkinngaaendehendelser.producer;

import no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostEndretEvent;
//import no.nav.joarkinngaaendehendelser.producer.inngaaendejournalpost.EndeligJournalfortPublisher;
//import no.nav.joarkinngaaendehendelser.producer.inngaaendejournalpost.Hendelse;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
//
//@RunWith(MockitoJUnitRunner.class)
//public class CommonInngaaendeEventProducerTest {
//
//    private static final String TOPIC = "topic-placeholder";
//    public static final Long JOURNALPOST_ID = 123L;
//    public static final String FAGOMRÅDE_BEFORE = "FOR";
//    public static final String JOURNALPOST_STATUS = "M";
//
//    private JournalpostEndretEvent event;
//    private ListenableFuture mockedObject = mock(ListenableFuture.class);
//
//
//    @Mock
//    private KafkaTemplate<Object, Object> kafkaTemplate;
//
//    @InjectMocks
//    private CommonInngaaendeEventProducer endeligJournalfortPublisherInstance = new EndeligJournalfortPublisher();
//
//    @Rule
//    public ExpectedException expectedException = ExpectedException.none();
//
//    @Before
//    public void before() {
//        event = JournalpostEndretEvent.builder()
//                .journalpostId(JOURNALPOST_ID)
//                .fagomradeBefore(FAGOMRÅDE_BEFORE)
//                .journalpostStatus(JOURNALPOST_STATUS)
//                .build();
//    }
//
//    @Test
//    public void shouldPublish() {
//        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(mockedObject);
//
//        endeligJournalfortPublisherInstance.sendEventToTopic(event, TOPIC);
//
//        ArgumentCaptor<ProducerRecord<Object, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
//        verify(kafkaTemplate, times(1)).send(captor.capture());
//
//        ProducerRecord producerRecord = captor.getValue();
//        Hendelse hendelse = (Hendelse) producerRecord.value();
//
//        assertEquals(TOPIC, captor.getValue().topic());
//        assertEquals(JOURNALPOST_ID, producerRecord.key());
//        assertEquals(JOURNALPOST_STATUS, hendelse.getJournalpostStatus());
//        assertEquals(FAGOMRÅDE_BEFORE, hendelse.getFagomradeBefore());
//    }
//
//    @Test
//    public void shouldFail() throws ExecutionException, InterruptedException {
//        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(mockedObject);
//        when(mockedObject.get()).thenThrow(new InterruptedException());
//
//        endeligJournalfortPublisherInstance.sendEventToTopic(event, TOPIC);
//    }
//
//}