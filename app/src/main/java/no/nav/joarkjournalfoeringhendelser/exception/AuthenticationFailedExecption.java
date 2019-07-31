package no.nav.joarkjournalfoeringhendelser.exception;

public class AuthenticationFailedExecption extends JoarkJournalfoeringHendelseTechnicalException {
	public AuthenticationFailedExecption(String s, Throwable t) {
		super(s, t);
	}

	public AuthenticationFailedExecption(String s) {
		super(s);
	}
}
