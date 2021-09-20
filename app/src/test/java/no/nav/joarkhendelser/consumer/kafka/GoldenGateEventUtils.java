package no.nav.joarkhendelser.consumer.kafka;

import no.nav.joarkhendelser.consumer.kafka.goldengate.GoldenGateEvent;
import no.nav.joarkhendelser.consumer.kafka.goldengate.GoldenGateColumns;

public class GoldenGateEventUtils {

	public static GoldenGateEvent createBasicEvent(String operation) {
		return new GoldenGateEvent(
				"JOARK.T_JOURNALPOST",
				operation,
				"2021-09-21 15:21:53.000000",
				"2021-09-21T15:21:58.162000",
				"00000000150000011839",
				null,
				null
		);
	}

	public static GoldenGateColumns createBasicColumns() {
		return new GoldenGateColumns(
				453655940,
				"MED",
				"M",
				"I",
				"EESSI",
				"1395459_dc20eeeae6d54a3a8957f8e82d99041c_1",
				"ab0269"
		);
	}

	public static GoldenGateColumns createBasicColumnsWithTema(String tema) {
		return new GoldenGateColumns(
				453655940,
				tema,
				"M",
				"I",
				"EESSI",
				"1395459_dc20eeeae6d54a3a8957f8e82d99041c_1",
				"ab0269"
		);
	}
}
