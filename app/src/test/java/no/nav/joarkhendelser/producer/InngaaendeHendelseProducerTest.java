package no.nav.joarkhendelser.producer;

import no.nav.joarkhendelser.consumer.JournalpostEndretEvent;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.JOURNALPOST_MOTTATT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {InngaaendeHendelseProducer.class})
public class InngaaendeHendelseProducerTest {

	@Autowired
	InngaaendeHendelseProducer inngaaendeHendelsePublisher;

	@MockBean
	private KafkaTemplate<String, JournalfoeringHendelseRecord> kafkaTemplate;
	@Mock
	private ListenableFuture listenableFuture;

	@BeforeEach
	public void setUp() throws Exception {
		TopicPartition topicPartition = new TopicPartition("Top", 1);
		RecordMetadata recordMetadata = new RecordMetadata(topicPartition, 1, 1, 1, 1L, 1 ,1);

		when(kafkaTemplate.send(any(ProducerRecord.class)))
				.thenReturn(listenableFuture);
		when(listenableFuture.get())
				.thenReturn(new SendResult<String, JournalfoeringHendelseRecord>(null, recordMetadata));
	}

	@Test
	public void shouldPublish() {
		InngaaendeHendelse hendelse = createInngaaendeHendelse();
		inngaaendeHendelsePublisher.publish(hendelse);

		verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
		assertEquals(JOURNALPOST_MOTTATT.toString(), hendelse.getHendelsesType());
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