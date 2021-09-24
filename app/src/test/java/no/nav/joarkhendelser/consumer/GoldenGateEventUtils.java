package no.nav.joarkhendelser.consumer;

import no.nav.joarkhendelser.consumer.goldengate.GoldenGateColumns;
import no.nav.joarkhendelser.consumer.goldengate.GoldenGateEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GoldenGateEventUtils {

	public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");;

	public static GoldenGateEvent createBasicEvent(String operation) {
		return new GoldenGateEvent(
				operation,
				LocalDateTime.parse("2021-09-21 15:21:53.000000", dateTimeFormatter),
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
