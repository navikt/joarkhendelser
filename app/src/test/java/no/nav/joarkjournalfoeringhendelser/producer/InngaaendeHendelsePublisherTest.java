package no.nav.joarkjournalfoeringhendelser.producer;

import static no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelsesType.MIDLERTIDIG_JOURNALFORT;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.joarkjournalfoeringhendelser.consumer.kafka.JournalpostEndretEvent;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.concurrent.ListenableFuture;

@RunWith(MockitoJUnitRunner.class)
public class InngaaendeHendelsePublisherTest {

	@InjectMocks
	InngaaendeHendelsePublisher inngaaendeHendelsePublisher;

	@Mock
	private KafkaTemplate<String, JournalfoeringHendelseRecord> kafkaTemplate;

	private InngaaendeHendelse hendelse;

	@Before
	public void setUp() throws Exception {
		ReflectionTestUtils.setField(inngaaendeHendelsePublisher, "topic", "test-topic");

		ListenableFuture listenableFuture = mock(ListenableFuture.class);
		when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(listenableFuture);
		when(listenableFuture.get()).thenReturn("ok");
	}

	@Test
	public void shouldPublish() throws Exception {
		hendelse = createInngaaendeHendelse();
		inngaaendeHendelsePublisher.publish(hendelse);

		verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
		assertEquals(MIDLERTIDIG_JOURNALFORT.toString(), hendelse.getHendelsesType());
	}

	private InngaaendeHendelse createInngaaendeHendelse() {
		JournalpostEndretEvent journalpostEndretEvent = JournalpostEndretEvent.builder()
				.journalpostId(1L)
				.journalpostStatusAfter("MO")
				.journalpostStatusBefore("MO")
				.operation("I")
				.journalpostType("I")
				.kanalReferanseId("kanal-ref")
				.build();

		return JournalpostEndretInngaaendeHendelseMapper.map(journalpostEndretEvent);
	}
}