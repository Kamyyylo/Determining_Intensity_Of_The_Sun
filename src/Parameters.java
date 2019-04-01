import java.util.ArrayList;

public class Parameters {   //Parametr bedzie obiektem mającym 6 parametrow potrzebnych
    // do podstawienia do wzoru, difference jest to sum a kwadratór wszystkich błędow dla danych wczytanych z pliku
    ArrayList<Double> param;
    double difference;

    public Parameters(ArrayList<Double> param,double difference) {
        this.param = param;
        this.difference = difference;
    }

    public void setDifference(double difference) {
        this.difference = difference;
    }

    @Override
    public String toString() {
        return "Parameters{" +
                "param=" + param +
                ", difference=" + difference +
                '}';
    }
}
