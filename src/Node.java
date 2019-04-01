public class Node {
    int day,hour;
    double preciseValue;

    public Node(int day, int hour, double preciseValue) {  //Node tworzy obiekty odpowiadajace danym dla nateżenia kazdego dnia roku co godzinę
        this.day = day;
        this.hour = hour;
        this.preciseValue = preciseValue;


    }
    @Override
    public String toString() {
        return "Node{" +
                "day=" + day +
                ", hour=" + hour +
                ", preciseValue=" + preciseValue +
                '}';
    }
}
