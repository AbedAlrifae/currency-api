# Currency API

REST API voor het beheren van crypto-valuta. Sollicitatieopdracht voor PharmaPartners.

Spring Boot 3, Java 21, H2 in-memory database.

## Starten

```bash
./mvnw spring-boot:run
```

Draait op `http://localhost:8080`. De database wordt bij het opstarten gevuld vanuit `data.sql` met BTC, ETH, XRP en BCH, en overleeft een herstart niet.

H2-console op `/h2-console`, JDBC URL `jdbc:h2:mem:currencies`, gebruiker `sa`, geen wachtwoord.

## Endpoints

| Methode | Pad                        | Response                   |
| ------- | -------------------------- | -------------------------- |
| POST    | `/api/currencies`          | 201 met Location-header    |
| GET     | `/api/currencies/{ticker}` | 200, of 404                |
| GET     | `/api/currencies`          | 200, met paging en sorting |
| PUT     | `/api/currencies/{ticker}` | 200, of 404                |
| DELETE  | `/api/currencies/{ticker}` | 204, of 404                |

```bash
curl "http://localhost:8080/api/currencies?size=2&page=0&sort=ticker"

curl -i -X POST http://localhost:8080/api/currencies \
  -H "Content-Type: application/json" \
  -d '{"ticker":"LTC","name":"Litecoin","numberOfCoins":54000000,"marketCap":3200000000}'
```

## Opbouw

`domain` → `repository` → `service` → `controller`, met de afhankelijkheden één kant op. De controller vertaalt alleen HTTP naar service-aanroepen, de logica zit in de service.

## Keuzes

**Ticker als primaire sleutel.** Natuurlijke sleutel, uniek en leesbaar in de URL. Bijlage A heeft geen id-kolom. Prijs: de ticker is niet te wijzigen via een PUT.

**BigDecimal voor market cap.** Het is geld, en `double` geeft afrondingsfouten.

**BigInteger voor het aantal coins.** XRP heeft 38.590.000.000 coins en past niet in een `Integer`.

**Builder op de entity, gekoppeld aan Jackson.** De builder voorkomt dat `ticker` en `name` verwisseld worden, en validatie zit op één plek in `build()`. Omdat de entity daardoor geen setters heeft, loopt deserialisatie via `@JsonDeserialize(builder = ...)`, zodat dezelfde validatie geldt voor binnenkomende JSON.

**Protected no-arg constructor.** Hibernate heeft er een nodig; protected is de strengste stand die werkt, zodat applicatiecode via de builder blijft gaan.

**Paging via Spring Data.** `JpaRepository` levert `findAll(Pageable)`. Het afkappen gebeurt in de database met `LIMIT` en `OFFSET`.

**ResponseStatusException** voor 404 en 409, zonder aparte exception-klassen.

**Logging** met SLF4J naar `logs/application.log`.

## Tests

```bash
./mvnw test
```

`CurrencyServiceTest` test de service met een gemockte repository: ophalen, 404 bij onbekende ticker, en conflict bij een dubbele ticker. Die laatste controleert ook dat er niets wordt opgeslagen.

## Met meer tijd

- De Angular-frontend uit de bonusopdracht. Dat is de stack waar mijn ervaring ligt, maar ik heb voorrang gegeven aan een complete en geteste back-end.
- Een eigen exception met `@RestControllerAdvice` in plaats van `ResponseStatusException`.
- Een controllertest met MockMvc en een `@DataJpaTest` voor paging en sorting.
- Bean Validation, zodat ongeldige invoer een 400 geeft.
- Richting productie: een echte database, authenticatie, en optimistic locking met een `@Version`-veld.
