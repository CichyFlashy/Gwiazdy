import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

class Gwiazda {
    private static final String[] GRECKI_ALFABET = {
            "alfa", "beta", "gamma", "delta", "epsilon", "zeta", "eta", "theta",
            "jota", "kappa", "lambda", "mi", "ni", "ksi", "omikron", "pi",
            "ro", "sigma", "tau", "ipsylon", "fi", "chi", "psi", "omega"
    };

    private static final Map<String, List<Gwiazda>> bazaGwiazdozbiorow = new HashMap<>();

    private String nazwa;
    private String nazwaKatalogowa;
    private String deklinacja;
    private String rektascensja;
    private double obserwowanaWielkoscGwiazdowa;
    private double absolutnaWielkoscGwiazdowa;
    private double odlegloscLatSwietlnych;
    private String gwiazdozbior;
    private String polkula;
    private double temperatura;
    private double masa;

    public Gwiazda(String nazwa, String deklinacja, String rektascensja, double obserwowanaWielkoscGwiazdowa,
                   double odlegloscLatSwietlnych, String gwiazdozbior, String polkula,
                   double temperatura, double masa) {

        if (!nazwa.matches("[A-Z]{3}[0-9]{4}")) {
            throw new IllegalArgumentException("Nazwa musi składać się z 3 dużych liter i 4 cyfr.");
        }

        if (!deklinacja.matches("-?\\d{1,2} stopni \\d{1,2} minut \\d{1,2}(\\.\\d+)? sekund")) {
            throw new IllegalArgumentException("Deklinacja musi być w formacie: xx stopni yy minut zz.zz sekund.");
        }

        if (!rektascensja.matches("\\d{1,2} h \\d{1,2} m \\d{1,2} s")) {
            throw new IllegalArgumentException("Rektascensja musi być w formacie: xx h yy m zz s.");
        }

        if (obserwowanaWielkoscGwiazdowa < -26.74 || obserwowanaWielkoscGwiazdowa > 15.00) {
            throw new IllegalArgumentException("Obserwowana wielkość gwiazdowa musi być w zakresie od -26.74 do 15.00.");
        }

        if (!polkula.equals("PN") && !polkula.equals("PD")) {
            throw new IllegalArgumentException("Półkula musi być PN (północna) lub PD (południowa).");
        }

        if (temperatura < 2000) {
            throw new IllegalArgumentException("Temperatura musi być większa lub równa 2000 stopni Celsjusza.");
        }

        if (masa < 0.1 || masa > 50) {
            throw new IllegalArgumentException("Masa musi być w zakresie od 0.1 do 50 mas Słońca.");
        }

        this.nazwa = nazwa;
        this.deklinacja = deklinacja;
        this.rektascensja = rektascensja;
        this.obserwowanaWielkoscGwiazdowa = obserwowanaWielkoscGwiazdowa;
        this.odlegloscLatSwietlnych = odlegloscLatSwietlnych;
        this.gwiazdozbior = gwiazdozbior;
        this.polkula = polkula;
        this.temperatura = temperatura;
        this.masa = masa;
        this.absolutnaWielkoscGwiazdowa = obliczAbsolutnaWielkoscGwiazdowa(obserwowanaWielkoscGwiazdowa, odlegloscLatSwietlnych / 3.26);

        dodajDoBazy();
    }

    private double obliczAbsolutnaWielkoscGwiazdowa(double obserwowanaWielkosc, double odlegloscParseki) {
        return obserwowanaWielkosc - 5 * Math.log10(odlegloscParseki) + 5;
    }

    private void dodajDoBazy() {
        List<Gwiazda> gwiazdy = bazaGwiazdozbiorow.computeIfAbsent(this.gwiazdozbior, k -> new ArrayList<>());
        this.nazwaKatalogowa = GRECKI_ALFABET[gwiazdy.size()] + " " + this.gwiazdozbior;
        gwiazdy.add(this);
    }

    public static void usunGwiazde(String nazwaKatalogowa) {
        for (List<Gwiazda> gwiazdy : bazaGwiazdozbiorow.values()) {
            gwiazdy.removeIf(gwiazda -> gwiazda.nazwaKatalogowa.equals(nazwaKatalogowa));
        }

        for (Map.Entry<String, List<Gwiazda>> entry : bazaGwiazdozbiorow.entrySet()) {
            List<Gwiazda> gwiazdy = entry.getValue();
            for (int i = 0; i < gwiazdy.size(); i++) {
                gwiazdy.get(i).nazwaKatalogowa = GRECKI_ALFABET[i] + " " + entry.getKey();
            }
        }
    }

    public static List<Gwiazda> wyszukajGwiazdyWGwiazdozbiorze(String gwiazdozbior) {
        return bazaGwiazdozbiorow.getOrDefault(gwiazdozbior, Collections.emptyList());
    }

    public static List<Gwiazda> wyszukajGwiazdyWOdleglosci(double parseki) {
        double lataSwietlne = parseki * 3.26;
        return bazaGwiazdozbiorow.values().stream()
                .flatMap(Collection::stream)
                .filter(gwiazda -> gwiazda.odlegloscLatSwietlnych <= lataSwietlne)
                .collect(Collectors.toList());
    }

    public static List<Gwiazda> wyszukajGwiazdyWPrzedzialeTemperatur(double minTemp, double maxTemp) {
        return bazaGwiazdozbiorow.values().stream()
                .flatMap(Collection::stream)
                .filter(gwiazda -> gwiazda.temperatura >= minTemp && gwiazda.temperatura <= maxTemp)
                .collect(Collectors.toList());
    }

    public static List<Gwiazda> wyszukajGwiazdyWPrzedzialeMagnitudo(double minMag, double maxMag) {
        return bazaGwiazdozbiorow.values().stream()
                .flatMap(Collection::stream)
                .filter(gwiazda -> gwiazda.obserwowanaWielkoscGwiazdowa >= minMag && gwiazda.obserwowanaWielkoscGwiazdowa <= maxMag)
                .collect(Collectors.toList());
    }

    public static List<Gwiazda> wyszukajGwiazdyNaPolkuli(String polkula) {
        return bazaGwiazdozbiorow.values().stream()
                .flatMap(Collection::stream)
                .filter(gwiazda -> gwiazda.polkula.equals(polkula))
                .collect(Collectors.toList());
    }

    public static List<Gwiazda> wyszukajSupernowe() {
        return bazaGwiazdozbiorow.values().stream()
                .flatMap(Collection::stream)
                .filter(gwiazda -> gwiazda.masa > 1.44)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Gwiazda{" +
                "nazwa='" + nazwa + '\'' +
                ", nazwaKatalogowa='" + nazwaKatalogowa + '\'' +
                ", deklinacja='" + deklinacja + '\'' +
                ", rektascensja='" + rektascensja + '\'' +
                ", obserwowanaWielkoscGwiazdowa=" + obserwowanaWielkoscGwiazdowa +
                ", absolutnaWielkoscGwiazdowa=" + absolutnaWielkoscGwiazdowa +
                ", odlegloscLatSwietlnych=" + odlegloscLatSwietlnych +
                ", gwiazdozbiór=" + gwiazdozbior +
                ", półkula=" + polkula +
                ", temperatura=" + temperatura +
                ", masa=" + masa + "}";
    }
}
public class Main {
    public static void main(String[] args) {

    }
}