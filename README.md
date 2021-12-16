# Joarkhendelser

## Funksjonalitet
Joarkhendelser gir andre fagsystem beskjed når det skjer endringer (insert eller update) på en inngående journalpost,
uavhengig av hvem som har gjort endringen. Ved å filtrere på tema kan ulike fagsystem enkelt få beskjed om oppdateringar som gjelder dem.

For informasjon om hvordan du kan abonnere på eventene kan du sjekke ut [Korleis abonnere på hendingar frå Joark](https://confluence.adeo.no/pages/viewpage.action?pageId=432217859).

Merk at det eksisterer to versjoner av appen i teamdokumenthandtering-namespacet i dev-fss:
- joarkhendelser (GoldenGate-replikering fra Joark-databasen i q2)
- joarkhendelser-q1 (GoldenGate-replikering fra Joark-databasen i q1)

## For intern utvikling
### Teknologier
* Spring Boot-app med Java 17
* Maven
* Kafka

### Deploy
Deploy av app og CI/CD skjer på [Jenkins](https://dok-jenkins.adeo.no/job/joarkhendelser/job/master/).
* Push til feature branch vil deploye til testmiljø i dev-fss.
* Merge av PR eller push til master-branch vil deploye til prod-fss.

### Kjøre opp lokalt
Applikasjonen kjøres lokalt på VDI.

### Bruk av Kubectl
For dev-fss:
```
kubectl config use-context dev-fss
kubectl get pods -n teamdokumenthandtering | grep joarkhendelser
kubectl logs -f joarkhendelser-<POD-ID> -n teamdokumenthandtering
```

For prod-fss:
```
kubectl config use-context prod-fss
kubectl get pods -n teamdokumenthandtering | grep joarkhendelser
kubectl logs -f joarkhendelser-<POD-ID> -n teamdokumenthandtering
```

## Henvendelser
Spørsmål om koden eller prosjektet kan rettes til Team Dokumentløsninger på:
* [\#Team Dokumentløsninger](https://nav-it.slack.com/archives/C6W9E5GPJ)