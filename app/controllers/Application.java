package controllers;

import java.util.concurrent.ExecutionException;

import notifier.Mails;
import play.cache.Cache;
import play.data.validation.Email;
import play.data.validation.Required;
import play.libs.Codec;
import play.libs.Images;
import play.mvc.With;
import securesocial.provider.SocialUser;
import controllers.securesocial.SecureSocial;
import controllers.securesocial.SecureSocialPublic;

@With(SecureSocialPublic.class)
public class Application extends AbstractController {

    public static void index() {
        String menu = "index";
        render(menu);
    }

    public static void about() {
        String menu = "about";
        SocialUser user = SecureSocial.getCurrentUser();
        render(menu, user);
    }

    public static void contact() {
        String menu = "contact";
        String randomID = Codec.UUID();
        SocialUser user = SecureSocial.getCurrentUser();
        if (user != null) {
            params.put("author", user.displayName);
            params.put("email", user.email);
        }
        render(menu, randomID, user);
    }

    public static void sendContact(@Required String author, @Required String message, @Required @Email String email,
            @Required String code, String randomID) throws InterruptedException, ExecutionException {

        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            randomID = Codec.UUID();
            String menu = "contact";
            render("@contact", menu, randomID);
        }
        Mails.contact(author, message, email);
        flash.success("Merci pour votre intéret %s", author);
        contact();
    }

    public static void captcha(String id) {
        Images.Captcha captcha = Images.captcha();
        // we set the text color of the captcha
        String code = captcha.getText("#000000");
        // we set the life time of the captcha
        Cache.set(id, code, "30mn");
        renderBinary(captcha);
    }

}
