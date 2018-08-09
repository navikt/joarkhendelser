package no.nav.dokarkivhendelser.consumer.kafka;

import java.nio.ByteBuffer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Component
public class ConsumerRecordToJournalpostEndretConverter {
    public JournalpostEndretEvent convert(ConsumerRecord<?, byte[]> source) {
        ByteBuffer buffer = ByteBuffer.wrap(source.value());

        return JournalpostEndretEvent.builder()
                .journalpostId(123L)
                .innhold(buffer.array())
                .build();
    }

}
