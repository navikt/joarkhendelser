package no.nav.joarkhendelser.producer;

public enum Hendelsestype {

	JOURNALPOST_MOTTATT("JournalpostMottatt"),
	TEMA_ENDRET("TemaEndret"),
	ENDELIG_JOURNALFOERT("EndeligJournalført"),
	JOURNALPOST_UTGAATT("JournalpostUtgått");

	private final String pretty;

	Hendelsestype(String value) {
		pretty = value;
	}

	@Override
	public String toString() {
		return pretty;
	}
}
