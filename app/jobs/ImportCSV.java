package jobs;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;

import models.Carte;
import play.Logger;
import play.jobs.Job;
import play.libs.Codec;
import service.Geocoding;
import service.MongoDb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

public class ImportCSV extends Job<Integer> {

    private File   csv;
    private Carte  carte;
    private String geocodingString;

    /**
     * Constructor.
     * 
     * @param csv
     * @param carte
     * @param geocodingString
     */
    public ImportCSV(File csv, Carte carte, String geocodingString) {
        super();
        this.csv = csv;
        this.carte = carte;
        this.geocodingString = geocodingString;
    }

    /**
     * Do Job.
     */
    public Integer doJobWithResult() throws Exception {
        Boolean geocoding = Boolean.FALSE;
        String[] format = new String[0];
        if (geocodingString != null) {
            geocoding = Boolean.TRUE;
            format = geocodingString.split("\\+");
        }
        Integer nbItem = 0;
        Integer autoNum = 0;
        try {
            FileInputStream fstream = new FileInputStream(csv);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            // Read File Line By Line
            String[] labels = br.readLine().split(";");
            while ((strLine = br.readLine()) != null) {
                if (strLine.trim() != "") {
                    // Print the content on the console
                    strLine.replaceAll(";", " ; ");
                    String[] data = strLine.split(";");
                    // try to find the association if t exist
                    if (data[0] != null) {
                        BasicDBObject mongo = new BasicDBObject();
                        DBCollection dataColl = MongoDb.db().getCollection("carte-" + carte.uuid);
                        nbItem++;
                        mongo.put("UUID", Codec.UUID());
                        for (int i = 0; i < data.length; i++) {
                            mongo.put(labels[i], data[i]);
                        }
                        if (geocoding) {
                            String adresse = "";
                            for (int j = 0; j < format.length; j++) {
                                try {
                                    Integer num = Integer.parseInt(format[j].trim());
                                    adresse += " " + data[num - 1];
                                } catch (NumberFormatException ex) {
                                    adresse += " " + format[j];
                                }

                            }
                            Map<String, Float> coord = Geocoding.geolocalize(adresse);
                            if (coord != null && coord.containsKey(Geocoding.LONGITUDE)
                                    && coord.containsKey(Geocoding.LATITUDE)) {
                                mongo.put(Geocoding.LONGITUDE, coord.get(Geocoding.LONGITUDE).toString());
                                mongo.put(Geocoding.LATITUDE, coord.get(Geocoding.LATITUDE).toString());
                            }
                        }
                        dataColl.insert(mongo);
                    }
                }
            }
            // Close the input stream
            in.close();
        } catch (Exception e) {// Catch exception if any
            Logger.error(e, e.getMessage());
            throw new Exception("An error occured when importing the CSV file : " + e.getMessage());
        }
        return nbItem;
    }
}
