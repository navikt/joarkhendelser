package no.nav.joarkhendelser.producer;

public enum InngaaendeHendelsesType {
	JOURNALPOST_MOTTATT("JournalpostMottatt"),
	TEMA_ENDRET("TemaEndret"),
	ENDELIG_JOURNALFORT("EndeligJournalført"),
	JOURNALPOST_UTGATT("JournalpostUtgått");

	private final String pretty;

	InngaaendeHendelsesType(String value) {
		pretty = value;
	}

	@Override
	public String toString() {
		return pretty;
	}
}
