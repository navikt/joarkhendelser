package no.nav.joarkjournalfoeringhendelser;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

@Import(value = {
		ApplicationConfig.class,
})
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class)
				.listeners(new ApplicationErrorListener())
				.run(args);
	}
}
