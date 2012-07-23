package models;

public class UserAccount {

    public final static String ACCOUNT_ID = "ACCOUNT_ID";
    public final static String PROVIDER   = "PROVIDER";
    public final static String USER_UUID  = "USER_UUID";
    public final static String UUID       = "UUID";

    /**
     * uuid of the document.
     * 
     */
    public String              uuid;

    /**
     * The id the user has in a external service.
     */
    public String              userId;

    /**
     * The provider this user belongs to.
     */
    public String              provider;

}
