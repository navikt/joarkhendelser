package no.nav.joarkhendelser.itest;

import no.nav.joarkhendelser.itest.utils.AbstractIT;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.joarkhendelser.producer.Hendelsestype.ENDELIG_JOURNALFOERT;
import static no.nav.joarkhendelser.producer.Hendelsestype.JOURNALPOST_MOTTATT;
import static no.nav.joarkhendelser.producer.Hendelsestype.JOURNALPOST_UTGAATT;
import static no.nav.joarkhendelser.producer.Hendelsestype.TEMA_ENDRET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JoarkhendelserIT extends AbstractIT {

	private static final Long JOURNALPOST_ID_UTGAAENDE = 105L;

	/**
	 * HVIS man mottar en Insert med JournalStatus = M, skal MidlertidigJournalfoert-hendelse publiseres til utgaaende topic
	 */
	@Test
	public void shouldPublishJournalpostMottattHendelse() throws Exception {
		sendToInnTopic(classpathToJsonNode("__files/midlertidig_journalfoert.json"));

		await().atMost(5, SECONDS).untilAsserted(() -> {
			List<JournalfoeringHendelseRecord> records = getAllCurrentRecordsOnTopicUt();
			assertEquals(1, records.size());
			JournalfoeringHendelseRecord utgaaendeRecord = records.get(0);
			assertEquals(1L, utgaaendeRecord.getJournalpostId());
			assertEquals(JOURNALPOST_MOTTATT.toString(), utgaaendeRecord.getHendelsesType());
		});
	}

	/**
	 * HVIS man mottar en Update med endring av Tema, skal TemaEndret-hendelse publiseres til utgaaende topic
	 */
	@Test
	public void shouldPublishTemaEndretHendelse() throws Exception {
		sendToInnTopic(classpathToJsonNode("__files/tema_endret.json"));

		await().atMost(5, SECONDS).untilAsserted(() -> {
			List<JournalfoeringHendelseRecord> records = getAllCurrentRecordsOnTopicUt();
			assertEquals(1, records.size());
			JournalfoeringHendelseRecord utgaaendeRecord = records.get(0);
			assertEquals(1L, utgaaendeRecord.getJournalpostId());
			assertEquals(TEMA_ENDRET.toString(), utgaaendeRecord.getHendelsesType());
			assertEquals("SAK", utgaaendeRecord.getTemaGammelt());
			assertEquals("AAP", utgaaendeRecord.getTemaNytt());
		});
	}

	/**
	 * HVIS man mottar en Insert med JournalStatus = J, skal EndeligJournalfoert-hendelse publiseres til utgaaende topic
	 */
	@Test
	public void shouldPublishEndeligJournalfoertHendelse() throws Exception {
		sendToInnTopic(classpathToJsonNode("__files/endelig_journalfoert.json"));

		await().atMost(5, SECONDS).untilAsserted(() -> {
			List<JournalfoeringHendelseRecord> records = getAllCurrentRecordsOnTopicUt();
			assertEquals(1, records.size());
			JournalfoeringHendelseRecord utgaaendeRecord = records.get(0);
			assertEquals(1L, utgaaendeRecord.getJournalpostId());
			assertEquals(ENDELIG_JOURNALFOERT.toString(), utgaaendeRecord.getHendelsesType());
		});
	}

	/**
	 * HVIS man mottar en Update med endring av JournalStatus = U, skal JournalpostUtgaatt-hendelse publiseres til utgaaende topic
	 */
	@Test
	public void shouldPublishJournalpostUtgaattHendelse() throws Exception {
		sendToInnTopic(classpathToJsonNode("__files/journalpost_utgaatt.json"));
		await().atMost(5, SECONDS).untilAsserted(() -> {
			List<JournalfoeringHendelseRecord> records = getAllCurrentRecordsOnTopicUt();
			assertEquals(1, records.size());
			JournalfoeringHendelseRecord utgaaendeRecord = records.get(0);
			assertEquals(1L, utgaaendeRecord.getJournalpostId());
			assertEquals(JOURNALPOST_UTGAATT.toString(), utgaaendeRecord.getHendelsesType());
		});
	}

	/**
	 * HVIS man mottar en Insert med K_JOURNALPOST_T = U, skal det _ikke_ publiseres noen hendelse til utgaaende topic
	 */
	@Test
	public void shouldNotPublishUtgaaendeEvent() throws Exception {
		sendToInnTopic(classpathToJsonNode("__files/journalpost_utgaaende.json"));

		await().atMost(5, SECONDS).untilAsserted(() -> {
			List<JournalfoeringHendelseRecord> records = getAllCurrentRecordsOnTopicUt();
			assertEquals(0, records.size());
			assertThat(records).extracting(JournalfoeringHendelseRecord::getJournalpostId).doesNotContain(JOURNALPOST_ID_UTGAAENDE);
		});
	}

	/**
	 * HVIS man mottar en Delete skal det _ikke_ publiseres noen hendelse til utgaaende topic
	 */
	@Test
	public void shouldNotPublishSlettetEvent() throws Exception {
		sendToInnTopic(classpathToJsonNode("__files/slettet.json"));

		await().atMost(5, SECONDS).untilAsserted(() -> {
			List<JournalfoeringHendelseRecord> records = getAllCurrentRecordsOnTopicUt();
			assertEquals(0, records.size());
		});
	}
}