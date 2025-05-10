# Payment 馃捀

## 馃搫 Opis
Ten projekt implementuje algorytm optymalizacji p艂atno艣ci dla supermarketu internetowego. Maj膮c list臋 zam贸wie艅 oraz dost臋pne metody p艂atno艣ci (w tym punkty lojalno艣ciowe i promocyjne zni偶ki kartowe), system okre艣la optymaln膮 strategi臋 p艂atno艣ci, aby zmaksymalizowa膰 ca艂kowity rabat dla klienta, jednocze艣nie zapewniaj膮c pe艂ne op艂acenie wszystkich zam贸wie艅.

Algorytm uwzgl臋dnia r贸偶ne zasady:
* Pe艂na p艂atno艣膰 okre艣lon膮 kart膮 w celu skorzystania z promocji powi膮zanych z kart膮.
* Pe艂na p艂atno艣膰 punktami lojalno艣ciowymi ("PUNKTY") z dedykowan膮 zni偶k膮.
* Cz臋艣ciowa p艂atno艣膰 punktami lojalno艣ciowymi (co najmniej 10% warto艣ci zam贸wienia) w celu uzyskania og贸lnej zni偶ki 10% na zam贸wienie, przy czym pozosta艂a cz臋艣膰 jest p艂acona kart膮.
* Preferencja dla u偶ycia punkt贸w lojalno艣ciowych, je艣li nie zmniejsza to nale偶nego rabatu.

## 鉁?G艂贸wne Funkcjonalno艣ci
* Parsowanie zam贸wie艅 i metod p艂atno艣ci z plik贸w JSON.
* Implementacja wieloetapowego algorytmu zach艂annego do przypisywania p艂atno艣ci.
* Priorytetyzacja p艂atno艣ci przynosz膮cych wy偶sze rabaty.
* Obs艂uga limit贸w na metodach p艂atno艣ci.
* Generowanie podsumowania kwot wydanych na poszczeg贸lne metody p艂atno艣ci.

## 馃洜锔?U偶yte Technologie
* **Java 21** 
* **Jackson Databind** 
* **Apache Maven** lub **Gradle** 
* **JUnit 5**

## 鈿欙笍 Wymagania Wst臋pne
* Java Development Kit (JDK) w wersji 21.
* Apache Maven lub Gradle.

## 馃殌 Budowanie Projektu

Projekt nale偶y spakowa膰 do pliku "fat-jar" (pliku JAR zawieraj膮cego wszystkie zale偶no艣ci).

### U偶ycie Apache Maven
1.  Upewnij si臋, 偶e masz zainstalowany Apache Maven.
2.  Przejd藕 do g艂贸wnego katalogu projektu (tam, gdzie znajduje si臋 plik `pom.xml`).
3.  Uruchom nast臋puj膮c膮 komend臋, aby zbudowa膰 fat-jar:
    ```bash
    mvn clean package
    ```
4.  Fat-jar zostanie utworzony w katalogu `target/` (np. `payment-optimizer-1.0-SNAPSHOT.jar`).

### U偶ycie Gradle
1.  Upewnij si臋, 偶e masz zainstalowany Gradle (lub u偶yj Gradle Wrapper `./gradlew`).
2.  Przejd藕 do g艂贸wnego katalogu projektu (tam, gdzie znajduje si臋 plik `build.gradle`).
3.  Uruchom nast臋puj膮c膮 komend臋, aby zbudowa膰 fat-jar:
    ```bash
    ./gradlew shadowJar
    ```
    (Na Windows u偶yj `gradlew.bat shadowJar`)
4.  Fat-jar zostanie utworzony w katalogu `build/libs/` (np. `payment-optimizer-1.0-SNAPSHOT-all.jar` lub `payment-optimizer-1.0-SNAPSHOT-shadow.jar`).

## 喽?Uruchamianie Aplikacji
Po utworzeniu fat-jara mo偶esz uruchomi膰 aplikacj臋 z wiersza polece艅:

```bash
java -jar <Payment\out\artifacts\Payment_jar\Payment.jar> <\Payment\src\main\resources\orders.json> <\Payment\src\main\resources\paymentmethods.json>