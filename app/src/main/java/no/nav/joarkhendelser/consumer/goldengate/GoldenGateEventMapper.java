package no.nav.joarkhendelser.consumer.goldengate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GoldenGateEventMapper {

	private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

	public GoldenGateEvent mapToEvent(String message) {
		try {
			return mapper.readValue(message, GoldenGateEvent.class);
		} catch (JsonProcessingException e) {
			log.warn("Mapping til GoldenGateEvent feilet. Feilmelding: {}", e.getMessage());
			return null;
		}
	}
}