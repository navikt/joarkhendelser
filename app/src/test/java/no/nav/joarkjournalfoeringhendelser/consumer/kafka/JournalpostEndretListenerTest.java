package no.nav.joarkjournalfoeringhendelser.consumer.kafka;

import static no.nav.joarkjournalfoeringhendelser.consumer.kafka.JoarkSchema.JOURNALPOST;
import static no.nav.joarkjournalfoeringhendelser.consumer.kafka.JournalpostStatus.INNGAAENDE;
import static no.nav.joarkjournalfoeringhendelser.consumer.kafka.JournalpostStatus.UTGAR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelse;
import no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelsePublisher;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;

@RunWith(MockitoJUnitRunner.class)
public class JournalpostEndretListenerTest {

	@Mock
	private ConsumerRecord<?, byte[]> consumerRecordMock;
	@Mock
	private ConsumerRecordAsJsonConverter converterMock;
	@Mock
	private InngaaendeHendelsePublisher publisher;
	@Mock
	private MeterRegistry meterRegistry;
	@Mock
	private Counter counterMock;
	@InjectMocks
	private JournalpostEndretListener listener;

	private JournalpostEndretEvent event;

	@Before
	public void before() {
		when(meterRegistry.counter(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(counterMock);
	}

	@Test
	public void shouldPublishEvent() {
		event = createJournalpostEndretEvent(INNGAAENDE);
		when(converterMock.convertRecordToEvent(any(ConsumerRecord.class))).thenReturn(event);

		listener.onMessage(consumerRecordMock);

		verify(converterMock).convertRecordToEvent(consumerRecordMock);
		verify(publisher).publish(any(InngaaendeHendelse.class));
		verify(meterRegistry, times(1)).counter(anyString(), anyString(), anyString(), anyString(), anyString());
		verify(counterMock, times(1)).increment();
	}

	@Test
	public void shouldNotPublishAnyEventIfJournalpostTypeIsNotInngaaende() {
		event = createJournalpostEndretEvent(UTGAR);
		when(converterMock.convertRecordToEvent(any(ConsumerRecord.class))).thenReturn(event);

		listener.onMessage(consumerRecordMock);

		verify(converterMock, times(1)).convertRecordToEvent(consumerRecordMock);
		verify(publisher, times(0)).publish(any(InngaaendeHendelse.class));
	}

	private JournalpostEndretEvent createJournalpostEndretEvent(String journalpostType) {
		HashSet<String> columnsChanged = new HashSet<>();
		columnsChanged.add(JOURNALPOST);

		return JournalpostEndretEvent.builder()
				.journalpostId(123L)
				.fagomradeBefore("FOR")
				.fagomradeAfter("BAR")
				.journalpostStatusAfter("M")
				.journalpostType(journalpostType)
				.operation("U")
				.columnsChanged(columnsChanged)
				.build();
	}
}