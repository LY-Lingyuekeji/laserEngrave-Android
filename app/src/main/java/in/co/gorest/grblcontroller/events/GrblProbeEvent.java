package in.co.gorest.grblcontroller.events;


import in.co.gorest.grblcontroller.model.Position;

public class GrblProbeEvent {

    private final String probeString;
    private Position probePosition;
    private Boolean isProbeSuccess = false;

    public GrblProbeEvent(String probeString){
        this.probeString = probeString;
        this.parseProbeString();
    }

    private void parseProbeString(){
        String[] parts = probeString.split(":");
        String[] coordinates = parts[0].split(",");

        this.probePosition = new Position(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]), Double.parseDouble(coordinates[2]));
        this.isProbeSuccess = parts[1].equals("1");
    }

    public Boolean getIsProbeSuccess(){ return this.isProbeSuccess; }

    public Double getProbeCordX(){ return this.probePosition.getCordX(); }

    public Double getProbeCordY(){ return this.probePosition.getCordY(); }

    public Double getProbeCordZ(){ return this.probePosition.getCordZ(); }

    public Position getProbePosition(){ return this.probePosition; }

}
