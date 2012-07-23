package models;

import java.util.ArrayList;
import java.util.List;

import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;
import securesocial.provider.UserId;

public class User {

    public String              uuid;
    public final static String UUID         = "UUID";

    /**
     * The user full name.
     */
    public String              displayName;
    public final static String DISPLAY_NAME = "DISPLAY_NAME";

    /**
     * The user's email
     */
    public String              email;
    public final static String EMAIL        = "EMAIL";

    /**
     * The user's site
     */
    public String              site;
    public final static String SITE         = "SITE";

    /**
     * A URL pointing to an avatar
     */
    public String              avatarUrl;
    public final static String AVATAR_URL   = "AVATAR_URL";

    /**
     * Is an admin ?
     */
    public Boolean             isAdmin;
    public final static String IS_ADMIN     = "IS_ADMIN";

    /**
     * The OAuth1 token (available when authMethod is OAUTH1 or OPENID_OAUTH_HYBRID)
     */
    public String              token;
    public final static String TOKEN        = "TOKEN";

    /**
     * The OAuth1 secret (available when authMethod is OAUTH1 or OPENID_OAUTH_HYBRID)
     */
    public String              secret;
    public final static String SECRET       = "SECRET";

    /**
     * The OAuth2 access token (available when authMethod is OAUTH2)
     */
    public String              accessToken;
    public final static String ACCESS_TOKEN = "ACCESS_TOKEN";

    /**
     * The user password (available when authMethod is USER_PASSWORD)
     */
    public String              password;
    public final static String PASSWORD     = "PASSWORD";

    /**
     * A boolean indicating if the user has validated his email adddress (available when authMethod is USER_PASSWORD)
     */
    public boolean             isEmailVerified;
    public final static String IS_EMAIL_OK  = "IS_EMAIL_OK";

    public List<UserAccount>   accounts     = new ArrayList<UserAccount>();

    /**
     * Convert a User object to UserSocial object.
     * 
     * @return SocialUser
     */
    public SocialUser toUserSocial() {
        SocialUser socialUser = new SocialUser();
        socialUser.accessToken = this.accessToken;
        socialUser.avatarUrl = this.avatarUrl;
        socialUser.displayName = this.displayName;
        socialUser.email = this.email;
        socialUser.isEmailVerified = this.isEmailVerified;
        socialUser.password = this.password;
        socialUser.secret = this.secret;
        socialUser.token = this.token;

        UserAccount account = accounts.get(0);
        UserId userId = new UserId();
        userId.id = account.userId;
        userId.provider = ProviderType.valueOf(account.provider);
        socialUser.id = userId;

        return socialUser;
    }
}
