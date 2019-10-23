package no.nav.joarkjournalfoeringhendelser.producer;

import static no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelsesType.MIDLERTIDIG_JOURNALFORT;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public class InngaaendeHendelsePublisherTest {

	@InjectMocks
	InngaaendeHendelsePublisher inngaaendeHendelsePublisher;

	@Mock
	private MeterRegistry meterRegistry;
	@Mock
	private KafkaTemplate<String, JournalfoeringHendelseRecord> kafkaTemplate;
	@Mock
	private Timer timerMock;

	private InngaaendeHendelse hendelse;

	@Before
	public void setUp() throws Exception {
		ReflectionTestUtils.setField(inngaaendeHendelsePublisher, "topic", "test-topic");

		ListenableFuture listenableFuture = mock(ListenableFuture.class);
		when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(listenableFuture);
		when(listenableFuture.get()).thenReturn(new SendResult<String, JournalfoeringHendelseRecord>(null, null));
		when(meterRegistry.timer(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(timerMock);

	}

	@Test
	public void shouldPublish() {
		hendelse = createInngaaendeHendelse();
		inngaaendeHendelsePublisher.publish(hendelse);

		verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
		verify(meterRegistry, times(1)).timer(anyString(), anyString(), anyString(), anyString(), anyString());
		verify(timerMock, times(1)).record(0L, TimeUnit.MILLISECONDS);
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