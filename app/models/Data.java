package models;

import java.util.ArrayList;
import java.util.List;

public class Data {

    public String              uuid;
    public final static String UUID = "UUID";

    public Float               longitude;
    public Float               latitude;
    public String              name;
    public String              text;

    public List<String>        data = new ArrayList<String>();
}
