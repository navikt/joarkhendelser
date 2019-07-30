package no.nav.joarkjournalfoeringhendelser.nais;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.TopicListing;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class NaisContract {

	public static final String APPLICATION_ALIVE = "Application is alive!";
	public static final String APPLICATION_READY = "Application is ready for traffic!";
	private static final String APPLICATION_NOT_READY = "Application is not ready for traffic :-(";
	private static AtomicInteger isReady = new AtomicInteger(1);
	private final AdminClient kafkaAdminClient;

	public NaisContract(MeterRegistry meterRegistry, AdminClient kafkaAdminClient) {
		this.kafkaAdminClient = kafkaAdminClient;
		Gauge.builder("dok_app_is_ready", isReady, AtomicInteger::get).register(meterRegistry);
	}

	private List<String> findTopicNames(){
		Properties properties = System.getProperties();
		List<String> topicNames = properties.keySet().stream().map(key -> System.getProperty((String) key)).filter(key -> key.contains("topic") || key.contains("TOPIC")).collect(Collectors.toList());
		if (topicNames.isEmpty()) {
			Map<String, String> env = System.getenv();
			topicNames = env.keySet().stream().filter(key -> key.contains("topic") || key.contains("TOPIC")).collect(Collectors.toList());
		}
		return topicNames;
	}

	private boolean topicsAreHealthy() throws ExecutionException, InterruptedException {
		List<String> topicListingNames = kafkaAdminClient.listTopics(new ListTopicsOptions().timeoutMs(10000)).listings().get().stream().map(TopicListing::name).collect(Collectors.toList());
		List<String> topicNames = findTopicNames();
		log.info(String.format("%s -- %s", String.join(",", topicListingNames), String.join(",", topicNames)));
		return topicListingNames.containsAll(topicNames);
	}

	@GetMapping("/isAlive")
	public ResponseEntity isAlive() throws ExecutionException, InterruptedException {
		if (!topicsAreHealthy()){
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(APPLICATION_ALIVE);
	}

	@RequestMapping(value = "/isReady", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity isReady() {
		return new ResponseEntity<>(APPLICATION_READY, HttpStatus.OK);
	}
}
