package controllers;

import java.io.File;
import java.util.List;

import javax.validation.Valid;

import jobs.ImportCSV;
import models.Data;
import play.data.validation.Required;
import play.mvc.With;
import securesocial.provider.SocialUser;
import service.CarteService;
import service.DataService;
import service.UserService;
import controllers.securesocial.SecureSocial;
import controllers.securesocial.SecureSocialPublic;

@With(SecureSocialPublic.class)
public class Carte extends AbstractController {

    public static void index() {
        render();
    }

    public static void add() {
        isValidUser();
        render("@edit");
    }

    public static void edit(String uuid) {
        isValidUser();
        models.Carte carte = CarteService.findMapByUuid(uuid);
        if (carte != null) {
            render(carte);
        }
        else {
            notFound();
        }
    }

    public static void save(@Valid models.Carte carte) {
        flash.clear();
        isValidUser();
        if (!validation.valid(carte).ok) {
            validation.keep();
            params.flash();
            render("@add", carte);
        }
        validation.clear();
        if (carte.user_uuid == null | carte.user_uuid.equals("")) {
            SocialUser socialUser = SecureSocial.getCurrentUser();
            models.User user = UserService.findUser(socialUser.id);
            carte.user_uuid = user.uuid;
        }
        carte = CarteService.save(carte);
        data(carte.uuid);
    }

    public static void importCSV(@Required File csv, @Required String uuid, String geocodingString, Boolean reset) {
        validation.clear();
        isValidUser();
        if (validation.hasErrors()) {
            validation.keep();
            params.flash();
            edit(uuid);
        }
        models.Carte carte = CarteService.findMapByUuid(uuid);
        if (reset != null && reset) {
            DataService.deleteDataCarte(uuid);
        }
        Integer nbItem = 0;
        try {
            nbItem = ImportCSV.doJobWithResult(csv, carte, geocodingString);
        } catch (Exception e) {
            flash.error(e.getMessage());
            params.flash();
            render("@edit", carte);
        }
        flash.success("Importing " + nbItem + " item");
        data(uuid);
    }

    public static void delete(@Required String uuid) {
        isValidUser();
        if (validation.hasErrors()) {
            validation.keep();
            params.flash();
        }
        validation.clear();
        models.Carte carte = CarteService.findMapByUuid(uuid);
        CarteService.delete(carte);
        DataService.deleteDataCarte(uuid);
        User.cartes();
    }

    public static void view(String uuid) {
        models.Carte carte = CarteService.findMapByUuid(uuid);
        List<Data> datas = DataService.getDataFromCarte(carte.uuid);
        Boolean mine = Boolean.FALSE;
        SocialUser socialUser = SecureSocial.getCurrentUser();
        if (socialUser != null) {
            models.User user = UserService.findUser(socialUser.id);
            if (user.uuid.equals(carte.user_uuid)) {
                mine = Boolean.TRUE;
            }

        }
        render(carte, datas, mine);
    }

    public static void data(String uuid) {
        models.Carte carte = CarteService.findMapByUuid(uuid);
        List<Data> datas = DataService.getDataFromCarte(carte.uuid);
        List<String> headers = DataService.getDataHeader(carte.uuid);
        Boolean mine = Boolean.FALSE;
        SocialUser socialUser = SecureSocial.getCurrentUser();
        if (socialUser != null) {
            models.User user = UserService.findUser(socialUser.id);
            if (user.uuid.equals(carte.user_uuid)) {
                mine = Boolean.TRUE;
            }

        }
        render(carte, datas, headers, mine);
    }

    public static void updatedata() {
        isValidUser();
        render();
    }
}
