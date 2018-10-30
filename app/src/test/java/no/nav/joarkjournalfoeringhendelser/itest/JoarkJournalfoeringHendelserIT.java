package no.nav.joarkjournalfoeringhendelser.itest;

import static no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelsesType.ENDELIG_JOURNALFORT;
import static no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelsesType.JOURNALPOST_UTGATT;
import static no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelsesType.MIDLERTIDIG_JOURNALFORT;
import static no.nav.joarkjournalfoeringhendelser.producer.InngaaendeHendelsesType.TEMA_ENDRET;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;

import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class JoarkJournalfoeringHendelserIT extends AbstractIT {

	private ProducerRecord<Object, Object> record;

	/**
	 * HVIS man mottar en Update med endring av Tema, skal TemaEndret-hendelse publiseres til utgaaende topic
	 */
	@Test
	public void shouldPublishNewTemaEndretHendelse() throws Exception {
		JsonNode jsonrecord = classpathToJsonNode("__files/tema_endret.json");

		record = new ProducerRecord<>(INN_TOPIC, 0, "key", jsonrecord);
		sendToTopic(record);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			List<ConsumerRecord<String, JournalfoeringHendelseRecord>> records = KafkaTestUtils.getRecords(consumer).records(new TopicPartition(UT_TOPIC, 0));
			assertThat(records, hasSize(1));
			JournalfoeringHendelseRecord utgaaendeRecord = records.get(0).value();
			assertEquals(1L, utgaaendeRecord.getJournalpostId().longValue());
			assertEquals(TEMA_ENDRET.toString(), utgaaendeRecord.getHendelsesType().toString());
		});
	}

	/**
	 * HVIS man mottar en Insert med JournalStatus = M, skal MidlertidigJournalfoert-hendelse publiseres til utgaaende topic
	 */
	@Test
	public void shouldPublishNewMidlertidigJournalfoertHendelse() throws Exception {
		JsonNode jsonrecord = classpathToJsonNode("__files/midlertidig_jf.json");

		record = new ProducerRecord<>(INN_TOPIC, 0, "key", jsonrecord);
		sendToTopic(record);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			List<ConsumerRecord<String, JournalfoeringHendelseRecord>> records = KafkaTestUtils.getRecords(consumer).records(new TopicPartition(UT_TOPIC, 0));
			assertThat(records, hasSize(1));
			JournalfoeringHendelseRecord utgaaendeRecord = records.get(0).value();
			assertEquals(1L, utgaaendeRecord.getJournalpostId().longValue());
			assertEquals(MIDLERTIDIG_JOURNALFORT.toString(), utgaaendeRecord.getHendelsesType().toString());
		});
	}

	/**
	 * HVIS man mottar en Insert med JournalStatus = J, skal EndeligJournalfoert-hendelse publiseres til utgaaende topic
	 */
	@Test
	public void shouldPublishNewEndligJournalfoertHendelse() throws Exception {
		JsonNode jsonrecord = classpathToJsonNode("__files/endelig_jf.json");

		record = new ProducerRecord<>(INN_TOPIC, 0, "key", jsonrecord);
		sendToTopic(record);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			List<ConsumerRecord<String, JournalfoeringHendelseRecord>> records = KafkaTestUtils.getRecords(consumer).records(new TopicPartition(UT_TOPIC, 0));
			assertThat(records, hasSize(1));
			JournalfoeringHendelseRecord utgaaendeRecord = records.get(0).value();
			assertEquals(1L, utgaaendeRecord.getJournalpostId().longValue());
			assertEquals(ENDELIG_JOURNALFORT.toString(), utgaaendeRecord.getHendelsesType().toString());
		});
	}

	/**
	 * HVIS man mottar en Update med endring av JournalStatus = U, skal JournalpostUtgaatt-hendelse publiseres til utgaaende topic
	 */
	@Test
	public void shouldPublishNewJournalpostUtgaattHendelse() throws Exception {
		JsonNode jsonrecord = classpathToJsonNode("__files/journalpost_utgaatt.json");

		record = new ProducerRecord<>(INN_TOPIC, 0, "key", jsonrecord);
		sendToTopic(record);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			List<ConsumerRecord<String, JournalfoeringHendelseRecord>> records = KafkaTestUtils.getRecords(consumer).records(new TopicPartition(UT_TOPIC, 0));
			assertThat(records, hasSize(1));
			JournalfoeringHendelseRecord utgaaendeRecord = records.get(0).value();
			assertEquals(1L, utgaaendeRecord.getJournalpostId().longValue());
			assertEquals(JOURNALPOST_UTGATT.toString(), utgaaendeRecord.getHendelsesType().toString());
		});
	}

	/**
	 * HVIS man mottar en Insert med K_JOURNALPOST_T = U, skal det _ikke_ publiseres noen hendelse til utgaaende topic
	 */
	@Test
	public void shouldNotPublishUtgaaendeEvent() throws Exception {
		JsonNode jsonrecord = classpathToJsonNode("__files/journalpost_utgaatt.json");

		record = new ProducerRecord<>(INN_TOPIC, 0, "key", jsonrecord);
		sendToTopic(record);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			List<ConsumerRecord<String, JournalfoeringHendelseRecord>> records = KafkaTestUtils.getRecords(consumer, 1_000L).records(new TopicPartition(UT_TOPIC, 0));
			assertThat(records, hasSize(0));
		});
	}

}
