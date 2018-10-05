package no.nav.joarkinngaaendehendelser.producer;

import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Martin Burheim Tingstad, Visma Consulting.
 */
@Slf4j
@Component
public class InngaaendeHendelsePublisher {

    @Autowired
    private KafkaTemplate<String, InngaaendeHendelseRecord> kafkaTemplate;

    @Value("${inngaaendeJournalpostEndret.topic}")
    private String topic;

    /*
    private String SCHEMA = "{\n"
            +"  \"namespace\" : \"JOARK\",\n"
            +"  \"type\" : \"InngaaendeHendelse\",\n"
            +"  \"name\" : \"InngaaendeHendelse\",\n"
            +"  \"fields\" : [\n"
            +"    {\"name\": \"hendelsesId\", \"type\": \"string\"},\n"
            +"    {\"name\": \"timestamp\", \"type\": \"long\"},\n"
            +"    {\"name\": \"versjon\", \"type\": \"integer\"},\n"
            +"    {\"name\": \"hendelsesType\", \"type\": \"string\"},\n"
            +"    {\"name\": \"journalpostId\", \"type\": \"long\"},\n"
            +"    {\"name\": \"journalpostStatus\", \"type\": \"string\"},\n"
            +"    {\"name\": \"temaGammelt\", \"type\": \"string\"},\n"
            +"    {\"name\": \"temaNytt\", \"type\": \"string\"},\n"
            +"    {\"name\": \"journalpostType\", \"type\": \"string\"},\n"
            +"    {\"name\": \"mottaksKanal\", \"type\": \"string\"},\n"
            +"    {\"name\": \"kanalReferanseId\", \"type\": \"string\"}\n"
            +"  ]\n"
            +"}";
            */

    public void publish(InngaaendeHendelse hendelse) {
        InngaaendeHendelseRecord record = new InngaaendeHendelseRecord(
                hendelse.getHendelsesId(),
                hendelse.getTimestamp(),
                hendelse.getVersjon(),
                hendelse.getHendelsesType(),
                hendelse.getJournalpostId(),
                hendelse.getJournalpostStatus(),
                hendelse.getTemaGammelt(),
                hendelse.getTemaNytt(),
                hendelse.getJournalpostType(),
                hendelse.getMottaksKanal(),
                hendelse.getKanalReferanseId()
        );

                /*
        org.apache.avro.Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(SCHEMA);
        GenericRecord record = new GenericData.Record(schema);

        record.put("hendelsesId", hendelse.getHendelsesId());
        record.put("timestamp", hendelse.getTimestamp());
        record.put("versjon", hendelse.getVersjon());
        record.put("hendelsesType", hendelse.getHendelsesType());
        record.put("journalpostId", hendelse.getJournalpostId());
        record.put("journalpostStatus", hendelse.getJournalpostStatus());
        record.put("temaGammelt", hendelse.getTemaGammelt());
        record.put("temaNytt", hendelse.getTemaNytt());
        record.put("journalpostType", hendelse.getJournalpostType());
        record.put("mottaksKanal", hendelse.getMottaksKanal());
        record.put("kanalReferanseId", hendelse.getKanalReferanseId());
        */

        ProducerRecord<String, InngaaendeHendelseRecord> producerRecord = new ProducerRecord<>(
            topic,
            null,
            hendelse.getTimestamp(),
            hendelse.getJournalpostId().toString(),
            record);

        try {
            kafkaTemplate.send(producerRecord).get();
        }
        catch (InterruptedException | ExecutionException e) {
            log.warn("Failed to send message to kafka. Topic: " + topic, e.getMessage());
        }

    }

}
