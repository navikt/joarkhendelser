package no.nav.joarkhendelser.consumer.kafka.goldengate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoldenGateEventMapper {

	public static GoldenGateEvent mapToEvent(String message) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(message, GoldenGateEvent.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw e;
		}
	}
}