package no.nav.joarkjournalfoeringhendelser.nais;

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

	@GetMapping("/isAlive")
	public ResponseEntity isAlive() {
		return ResponseEntity.ok(APPLICATION_ALIVE);
	}

	@RequestMapping(value = "/isReady", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity isReady() {
		return new ResponseEntity<>(APPLICATION_READY, HttpStatus.OK);
	}
}
