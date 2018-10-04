package no.nav.joarkinngaaendehendelser.producer;

import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.INNGAAENDE;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.JOURNALFORT;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.MIDLERTIDIG;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.MOTTATT;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.UKJENTBRUKER;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostStatus.UTGAR;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.OracleSchema.INSERT_OPERATION;
import static no.nav.joarkinngaaendehendelser.consumer.kafka.OracleSchema.UPDATE_OPERATION;

import java.util.UUID;
import org.apache.commons.lang.StringUtils;

import no.nav.joarkinngaaendehendelser.consumer.kafka.JournalpostEndretEvent;

/**
 * @author Martin Burheim Tingstad, Visma Consulting.
 */
public class JournalpostEndretInngaaendeHendelseMapper {
    public static InngaaendeHendelse map(JournalpostEndretEvent event) {
        InngaaendeHendelsesType inngaaendeHendelsesType = finnHendelsesType(event);

        return inngaaendeHendelsesType != null ?
                InngaaendeHendelse.builder()
                        .hendelsesId(UUID.randomUUID().toString())
                        .versjon(1)
                        .temaNytt(event.getFagomradeAfter())
                        .temaGammelt(event.getFagomradeBefore())
                        .journalpostId(event.getJournalpostId())
                        .kanalReferanseId(event.getKanalReferanseId())
                        .mottaksKanal(event.getMottaksKanal())
                        .journalpostStatus(event.getJournalpostStatusAfter())
                        .journalpostType(event.getJournalpostType())
                        .hendelsesType(inngaaendeHendelsesType.toString())
                        .timestamp(event.getTimestamp())
                        .build() : null;
    }

    private static InngaaendeHendelsesType finnHendelsesType(JournalpostEndretEvent event) {
        if(INSERT_OPERATION.equalsIgnoreCase(event.getOperation()) &&
                INNGAAENDE.equalsIgnoreCase(event.getJournalpostType()) &&
                (MOTTATT.equalsIgnoreCase(event.getJournalpostStatusAfter()) || MIDLERTIDIG.equalsIgnoreCase(event.getJournalpostStatusAfter()))) {
            return InngaaendeHendelsesType.MIDLERTIDIG_JOURNALFORT;
        }
        if(UPDATE_OPERATION.equalsIgnoreCase(event.getOperation()) &&
                INNGAAENDE.equalsIgnoreCase(event.getJournalpostType()) &&
                StringUtils.isNotEmpty(event.getFagomradeBefore()) &&
                StringUtils.isNotEmpty(event.getFagomradeAfter()) &&
                !event.getFagomradeBefore().equalsIgnoreCase(event.getFagomradeAfter())) {
            return InngaaendeHendelsesType.TEMA_ENDRET;
        }
        if(INNGAAENDE.equalsIgnoreCase(event.getJournalpostType()) &&
                MIDLERTIDIG.equalsIgnoreCase(event.getJournalpostStatusBefore()) &&
                JOURNALFORT.equalsIgnoreCase(event.getJournalpostStatusAfter())) {
            return InngaaendeHendelsesType.ENDELIG_JOURNALFORT;
        }
        if(UPDATE_OPERATION.equalsIgnoreCase(event.getOperation()) &&
                INNGAAENDE.equalsIgnoreCase(event.getJournalpostType()) &&
                StringUtils.isNotEmpty(event.getJournalpostStatusBefore()) &&
                (UTGAR.equalsIgnoreCase(event.getJournalpostStatusAfter()) || UKJENTBRUKER.equalsIgnoreCase(event.getJournalpostStatusAfter()))) {
            return InngaaendeHendelsesType.JOURNALPOST_UTGATT;
        }
        return null;
    }
}
