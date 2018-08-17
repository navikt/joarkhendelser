package no.nav.dokarkivhendelser;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import no.nav.dokarkivhendelser.config.KafkaConfig;

@ComponentScan
@EnableAutoConfiguration
public class ApplicationConfig {
}
