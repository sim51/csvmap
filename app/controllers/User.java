package controllers;

import java.util.Collection;
import java.util.List;

import play.data.validation.Valid;
import play.mvc.With;
import securesocial.provider.ProviderRegistry;
import securesocial.provider.SocialUser;
import service.CarteService;
import service.UserService;
import controllers.securesocial.SecureSocial;
import controllers.securesocial.SecureSocialPublic;

@With(SecureSocialPublic.class)
public class User extends AbstractController {

    public static void view(String uuid) {
        models.User member = UserService.findUserByUuid(uuid);
        if (member != null) {
            List<models.Carte> cartes = CarteService.findMapByUser(member);
            Boolean mine = Boolean.FALSE;
            render(member, cartes, mine);
        }
        else {
            notFound();
        }
    }

    public static void myProfile() {
        isValidUser();
        SocialUser user = SecureSocial.getCurrentUser();
        models.User member = UserService.findUser(user.id);
        List<models.Carte> cartes = CarteService.findMapByUser(member);
        Boolean mine = Boolean.TRUE;
        render("@view", member, cartes, mine);
    }

    public static void edit() {
        isValidUser();
        Collection providers = ProviderRegistry.all();
        SocialUser userSocial = SecureSocial.getCurrentUser();
        models.User member = UserService.findUser(userSocial.id);
        session.put("member", member.uuid);
        render(providers, member);
    }

    public static void save(@Valid models.User member) {
        isValidUser();
        if (!validation.valid(member).ok) {
            validation.keep();
            params.flash();
            Collection providers = ProviderRegistry.all();
            render("@edit", providers, member);
        }
        validation.clear();
        UserService.saveUser(member);
        edit();
    }

    public static void cartes() {
        isValidUser();
        SocialUser user = SecureSocial.getCurrentUser();
        models.User member = UserService.findUser(user.id);
        List<models.Carte> cartes = CarteService.findMapByUser(member);
        render(cartes);
    }
}
