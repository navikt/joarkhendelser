package no.nav.dokarkivhendelser.consumer.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalpostEndretEvent {
    Long journalpostId;
    byte[] innhold;
    /**
     * Fylles inn når vi ser hva som kommer fra Golden Gate
     */
}
