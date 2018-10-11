package no.nav.joarkinngaaendehendelser.itest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.test.rule.KafkaEmbedded;

@EnableKafka
@Configuration
@Profile("itest")
public class KafkaTestConfig {

	@Value("${journalpostEndret.topic}")
	private String journalpostEndretTopic;

	@Value("${journalfoeringHendelse-v1.topic}")
	private String journalfoeringTopic;

	@Bean
	@Order(1)
	public KafkaEmbedded kafkaEmbedded() {
		KafkaEmbedded embedded = new KafkaEmbedded(1, true, 1, journalfoeringTopic, journalpostEndretTopic);
		embedded.setKafkaPorts(60172);
		embedded.brokerProperty("offsets.topic.replication.factor", (short) 1);
		embedded.brokerProperty("transaction.state.log.replication.factor", (short) 1);
		embedded.brokerProperty("transaction.state.log.min.isr", 1);
		return embedded;
	}

}
