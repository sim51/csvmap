package service;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import play.Logger;

public class Geocoding {

    private final static String  GEO_REVERSE_URL = "http://nominatim.openstreetmap.org/search?";
    private final static String  GEO_REVERSE_KEY = "contact@bsimard.com";
    private final static Charset charset         = Charset.defaultCharset();
    public final static String   LONGITUDE       = "LONGITUDE";
    public final static String   LATITUDE        = "LATITUDE";

    public static Map<String, Float> geolocalize(String adresse) {
        HashMap<String, Float> coord = new HashMap<String, Float>();
        Boolean isOk = Boolean.FALSE;
        try {
            while (!isOk) {
                isOk = Boolean.TRUE;
                URL url = new URL(GEO_REVERSE_URL + "q=" + URLEncoder.encode(adresse, "UTF-8")
                        + "&format=xml&polygon=0&email=" + GEO_REVERSE_KEY);
                Logger.debug("Url is " + url.toString());
                URLConnection conn = url.openConnection();

                ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
                IOUtils.copy(conn.getInputStream(), output);
                output.close();

                String result[] = output.toString().replaceAll("\\n", "")
                        .replaceFirst("(.*) lat='([-]?[0-9]*.[0-9]*)' lon='([-]?[0-9]*.[0-9]*)'(.*)", "$2,$3")
                        .split(",");
                if (result.length >= 2 & result[0] != null && result[1] != null) {
                    try {
                        coord.put(LATITUDE, Float.valueOf(result[0]));
                        coord.put(LONGITUDE, Float.valueOf(result[1]));
                    } catch (NumberFormatException e) {
                        Logger.debug("Can't find geolocalisation !!!");
                    }
                }

            }
        } catch (Exception e) {
            Logger.error(e, e.getMessage());
        }
        return coord;
    }

}
