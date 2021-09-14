package no.nav.joarkhendelser.itest;

import no.nav.joarkhendelser.itest.utils.AbstractIT;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.ENDELIG_JOURNALFORT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.JOURNALPOST_MOTTATT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.JOURNALPOST_UTGATT;
import static no.nav.joarkhendelser.producer.InngaaendeHendelsesType.TEMA_ENDRET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JoarkJournalfoeringHendelserIT extends AbstractIT {

	private static final Long JOURNALPOST_ID_UTGAAENDE = 105L;

	/**
	 * HVIS man mottar en Update med endring av Tema, skal TemaEndret-hendelse publiseres til utgaaende topic
	 */
	@Test
	public void shouldPublishNewTemaEndretHendelse() throws Exception {
		sendToInnTopic(classpathToJsonNode("__files/tema_endret.json"));

		await().atMost(10, SECONDS).untilAsserted(() -> {
			List<JournalfoeringHendelseRecord> records = this.getAllCurrentRecordsOnTopicUt();
			assertEquals(1, records.size());
			JournalfoeringHendelseRecord utgaaendeRecord = records.get(0);
			assertEquals(1L, utgaaendeRecord.getJournalpostId().longValue());
			assertEquals(TEMA_ENDRET.toString(), utgaaendeRecord.getHendelsesType().toString());
			assertEquals("SAK", utgaaendeRecord.getTemaGammelt().toString());
			assertEquals("AAP", utgaaendeRecord.getTemaNytt().toString());
		});
	}

	/**
	 * HVIS man mottar en Insert med JournalStatus = M, skal MidlertidigJournalfoert-hendelse publiseres til utgaaende topic
	 */
	@Test
	public void shouldPublishNewMidlertidigJournalfoertHendelse() throws Exception {
		sendToInnTopic(classpathToJsonNode("__files/midlertidig_jf.json"));

		await().atMost(10, SECONDS).untilAsserted(() -> {
			List<JournalfoeringHendelseRecord> records = this.getAllCurrentRecordsOnTopicUt();
			assertEquals(1, records.size());
			JournalfoeringHendelseRecord utgaaendeRecord = records.get(0);
			assertEquals(1L, utgaaendeRecord.getJournalpostId().longValue());
			assertEquals(JOURNALPOST_MOTTATT.toString(), utgaaendeRecord.getHendelsesType().toString());
		});
	}

	/**
	 * HVIS man mottar en Insert med JournalStatus = J, skal EndeligJournalfoert-hendelse publiseres til utgaaende topic
	 */
	@Test
	public void shouldPublishNewEndligJournalfoertHendelse() throws Exception {
		sendToInnTopic(classpathToJsonNode("__files/endelig_jf.json"));

		await().atMost(10, SECONDS).untilAsserted(() -> {
			List<JournalfoeringHendelseRecord> records = this.getAllCurrentRecordsOnTopicUt();
			assertEquals(1, records.size());
			JournalfoeringHendelseRecord utgaaendeRecord = records.get(0);
			assertEquals(1L, utgaaendeRecord.getJournalpostId().longValue());
			assertEquals(ENDELIG_JOURNALFORT.toString(), utgaaendeRecord.getHendelsesType().toString());
		});
	}

	/**
	 * HVIS man mottar en Update med endring av JournalStatus = U, skal JournalpostUtgaatt-hendelse publiseres til utgaaende topic
	 */
	@Test
	public void shouldPublishNewJournalpostUtgaattHendelse() throws Exception {
		sendToInnTopic(classpathToJsonNode("__files/journalpost_utgaatt_med_update.json"));
		await().atMost(10, SECONDS).untilAsserted(() -> {
			List<JournalfoeringHendelseRecord> records = this.getAllCurrentRecordsOnTopicUt();
			assertEquals(1, records.size());
			JournalfoeringHendelseRecord utgaaendeRecord = records.get(0);
			assertEquals(1L, utgaaendeRecord.getJournalpostId().longValue());
			assertEquals(JOURNALPOST_UTGATT.toString(), utgaaendeRecord.getHendelsesType().toString());
		});
	}

	/**
	 * HVIS man mottar en Insert med K_JOURNALPOST_T = U, skal det _ikke_ publiseres noen hendelse til utgaaende topic
	 */
	@Test
	public void shouldNotPublishUtgaaendeEvent() throws Exception {
		sendToInnTopic(classpathToJsonNode("__files/journalpost_utgaaende.json"));

		await().atLeast(1, SECONDS).atMost(10, SECONDS).untilAsserted(() -> {
			List<JournalfoeringHendelseRecord> records = this.getAllCurrentRecordsOnTopicUt();
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

		await().atMost(10, SECONDS).untilAsserted(() -> {
			List<JournalfoeringHendelseRecord> records = this.getAllCurrentRecordsOnTopicUt();
			assertEquals(0, records.size());
		});
	}

	/**
	 * HVIS man mottar en Update med null-verdier i after, så skal det publiseres hendelse til utgaaende topic
	 */
	@Test
	public void shouldNotProduceNullPointer() throws Exception {
		sendToInnTopic(classpathToJsonNode("__files/nullpointer.json"));

		await().atMost(10, SECONDS).untilAsserted(() -> {
			List<JournalfoeringHendelseRecord> records = this.getAllCurrentRecordsOnTopicUt();
			assertEquals(1, records.size());
			JournalfoeringHendelseRecord utgaaendeRecord = records.get(0);
			assertEquals(123456789L, utgaaendeRecord.getJournalpostId().longValue());
			assertEquals(ENDELIG_JOURNALFORT.toString(), utgaaendeRecord.getHendelsesType().toString());
		});
	}
}