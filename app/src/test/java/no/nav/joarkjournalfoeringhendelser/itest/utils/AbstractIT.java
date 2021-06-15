package no.nav.joarkjournalfoeringhendelser.itest.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SpringBootTest(
		classes = {ApplicationTestConfig.class},
		properties = "spring.main.allow-bean-definition-overriding=true"
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("itest")
@EmbeddedKafka(
		topics = {
				"test-ut-topic",
				"test-inn-topic",
		},
		bootstrapServersProperty = "spring.kafka.bootstrap-servers",
		brokerProperties = {
				"offsets.topic.replication.factor=1",
				"transaction.state.log.replication.factor=1",
				"transaction.state.log.min.isr=1"
		},
		partitions = 1
)
public abstract class AbstractIT {

	@Value("${journalfoeringHendelse-v1.topic}")
	public static String UT_TOPIC = "test-ut-topic";

	@Value("${journalpostEndret.topic}")
	public static String INN_TOPIC = "test-inn-topic";

	@Autowired
	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	public EmbeddedKafkaBroker kafkaEmbedded;

	public static Consumer<String, JournalfoeringHendelseRecord> consumer;

	public static Producer<String, String> producer;

	@BeforeAll
	public void setUpClass() {
		// KafkaConsumer for å kunne konsumere meldinger som InngaaendeHendelsePublisher dytter til 'test-ut-topic'
		this.setUpConsumerForTopicUt();

		// KafkaProducer for å kunne produsere meldinger til topic 'test-inn-topic' som konsumeres av JournalpostEndretConsumer
		Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(kafkaEmbedded));
		producer = new DefaultKafkaProducerFactory<>(
				configs,
				new StringSerializer(),
				new StringSerializer()
		).createProducer();
		Awaitility.setDefaultPollInterval(Duration.ofSeconds(1));
	}

	protected void sendToInnTopic(JsonNode value) {
		producer.send(new ProducerRecord<>(INN_TOPIC, value.toString()));
		producer.flush();
	}

	protected JsonNode classpathToJsonNode(String classpathResource) throws IOException {
		InputStream inputStream = new ClassPathResource(classpathResource).getInputStream();
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readTree(inputStream);
	}

	public List<JournalfoeringHendelseRecord> getAllCurrentRecordsOnTopicUt() {
		return StreamSupport.stream(KafkaTestUtils.getRecords(consumer, 2000).records(UT_TOPIC).spliterator(), false)
				.map(ConsumerRecord::value)
				.collect(Collectors.toList());
	}

	public void setUpConsumerForTopicUt() {
		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("dittnv-consumer", "true", kafkaEmbedded);
		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");
		consumerProps.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "mock://localhost");
		consumerProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");

		consumer = new DefaultKafkaConsumerFactory<String, JournalfoeringHendelseRecord>(consumerProps)
				.createConsumer();
		consumer.subscribe(Collections.singletonList(UT_TOPIC));
	}
}
