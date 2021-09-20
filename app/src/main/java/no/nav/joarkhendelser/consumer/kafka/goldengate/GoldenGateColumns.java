package no.nav.joarkhendelser.consumer.kafka.goldengate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoldenGateColumns {

	@JsonProperty("JOURNALPOST_ID")
	private Integer journalpostId;

	@JsonProperty("K_FAGOMRADE")
	private String fagomraade;

	@JsonProperty("K_JOURNAL_S")
	private String journalpoststatus;

	@JsonProperty("K_JOURNALPOST_T")
	private String journalposttype;

	@JsonProperty("K_MOTTAKS_KANAL")
	private String mottakskanal;

	@JsonProperty("KANAL_REFERANSE_ID")
	private String kanalreferanseId;

	@JsonProperty("K_BEHANDLINGSTEMA")
	private String behandlingstema;

	// --- Alt som er utkommentert er ikkje ein del av output InngaaendeHendelse ---
	/* public String JOURNALF_ENHET;
	 public String OPPRETTET_AV_NAVN;
	 public DATO_JOURNAL; // 2018-09-24 12:21:57.183000000
	 public int ANTALL_RETUR;
	 public DATO_AVS_RETUR; // alltid null?
	 public String INNHOLD;
	 public KRAV_TYPE; // alltid null?
	 public MERKNAD; // alltid null?
	 public FORDELING; // alltid null?
	 public String ORIGINAL_BESTILT;
	 public String ENDRET_AV_NAVN;
	 public String OPPRETTET_AV;
	 public DATO_ENDRET; // 2018-08-09 10:42:35.726000000
	 public String ENDRET_AV; // alltid null?
	 public DATO_SENDT_PRINT; // alltid null?
	 public int VERSJON;
	 public DATO_OPPRETTET; // 2018-08-09 10:41:47.710000000
	 public DATO_DOKUMENT; // 2019-08-10 00:00:00.000000000
	 public String AVSEND_MOTTAKER;
	 public String AVSEND_MOTTAK_ID;
	 public String JOURNALFORT_AV_NAVN;
	 public DATO_MOTTATT; // 2018-08-09 00:00:00.000000000
	 public LAND; // alltid null?
	 public K_FAKT_DIS_KANAL; // alltid null?
	 public String ELEKTRONISK_DISTR;
	 public DATO_EKSPEDERT; // alltid null?
	 public DATO_LEST; // alltid null?
	 public MOTTATT_ADRESSAT; // alltid null?
	 public String OPPRETTET_KILDE_NAVN;
	 public K_UTSENDINGS_KANAL; // alltid null?
	 public String ENDRET_KILDE_NAVN;
	 public SIGNATUR; // alltid null?*/
}
