package entity;

public class TrainInfo {
    private final String code;
    private final String depart;
    private final String arrive;
    private final String route;
    private final int carCount;

    public TrainInfo(String code, String depart, String arrive, String route, int carCount) {
        this.code = code;
        this.depart = depart;
        this.arrive = arrive;
        this.route = route;
        this.carCount = carCount;
    }

    public String getCode() {
        return code;
    }

    public String getDepart() {
        return depart;
    }

    public String getArrive() {
        return arrive;
    }

    public String getRoute() {
        return route;
    }

    public int getCarCount() {
        return carCount;
    }
}