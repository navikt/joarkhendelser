package no.nav.joarkjournalfoeringhendelser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;

@Slf4j
public class ApplicationErrorListener implements ApplicationListener<ApplicationFailedEvent> {

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        if (event.getException() != null) {
            log.error(event.getException().getClass().getName() + " occured");
            log.error(event.getException().getMessage(), event.getException());
            log.error("STOPPING APPLICATION!!");
            //event.getApplicationContext().close();
            //System.exit(-1);
        }
    }
}