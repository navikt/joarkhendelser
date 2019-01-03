package no.nav.joarkjournalfoeringhendelser.consumer.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerRecordAsJsonConverterTest {

    LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
    LinkedHashMap<Object, Object> before = new LinkedHashMap<>();
    LinkedHashMap<Object, Object> after = new LinkedHashMap<>();

    private Long JOURNALPOST_ID = 123L;
    private String timestamp;

    @Mock
    private ConsumerRecord<?, Map> consumerRecordMock;

    @InjectMocks
    private ConsumerRecordAsJsonConverter converter;

    @Before
    public void before() {
        timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(
                DateTimeFormatter.ISO_INSTANT);
    }

    @After
    public void tearDown() {
    }


    private LinkedHashMap<String, Object> createBeforeValues() {
        LinkedHashMap<String, Object> valuesBefore = new LinkedHashMap<>();

        valuesBefore.put("JOURNALPOST_ID", Math.toIntExact(JOURNALPOST_ID));
        valuesBefore.put("K_JOURNALPOST_T", "I");
        valuesBefore.put("K_FAGOMRADE", "FOR");
        valuesBefore.put("K_JOURNAL_S", "M");

        return valuesBefore;
    }


    private LinkedHashMap<String, Object> createAfterValues() {
        LinkedHashMap<String, Object> valuesAfter = new LinkedHashMap<>();

        valuesAfter.put("JOURNALPOST_ID", Math.toIntExact(JOURNALPOST_ID));
        valuesAfter.put("K_JOURNALPOST_T", "I");
        valuesAfter.put("K_FAGOMRADE", "DAG");
        valuesAfter.put("K_JOURNAL_S", "M");

        return valuesAfter;
    }

    private LinkedHashMap<String, Object> createLongBeforeValues() {
        LinkedHashMap<String, Object> valuesBefore = new LinkedHashMap<>();

        valuesBefore.put("JOURNALPOST_ID", 423262338);
        valuesBefore.put("JOURNALF_ENHET", "0219");
        valuesBefore.put("OPPRETTET_AV_NAVN", "BJOARK002");
        valuesBefore.put("DATO_JOURNAL", "2018-09-24 12:21:57.183000000");
        valuesBefore.put("ANTALL_RETUR", "0");
        valuesBefore.put("DATO_AVS_RETUR", null);
        valuesBefore.put("INNHOLD", "Søknad om barnetrygd");
        valuesBefore.put("KRAV_TYPE", "null");
        valuesBefore.put("MERKNAD", "null");
        valuesBefore.put("FORDELING", "null");
        valuesBefore.put("ORIGINAL_BESTILT", "0");
        valuesBefore.put("ENDRET_AV_NAVN", "F_Z990101 E_Z990101");
        valuesBefore.put("OPPRETTET_AV", "BJOARK002");
        valuesBefore.put("DATO_ENDRET", "2018-09-24 12:21:57.184000000");
        valuesBefore.put("ENDRET_AV", "Z990101");
        valuesBefore.put("DATO_SENDT_PRINT", "null");
        valuesBefore.put("VERSJON", "2");
        valuesBefore.put("K_FAGOMRADE", "BAR");
        valuesBefore.put("K_JOURNAL_S", "J");
        valuesBefore.put("DATO_OPPRETTET", "2018-09-24 12:19:23.280000000");
        valuesBefore.put("DATO_DOKUMENT", "null");
        valuesBefore.put("AVSEND_MOTTAKER", "FORNAVN ETTERNAVN");
        valuesBefore.put("AVSEND_MOTTAK_ID", "***gammelt_fnr***");
        valuesBefore.put("JOURNALFORT_AV_NAVN", "SAKSBEHANDLER NORMANN");
        valuesBefore.put("DATO_MOTTATT", "2018-09-24 00:00:00.000000000");
        valuesBefore.put("LAND", "null");
        valuesBefore.put("K_FAKT_DIS_KANAL", "null");
        valuesBefore.put("ELEKTRONISK_DISTR", "F");
        valuesBefore.put("DATO_EKSPEDERT", "null");
        valuesBefore.put("DATO_LEST", "null");
        valuesBefore.put("MOTTATT_ADRESSAT", "null");
        valuesBefore.put("OPPRETTET_KILDE_NAVN", "AS36");
        valuesBefore.put("K_JOURNALPOST_T", "I");
        valuesBefore.put("K_UTSENDINGS_KANAL", "null");
        valuesBefore.put("K_MOTTAKS_KANAL", "SKAN_NETS");
        valuesBefore.put("ENDRET_KILDE_NAVN", "FS22");
        valuesBefore.put("SIGNATUR", "null");
        valuesBefore.put("KANAL_REFERANSE_ID", "null");

        return valuesBefore;
    }

    private LinkedHashMap<String, Object> createLongAfterValues() {
        LinkedHashMap<String, Object> valuesAfter = createLongBeforeValues();
        valuesAfter.put("K_FAGOMRADE", "FOR");
        return valuesAfter;
    }

    @Test
    public void shouldConvertUpdateOperation() {
        values.clear();
        values.put("op_type", "U");
        values.put("op_ts", timestamp);

        after.put("JOURNALPOST_ID", Math.toIntExact(JOURNALPOST_ID));
        values.put("before", createBeforeValues());
        values.put("after", createAfterValues());

        when(consumerRecordMock.value()).thenReturn(values);
        JournalpostEndretEvent event = converter.convertRecordToEvent(consumerRecordMock);
        assertEquals((Long)123L, event.getJournalpostId());
        assertEquals(1, event.columnsChanged.size());
        assertEquals("U", event.getOperation());
        assertEquals("M", event.getJournalpostStatusAfter());
        assertEquals("FOR", event.getFagomradeBefore());
        assertEquals("DAG", event.getFagomradeAfter());
    }

    @Test
    public void shouldConvertCreateOperation() {
        values.clear();
        values.put("op_type", "I");
        values.put("op_ts", timestamp);

        values.put("after", createAfterValues());

        when(consumerRecordMock.value()).thenReturn(values);
        JournalpostEndretEvent event = converter.convertRecordToEvent(consumerRecordMock);
        assertEquals((Long)123L, event.getJournalpostId());
        assertEquals(4, event.columnsChanged.size());
        assertEquals("I", event.getOperation());
        assertEquals("M", event.getJournalpostStatusAfter());
        assertEquals("DAG", event.getFagomradeAfter());
        assertEquals("", event.getFagomradeBefore());
    }

    @Test
    public void shouldProduceCorrectNumberOfColumnsChanged() {
        values.clear();
        values.put("op_type", "U");

        values.put("before", createBeforeValues());
        values.put("after", createAfterValues());

        when(consumerRecordMock.value()).thenReturn(values);
        JournalpostEndretEvent event = converter.convertRecordToEvent(consumerRecordMock);

        assertEquals(1, event.columnsChanged.size());


    }

    @Test
    public void shouldAlwaysIncludeTemaBeforeAndAfter() {
        values.clear();
        values.put("op_type", "U");

        LinkedHashMap<String, Object> beforeValues = createBeforeValues();
        beforeValues.remove("K_FAGOMRADE");
        beforeValues.put("INNHOLD", "Test");
        values.put("before", beforeValues);
        LinkedHashMap<String, Object> afterValues = createAfterValues();
        afterValues.remove("K_FAGOMRADE");
        afterValues.put("INNHOLD", "Test 2");
        values.put("after", afterValues);

        when(consumerRecordMock.value()).thenReturn(values);
        JournalpostEndretEvent event = converter.convertRecordToEvent(consumerRecordMock);

        assertEquals(1, event.columnsChanged.size());
    }

    @Test
    public void shouldProduceCorrectNumberOfColumnsChangedLong() {
        values.clear();
        values.put("op_type", "U");

        values.put("before", createLongBeforeValues());
        values.put("after", createLongAfterValues());

        when(consumerRecordMock.value()).thenReturn(values);
        JournalpostEndretEvent event = converter.convertRecordToEvent(consumerRecordMock);

        assertEquals(1, event.columnsChanged.size());
    }
}