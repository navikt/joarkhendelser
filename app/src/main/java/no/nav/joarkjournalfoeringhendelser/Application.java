package no.nav.joarkjournalfoeringhendelser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

@Import(value = {
		ApplicationConfig.class,
})
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		ConfigurableApplicationContext run =
				SpringApplication.run(Application.class, args);
		run.addApplicationListener(new ApplicationErrorListener());
	}
}
