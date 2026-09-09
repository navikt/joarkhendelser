package no.nav.joarkhendelser.config;

import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import org.apache.avro.util.ClassSecurityValidator;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AvroSecurityConfig {

	static {
		// Avro deserialiserer kun til klasser som er i en allowlist; alt som ikke er med her vil feile
		ClassSecurityValidator.setGlobal(ClassSecurityValidator.composite(
				ClassSecurityValidator.DEFAULT,
				ClassSecurityValidator.builder()
						.add(JournalfoeringHendelseRecord.class)
						.build()));
	}
}
