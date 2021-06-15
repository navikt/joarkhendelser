package no.nav.joarkjournalfoeringhendelser.consumer.kafka;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelse;
import no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelseProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashSet;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static no.nav.joarkjournalfoeringhendelser.consumer.kafka.JoarkSchema.JOURNALPOST;
import static no.nav.joarkjournalfoeringhendelser.consumer.kafka.JournalpostStatus.INNGAAENDE;
import static no.nav.joarkjournalfoeringhendelser.consumer.kafka.JournalpostStatus.UTGAR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {JournalpostEndretConsumer.class})
public class JournalpostEndretConsumerTest {

	@Autowired
	private JournalpostEndretConsumer listener;

	@Mock
	private ConsumerRecord<?, byte[]> consumerRecordMock;
	@MockBean
	private ConsumerRecordAsJsonConverter converterMock;
	@MockBean
	private InngaaendeHendelseProducer publisher;
	@MockBean
	private MeterRegistry meterRegistry;
	@Mock
	private Counter counterMock;
	@Mock
	private Timer timerMock;

	private JournalpostEndretEvent event;

	@BeforeEach
	public void beforeAll() {
		when(meterRegistry.counter(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(counterMock);
		when(meterRegistry.timer(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(timerMock);
	}

	@Test
	public void shouldPublishEvent() {
		event = createJournalpostEndretEvent(INNGAAENDE);
		when(converterMock.convertRecordToEvent(any(ConsumerRecord.class))).thenReturn(event);

		listener.onMessage(consumerRecordMock);

		verify(converterMock).convertRecordToEvent(consumerRecordMock);
		verify(publisher).publish(any(InngaaendeHendelse.class));
		verify(meterRegistry, times(1)).counter(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
		verify(counterMock, times(1)).increment();
		verify(counterMock, times(1)).increment();
		verify(meterRegistry, times(1)).timer(anyString(), anyString(), anyString(), anyString(), anyString());
		verify(timerMock, times(1)).record(0L, MILLISECONDS);
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