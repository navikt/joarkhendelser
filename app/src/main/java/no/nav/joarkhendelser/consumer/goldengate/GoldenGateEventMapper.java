package no.nav.joarkhendelser.consumer.goldengate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
public class GoldenGateEventMapper {

	private final JsonMapper mapper = JsonMapper.builder().build();

	public GoldenGateEvent mapToEvent(String message) {
		try {
			return mapper.readValue(message, GoldenGateEvent.class);
		} catch (JacksonException e) {
			log.warn("Mapping til GoldenGateEvent feilet. Feilmelding: {}", e.getMessage());
			return null;
		}
	}
}