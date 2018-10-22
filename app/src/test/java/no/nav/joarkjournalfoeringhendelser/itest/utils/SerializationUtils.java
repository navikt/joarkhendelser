package no.nav.joarkjournalfoeringhendelser.itest.utils;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;

class SerializationUtils {
	static final SchemaRegistryClient REGISTRY = new MockSchemaRegistryClient();

	private SerializationUtils() {
	}
}
