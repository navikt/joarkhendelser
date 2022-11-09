# Joarkhendelser
Joarkhendelser gir andre fagsystem beskjed når det skjer endringer (insert eller update) på en inngående journalpost,
uavhengig av hvem som har gjort endringen. Ved å filtrere på tema kan ulike fagsystem enkelt få beskjed om oppdateringar som gjelder dem.

Hendelsene blir publiserte til følgende Kafka-topic:
- teamdokumenthandtering.aapen-dok-journalfoering i prod-gcp og dev-gcp (Joark i q2)
- teamdokumenthandtering.aapen-dok-journalfoering-q1 i dev-gcp (Joark i q1)

For informasjon om hvordan du kan abonnere på hendelsene kan du sjekke ut [Korleis abonnere på hendingar frå Joark](https://confluence.adeo.no/pages/viewpage.action?pageId=432217859).

Merk at det eksisterer to versjoner av appen i teamdokumenthandtering-namespacet i dev-fss:
- joarkhendelser (GoldenGate-replikering fra Joark-databasen i q2)
- joarkhendelser-q1 (GoldenGate-replikering fra Joark-databasen i q1)

### Henvendelser
Spørsmål om koden eller prosjektet kan rettes til [Slack-kanalen for \#Team Dokumentløsninger](https://nav-it.slack.com/archives/C6W9E5GPJ)
