package no.nav.joarkinngaaendehendelser;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import no.nav.joarkinngaaendehendelser.config.KafkaConfig;
import no.nav.joarkinngaaendehendelser.nais.NaisContract;

@Configuration
@Import(value = {
		ApplicationConfig.class,
		NaisContract.class,
		KafkaConfig.class
})
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class).run(args);
	}
}
