package no.nav.joarkinngaaendehendelser.itest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import no.nav.joarkinngaaendehendelser.Application;
import no.nav.joarkinngaaendehendelser.itest.utils.CustomAvroDeserializer;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RunWith(SpringRunner.class)
@Import(ApplicationTestConfig.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("itest")
public abstract class AbstractIT {

	static final String INN_TOPIC = "test-inn-topic";
	static final String UT_TOPIC = "test-ut-topic";
	@Autowired
	protected KafkaEmbedded kafkaEmbedded;
	KafkaTemplate<Object, Object> kafkaTemplate;
	KafkaConsumer<String, JournalfoeringHendelseRecord> consumer;

	ProducerFactory<Object, Object> producerFactory() {
		Map<String, Object> config = new HashMap<>();
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:60172");
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSerializer.class.getName());
		return new DefaultKafkaProducerFactory<>(config);
	}

	Map<String, Object> consumerProperties() {
		Map<String, Object> config = new HashMap<>();
		config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:60172");
		config.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
		config.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "bogus");
		config.put(ConsumerConfig.GROUP_ID_CONFIG, "itest");
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, CustomAvroDeserializer.class.getName());
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CustomAvroDeserializer.class.getName());
		return config;
	}

	void sendToTopic(ProducerRecord<Object, Object> record) {
		try {
			kafkaTemplate.execute(operations -> {
				try {
					Future<RecordMetadata> send = operations.send(record);
					send.get();
					return null;
				} catch (InterruptedException | ExecutionException e) {
					throw new RuntimeException("Failed to send", e);
				}
			});

		} catch (Exception e) {
			throw new RuntimeException("Failed to send Kafka message", e);
		}
	}

	JsonNode classpathToJsonNode(String classpathResource) throws IOException {
		InputStream inputStream = new ClassPathResource(classpathResource).getInputStream();
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readTree(inputStream);
	}
}
