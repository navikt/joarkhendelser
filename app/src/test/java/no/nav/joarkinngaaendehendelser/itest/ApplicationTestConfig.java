package no.nav.joarkinngaaendehendelser.itest;


import lombok.extern.slf4j.Slf4j;
import no.nav.joarkinngaaendehendelser.itest.config.KafkaTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@Import(value = {KafkaTestConfig.class})
@Profile("itest")
public class ApplicationTestConfig {

}
