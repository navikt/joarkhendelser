package no.nav.joarkhendelser.exception;

public class AuthenticationFailedException extends JoarkhendelserTechnicalException {

	public AuthenticationFailedException(String s, Throwable t) {
		super(s, t);
	}
}
