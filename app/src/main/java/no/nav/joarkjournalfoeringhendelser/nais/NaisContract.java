package no.nav.joarkjournalfoeringhendelser.nais;

import static no.nav.joarkjournalfoeringhendelser.config.KafkaConfig.N_CONCURRENCY;
import static no.nav.joarkjournalfoeringhendelser.config.KafkaErrorHandler.authorizationErrorCounter;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
public class NaisContract {

	public static final String APPLICATION_ALIVE = "Application is alive!";
	public static final String APPLICATION_READY = "Application is ready for traffic!";
	private static final String APPLICATION_NOT_READY = "Application is not ready for traffic :-(";
	private static AtomicInteger isReady = new AtomicInteger(1);

	public NaisContract(MeterRegistry meterRegistry) {
		Gauge.builder("dok_app_is_ready", isReady, AtomicInteger::get).register(meterRegistry);
	}

	//TODO: Brukes bare for testing, fjern før prod
	@GetMapping("/increment")
	public String increment(){
		log.info("Har økt errorCounter til {}", authorizationErrorCounter.get());
		return String.format("Har økt errorCounter til %s", authorizationErrorCounter.get());
	}

	@GetMapping("/isAlive")
	public ResponseEntity isAlive() {
		if (authorizationErrorCounter.get()>N_CONCURRENCY*5){
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(APPLICATION_ALIVE);
	}

	@RequestMapping(value = "/isReady", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity isReady() {
		return new ResponseEntity<>(APPLICATION_READY, HttpStatus.OK);
	}
}
