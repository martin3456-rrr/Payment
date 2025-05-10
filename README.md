# Payment 

## Opis
Projekt implementuje algorytm optymalizacji płatności dla supermarketu internetowego. Mając listę zamówień oraz dostępne metody płatności (w tym punkty lojalnościowe i promocyjne zniżki kartowe), system określa optymalną strategię płatności, aby:
* zmaksymalizować całkowity rabat dla klienta,
* zapewnić pełne opłacenie wszystkich zamówień.

Algorytm uwzględnia różne zasady:
* Pełna płatność określoną kartą w celu skorzystania z powiązanej promocji..
* Pełna płatność punktami lojalnościowymi ("PUNKTY") z dedykowaną zniżką..
* Częściowa płatność punktami lojalnościowymi (min. 10% wartości zamówienia) w celu uzyskania ogólnej zniżki 10% na całe zamówienie; reszta płatna kartą.
* Preferencja dla użycia punktów lojalnościowych, o ile nie zmniejsza to przysługującego rabatu.

## Główne funkcjonalności
* Parsowanie zamówień i metod płatności z plików JSON.
* Wieloetapowy algorytm zachłanny do przypisywania płatności.
* Priorytetyzacja płatności przynoszących wyższe rabaty.
* Obsługa limitów przypisanych do metod płatności.
* Generowanie podsumowania kwot wydanych na poszczególne metody płatności..

## Użyte technologie
* **Java 21** 
* **Jackson Databind** 
* **Apache Maven** lub **Gradle** 
* **JUnit 5**

## Wymagania Wstępne
* Java Development Kit (JDK) w wersji 21.
* Apache Maven lub Gradle.

## Budowanie Projektu

Projekt należy spakować do tzw. fat-jar (plik JAR zawierający wszystkie zależności).

### Użycie Apache Maven
1.  Upewnij się, że masz zainstalowany Apache Maven.
2.  Przejdź do katalogu głównego projektu (tam, gdzie znajduje się pom.xml)..
3.  Uruchom następującą komendę, aby zbudował fat-jar:
    ```bash
    mvn clean package
    ```
4.  Fat-jar zostanie utworzony w katalogu `target/` (np. `payment-optimizer-1.0-SNAPSHOT.jar`).

### Użycie Gradle
1.  Upewnij się, że masz zainstalowany Gradle (lub użyj Gradle Wrapper `./gradlew`).
2.  Przejdź do głównego katalogu projektu (tam, gdzie znajduje się plik `build.gradle`).
3.  Uruchom nastąpującą komendę, aby zbudował fat-jar:
    ```bash
    ./gradlew shadowJar
    ```
    (Na Windows użyj `gradlew.bat shadowJar`)
4.  Fat-jar zostanie utworzony w katalogu `build/libs/` (np. `payment-optimizer-1.0-SNAPSHOT-all.jar` lub `payment-optimizer-1.0-SNAPSHOT-shadow.jar`).

## Uruchamianie Aplikacji
Po utworzeniu fat-jara możesz uruchomić aplikację z wiersza poleceń:

```bash
java -jar <Payment\out\artifacts\Payment_jar\Payment.jar> <\Payment\src\main\resources\orders.json> <\Payment\src\main\resources\paymentmethods.json>