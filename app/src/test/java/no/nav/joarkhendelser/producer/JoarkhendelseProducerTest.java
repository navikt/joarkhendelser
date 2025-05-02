package no.nav.joarkhendelser.producer;

import no.nav.joarkhendelser.consumer.JournalpostEndretEvent;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEvent;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static no.nav.joarkhendelser.producer.Hendelsestype.JOURNALPOST_MOTTATT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {JoarkhendelseProducer.class})
public class JoarkhendelseProducerTest {

	@Autowired
	JoarkhendelseProducer joarkhendelseProducer;

	@MockitoBean
	private KafkaTemplate<String, JournalfoeringHendelseRecord> kafkaTemplate;

	@Mock
	private CompletableFuture completableFuture;

	@BeforeEach
	public void setUp() throws Exception {
		TopicPartition topicPartition = new TopicPartition("Top", 1);
		RecordMetadata recordMetadata = new RecordMetadata(topicPartition, 1, 1, 1, 1, 1);

		when(kafkaTemplate.send(any(ProducerRecord.class)))
				.thenReturn(completableFuture);
		when(completableFuture.get())
				.thenReturn(new SendResult<String, JournalfoeringHendelseRecord>(null, recordMetadata));
	}

	@Test
	public void shouldPublish() {
		Joarkhendelse hendelse = createInngaaendeHendelse();
		joarkhendelseProducer.publish(hendelse);

		verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
		assertEquals(JOURNALPOST_MOTTATT.toString(), hendelse.getHendelsestype());
	}

	private Joarkhendelse createInngaaendeHendelse() {
		JournalpostEndretEvent journalpostEndretEvent = JournalpostEndretEvent.builder()
				.journalpostId(1L)
				.journalpostStatusAfter("MO")
				.journalpostStatusBefore("MO")
				.operation("I")
				.journalpostType("I")
				.kanalReferanseId("kanal-ref")
				.build();

		GoldenGateEvent goldenGateEvent = new GoldenGateEvent();
		goldenGateEvent.setOperationTimestamp(LocalDateTime.now());

		return JoarkhendelseMapper.map(journalpostEndretEvent, goldenGateEvent);
	}
}