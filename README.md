joarkhendelser
==========
# Funksjonalitet
Joarkhendelser lar fagsystemene få informasjon når det skjer endringer på inngående journalposter på tema/fagområder som de er interessert i, uavhengig av hvem som har gjort endringen.

Tilgangsstyring må kunne begrense hvilke produsenter som kan skrive hendelser til topic.

For mer informasjon: [confluence](https://confluence.adeo.no/display/BOA/joarkhendelser)

# Deploy
Distribusjon av tjenesten er gjort med integrasjon mot Jenkins:
[joarkhendelser CI / CD](https://dok-jenkins.adeo.no/job/joarkhendelser/job/master/)

Push/merge til master branch vil teste, bygge og deploye til produksjonsmiljø og testmiljø.

# Utvikling
## Forutsetninger
* Java 17
* Kubectl
* Maven


## Kjøre Prosjekt
Lokal utvikling er satt opp slik at applikasjonen kjøres lokalt på VDI. 
For å kjøre opp applikasjonen lokalt, bruk systemvariabler hentet fra [Vault](https://vault.adeo.no/ui/vault/secrets/secret/show/dokument/joarkhendelser). Hvis du ikke har den nyeste truststore filen spør på slack kanalen Anakonda.


# Drift og støtte
## Logging
Loggene til tjenesten kan leses på to måter:

### Kibana
For [dev-fss](https://logs.adeo.no/goto/2025ed36ac97de78e0a9880b19865ff2)

For [prod-fss](https://logs.adeo.no/goto/ca96f2932f187020cc9089559b1b739e)

### Kubectl
Merk at det eksisterer to versjoner av appen i dev-fss: joarkhendelser og joarkhendelser-q1, 
med integrasjoner mot henholdsvis q2 og q1.

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
* [\#Team Dokumentløsninger](https://nav-it.slack.com/client/T5LNAMWNA/C6W9E5GPJ)