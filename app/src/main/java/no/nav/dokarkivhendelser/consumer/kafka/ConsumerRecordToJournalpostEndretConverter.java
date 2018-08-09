package no.nav.dokarkivhendelser.consumer.kafka;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.stereotype.Component;

@Component
public class ConsumerRecordToJournalpostEndretConverter {
    public JournalpostEndretEvent convert(ConsumerRecord<?, byte[]> source) {
        ByteBuffer buffer = ByteBuffer.wrap(source.value());
        Headers headers = source.headers();
        String journalpostId = new String(headers.lastHeader("journalpostId").value(), StandardCharsets.UTF_8);

        return JournalpostEndretEvent.builder()
                .journalpostId(Long.parseLong(journalpostId))
                .innhold(buffer.array())
                .build();
    }

}
