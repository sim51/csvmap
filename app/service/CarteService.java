package service;

import java.util.ArrayList;
import java.util.List;

import models.Carte;
import models.User;
import play.libs.Codec;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class CarteService {

    public final static String COLLECTION_CARTE = "carte";

    public static List<Carte> findMapByUser(User user) {
        List<Carte> cartes = new ArrayList<Carte>();

        DBCollection carteColl = MongoDb.db().getCollection(COLLECTION_CARTE);
        // do a query to find the user carte
        BasicDBObject query = new BasicDBObject();
        query.put(Carte.USER_UUID, user.uuid);
        DBCursor cursor = carteColl.find(query);

        while (cursor.hasNext()) {
            Carte carte = mongoToCarte(cursor.next());
            cartes.add(carte);
        }
        return cartes;
    }

    public static Carte findMapByUuid(String uuid) {
        DBCollection carteColl = MongoDb.db().getCollection(COLLECTION_CARTE);
        // do a query to find the user carte
        BasicDBObject query = new BasicDBObject();
        query.put(Carte.UUID, uuid);
        DBCursor cursor = carteColl.find(query);

        if (cursor.hasNext()) {
            return mongoToCarte(cursor.next());
        }
        else {
            return null;
        }
    }

    public static Carte save(Carte carte) {
        DBCollection carteColl = MongoDb.db().getCollection(COLLECTION_CARTE);
        BasicDBObject mongoCarte = new BasicDBObject();
        mongoCarte.put(Carte.DESCRIPTION, carte.description);
        mongoCarte.put(Carte.NAME, carte.name);
        mongoCarte.put(Carte.CENTER_LAT, carte.centerLat);
        mongoCarte.put(Carte.CENTER_LNG, carte.centerLng);
        mongoCarte.put(Carte.CENTER_ZOOM, carte.centerZoom);
        mongoCarte.put(Carte.LEAFT_PILE_ZOOM, carte.leafPileZoom);
        mongoCarte.put(Carte.USER_UUID, carte.user_uuid);
        mongoCarte.put(Carte.PATTERN, carte.pattern);
        if (carte.uuid != null && !carte.uuid.equals("")) {
            mongoCarte.put(Carte.UUID, carte.uuid);
            carteColl.update(new BasicDBObject().append(Carte.UUID, carte.uuid), mongoCarte);
            return carte;
        }
        else {
            String uuid = Codec.UUID();
            mongoCarte.put(Carte.UUID, uuid);
            carteColl.insert(mongoCarte);
            return findMapByUuid(uuid);
        }

    }

    public static void delete(Carte carte) {
        DBCollection carteColl = MongoDb.db().getCollection(COLLECTION_CARTE);
        // do a query to find the user carte
        BasicDBObject query = new BasicDBObject();
        query.put(Carte.UUID, carte.uuid);
        DBCursor cursor = carteColl.find(query);

        if (cursor.hasNext()) {
            DBObject map = cursor.next();
            carteColl.remove(map);
        }
    }

    private static Carte mongoToCarte(DBObject mongoCarte) {
        Carte carte = new Carte();

        if (mongoCarte.containsField(Carte.DESCRIPTION))
            carte.description = (String) mongoCarte.get(Carte.DESCRIPTION);

        if (mongoCarte.containsField(Carte.NAME))
            carte.name = (String) mongoCarte.get(Carte.NAME);

        if (mongoCarte.containsField(Carte.USER_UUID))
            carte.user_uuid = (String) mongoCarte.get(Carte.USER_UUID);

        if (mongoCarte.containsField(Carte.CENTER_LAT) && ((Double) mongoCarte.get(Carte.CENTER_LAT)) != null)
            carte.centerLat = ((Double) mongoCarte.get(Carte.CENTER_LAT)).floatValue();

        if (mongoCarte.containsField(Carte.CENTER_LNG) && ((Double) mongoCarte.get(Carte.CENTER_LNG)) != null)
            carte.centerLng = ((Double) mongoCarte.get(Carte.CENTER_LNG)).floatValue();

        if (mongoCarte.containsField(Carte.CENTER_ZOOM))
            carte.centerZoom = (Integer) mongoCarte.get(Carte.CENTER_ZOOM);

        if (mongoCarte.containsField(Carte.LEAFT_PILE_ZOOM))
            carte.leafPileZoom = (Integer) mongoCarte.get(Carte.LEAFT_PILE_ZOOM);

        if (mongoCarte.containsField(Carte.UUID))
            carte.uuid = (String) mongoCarte.get(Carte.UUID);

        if (mongoCarte.containsField(Carte.PATTERN))
            carte.pattern = (String) mongoCarte.get(Carte.PATTERN);

        return carte;
    }
}
