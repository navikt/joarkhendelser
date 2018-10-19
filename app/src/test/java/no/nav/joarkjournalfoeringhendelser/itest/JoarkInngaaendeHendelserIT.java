package no.nav.joarkjournalfoeringhendelser.itest;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import com.fasterxml.jackson.databind.JsonNode;
import no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelseRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class JoarkInngaaendeHendelserIT extends AbstractIT {

	@Before
	public void setUp() {
		// KafkaTemplate for å sende meldinger til 'test-inn-topic' som JournalpostEndretListener skal fange opp.
		kafkaTemplate = new KafkaTemplate<>(producerFactory());
		// KafkaConsumer for å kunne konsumere meldinger som InngaaendeHendelsePublisher dytter til 'test-ut-topic'
		consumer = new KafkaConsumer<>(consumerProperties());
		consumer.subscribe(Collections.singletonList(UT_TOPIC));
	}

	/**
	 * test-beskrivelse goes here
	 */
	@Test
	public void happyPath() throws Exception {
		JsonNode jsonrecord = classpathToJsonNode("__files/happyPath.json");

		ProducerRecord<Object, Object> record = new ProducerRecord<>(INN_TOPIC, 0, "key", jsonrecord);
		sendToTopic(record);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			List<ConsumerRecord<String, InngaaendeHendelseRecord>> records = KafkaTestUtils.getRecords(consumer).records(new TopicPartition(UT_TOPIC, 0));
			assertThat(records, hasSize(1));
		});
	}

}
