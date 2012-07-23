package service;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import play.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Geocoding {

    private final static String GEO_REVERSE_URL = "http://maps.google.com/maps/geo?output=json";
    private final static String GEO_REVERSE_KEY = "aaa";
    public final static String  LONGITUDE       = "LONGITUDE";
    public final static String  LATITUDE        = "LATITUDE";

    public static Map<String, Float> geolocalize(String adresse) {
        HashMap<String, Float> coord = new HashMap<String, Float>();
        Boolean isOk = Boolean.FALSE;
        try {
            while (!isOk) {
                isOk = Boolean.TRUE;
                URL url = new URL(GEO_REVERSE_URL + "&q=" + URLEncoder.encode(adresse, "UTF-8") + "&key="
                        + GEO_REVERSE_KEY);
                URLConnection conn = url.openConnection();
                ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
                IOUtils.copy(conn.getInputStream(), output);
                output.close();
                JsonParser parser = new JsonParser();
                JsonObject object = (JsonObject) parser.parse(output.toString());
                JsonElement placemark = object.get("Placemark");
                if (placemark != null) {
                    String text = placemark.toString();
                    String result[] = text.replaceAll("(.*)\"coordinates\":\\[(.*)\\]\\}\\}\\]", "$2").split(",");
                    if (result.length >= 2 & result[0] != null && result[1] != null) {
                        coord.put(LONGITUDE, Float.valueOf(result[0]));
                        coord.put(LATITUDE, Float.valueOf(result[1]));
                    }
                    else {
                        Logger.error("Error of geolocalisation  (" + url.toString() + "): " + output.toString());
                        if (output.toString().contains("\"code\": 620,")) {
                            isOk = Boolean.FALSE;
                            Thread.sleep(1000);
                        }
                    }
                }
                else {
                    Logger.error("Error of geolocalisation  (" + url.toString() + "): " + output.toString());
                    if (output.toString().contains("\"code\": 620,")) {
                        isOk = Boolean.FALSE;
                        Thread.sleep(1000);
                    }
                }
            }
        } catch (Exception e) {
            Logger.error(e, e.getMessage());
            return null;
        }
        return coord;
    }

}
