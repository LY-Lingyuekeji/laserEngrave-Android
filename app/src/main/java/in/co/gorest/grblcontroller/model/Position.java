package in.co.gorest.grblcontroller.model;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class Position {
    private final Double cordX;
    private final Double cordY;
    private final Double cordZ;


    private static final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
    private static final DecimalFormat decimalFormat = (DecimalFormat) numberFormat;

    public Position(double x, double y, double z){
        this.cordX = x; this.cordY = y; this.cordZ = z;
        decimalFormat.applyPattern("#0.###");
    }

    public Double getCordX(){ return this.cordX; }
    public Double getCordY(){ return this.cordY; }
    public Double getCordZ(){ return this.cordZ; }

    private Double roundDouble(Double value){
        String s = decimalFormat.format(value);
        return Double.parseDouble(s);
    }

    public boolean hasChanged(Position position){
        return (position.getCordX().compareTo(this.cordX) != 0) || (position.getCordY().compareTo(this.cordY) != 0) || (position.getCordZ().compareTo(this.cordZ) != 0);
    }

    public boolean atZero(){
        return Math.abs(this.cordX) == 0 && Math.abs(this.cordY) == 0 && Math.abs(this.cordZ) == 0;
    }

}
