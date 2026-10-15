# Currency API

REST API voor het beheren van crypto-valuta, gebouwd als sollicitatieopdracht voor PharmaPartners.

Spring Boot 3, Java 21, H2 in-memory database.

## Starten

```bash
./mvnw spring-boot:run
```

De applicatie draait op `http://localhost:8080`.

Bij het opstarten wordt de in-memory database gevuld vanuit `src/main/resources/data.sql` met vier valuta: BTC, ETH, XRP en BCH. De data overleeft een herstart niet, zoals de opdracht vraagt.

De database is in te zien via de H2-console op `http://localhost:8080/h2-console`:

| Veld      | Waarde                   |
| --------- | ------------------------ |
| JDBC URL  | `jdbc:h2:mem:currencies` |
| User Name | `sa`                     |
| Password  | leeg laten               |

## Endpoints

Basis-URL: `/api/currencies`

| Methode | Pad                        | Omschrijving                         | Response                                  |
| ------- | -------------------------- | ------------------------------------ | ----------------------------------------- |
| POST    | `/api/currencies`          | Nieuw record aanmaken                | 201 Created met Location-header           |
| GET     | `/api/currencies/{ticker}` | Eén record ophalen                   | 200 OK, of 404 als de ticker niet bestaat |
| GET     | `/api/currencies`          | Lijst ophalen, met paging en sorting | 200 OK                                    |
| PUT     | `/api/currencies/{ticker}` | Record bijwerken                     | 200 OK, of 404                            |
| DELETE  | `/api/currencies/{ticker}` | Record verwijderen                   | 204 No Content, of 404                    |

### Voorbeelden

Alle valuta ophalen:

```bash
curl http://localhost:8080/api/currencies
```

Paging en sorting:

```bash
curl "http://localhost:8080/api/currencies?size=2&page=0&sort=ticker"
```

Eén valuta ophalen:

```bash
curl http://localhost:8080/api/currencies/BTC
```

Nieuwe valuta aanmaken:

```bash
curl -i -X POST http://localhost:8080/api/currencies \
  -H "Content-Type: application/json" \
  -d '{"ticker":"LTC","name":"Litecoin","numberOfCoins":54000000,"marketCap":3200000000}'
```

Verwijderen:

```bash
curl -i -X DELETE http://localhost:8080/api/currencies/LTC
```

## Opbouw

```
com.currency.currency_api
├── domain       Currency (entity met builder)
├── repository   CurrencyRepository
├── service      CurrencyService
└── controller   CurrencyController
```

De lagen hebben een eenrichtingsafhankelijkheid: de controller kent de service, de service kent de repository, en het domain kent geen van beide. De controller bevat geen logica, alleen het vertalen van HTTP naar service-aanroepen en terug.

## Gemaakte keuzes

**Ticker als primaire sleutel.** De ticker is een natuurlijke sleutel: uniek, kort en leesbaar in een URL (`/api/currencies/BTC`). Bijlage A van de opdracht bevat ook geen id-kolom. De prijs die ik hiervoor betaal is dat de ticker niet te wijzigen is via een PUT; dat zou neerkomen op een nieuw record. Met een gegenereerd surrogaat-id was dat wel gekund, ten koste van leesbaarheid.

**BigDecimal voor de market cap.** Market cap is een geldbedrag en `double` geeft bij geldberekeningen afrondingsfouten. `BigDecimal` rekent exact.

**BigInteger voor het aantal coins.** XRP heeft 38.590.000.000 coins. Dat past niet in een `Integer`, die stopt bij 2.147.483.647. Met een `Integer` zou dit record stil corrupt raken.

**Builder design pattern op de entity.** De opdracht vraagt hierom. De meerwaarde zit in twee dingen: de entity heeft vier velden waarvan er twee een `String` zijn, en bij een constructor met vier parameters kun je `ticker` en `name` verwisselen zonder dat de compiler iets merkt. Bij een builder staat de veldnaam naast de waarde. Daarnaast gebeurt de validatie op één plek, in `build()`, waardoor een ongeldige Currency niet kan bestaan.

**De builder gekoppeld aan Jackson.** Omdat de entity geen setters heeft en dus onveranderlijk is, kan Jackson hem niet vullen bij een POST. In plaats van setters toe te voegen en die onveranderlijkheid op te geven, loopt de deserialisatie via `@JsonDeserialize(builder = Currency.Builder.class)` door de builder heen. Daardoor geldt dezelfde validatie voor binnenkomende JSON als voor objecten die in code worden gemaakt.

**Protected no-arg constructor.** Hibernate heeft een parameterloze constructor nodig om entities te instantiëren, en de JPA-specificatie eist dat die public of protected is. Protected is de strengste stand die werkt: Hibernate en zijn lazy-loading proxies kunnen erbij, maar normale applicatiecode gaat via de builder.

**Paging en sorting via Spring Data.** `CurrencyRepository` erft van `JpaRepository`, dat `findAll(Pageable)` meebrengt. De controller neemt een `Pageable` als parameter, die Spring uit de query string vult. Het afkappen gebeurt in de database via `LIMIT` en `OFFSET`, niet in het geheugen.

**ResponseStatusException voor foutcodes.** Hiermee komt een onbekende ticker terug als 404 en een dubbele ticker bij POST als 409, zonder aparte exception-klassen. Netter zou een eigen exception met een `@RestControllerAdvice` zijn; dat staat hieronder bij de verbeterpunten.

**Logging.** Alle calls op de API worden gelogd met SLF4J naar `logs/application.log`, geconfigureerd via `logging.file.name` in `application.properties`.

## Tests

```bash
./mvnw test
```

`CurrencyServiceTest` test de servicelaag in isolatie met een gemockte repository, dus zonder Spring-context. Afgedekt zijn het ophalen van een bestaande valuta, een 404 bij een onbekende ticker, en een conflict bij een dubbele ticker. Die laatste controleert ook dat er in dat geval niets wordt opgeslagen.

`CurrencyApiApplicationTests` controleert dat de Spring-context opstart.

## Met meer tijd

- De bonusopdracht: een Angular-frontend met een tabel waarin records toegevoegd, gewijzigd en verwijderd kunnen worden. Dit is de stack waar mijn dagelijkse ervaring ligt, maar binnen de beschikbare tijd heb ik voorrang gegeven aan een complete en geteste back-end.
- Een eigen `CurrencyNotFoundException` met een `@RestControllerAdvice`, zodat de foutafhandeling niet verspreid door de servicelaag staat en de foutrespons een eigen formaat krijgt.
- Meer testdekking: een controllertest met `@WebMvcTest` en MockMvc voor de statuscodes en de JSON, en een `@DataJpaTest` die paging en sorting op de repository controleert.
- Bean Validation met `@NotBlank` en `@Valid`, zodat ongeldige invoer een 400 geeft in plaats van af te ketsen op de validatie in `build()`.
- Richting productie: een echte database in plaats van H2, authenticatie en autorisatie, en optimistic locking met een `@Version`-veld zodat twee gelijktijdige updates elkaar niet stil overschrijven.

## Bekend aandachtspunt

Bij een PUT komt de ticker zowel uit de URL als uit de request body. Wanneer die twee van elkaar verschillen is het gedrag een bewuste keuze en geen toeval; zie de javadoc bij `CurrencyService.update`.
