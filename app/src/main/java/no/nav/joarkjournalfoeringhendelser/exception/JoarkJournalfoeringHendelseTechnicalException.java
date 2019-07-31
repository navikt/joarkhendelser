package no.nav.joarkjournalfoeringhendelser.exception;

public class JoarkJournalfoeringHendelseTechnicalException extends RuntimeException {
    public JoarkJournalfoeringHendelseTechnicalException(String s, Throwable t) {
        super(s, t);
    }

    public JoarkJournalfoeringHendelseTechnicalException(String s) {
        super(s);
    }
}
