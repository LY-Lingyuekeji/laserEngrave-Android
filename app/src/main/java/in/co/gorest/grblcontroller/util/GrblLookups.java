package in.co.gorest.grblcontroller.util;

import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class GrblLookups {

    private final HashMap<String,String[]> lookups = new HashMap<>();

    public GrblLookups(Context context, String prefix) {
        String filename = prefix + ".csv";

        try {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(filename)))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    lookups.put(parts[0], parts);
                }
            }
        } catch (IOException ex) {
            System.out.println("Unable to load GRBL resources.");
            ex.printStackTrace();
        }
    }

    public String[] lookup(String idx){
        return lookups.get(idx);
    }

}
