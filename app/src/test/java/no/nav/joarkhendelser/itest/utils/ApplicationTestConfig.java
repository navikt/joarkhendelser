package no.nav.joarkhendelser.itest.utils;


import lombok.extern.slf4j.Slf4j;
import no.nav.joarkhendelser.ApplicationConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Slf4j
@Configuration
@Import(value = {ApplicationConfig.class})
public class ApplicationTestConfig {}
