package no.nav.joarkjournalfoeringhendelser.config;

public class AuthenticationFailedExecption extends JoarkJournalfoeringHendelseTechnicalException {
	public AuthenticationFailedExecption(String s, Throwable t) {
		super(s, t);
	}

	public AuthenticationFailedExecption(String s) {
		super(s);
	}
}
