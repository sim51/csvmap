package service;

import models.User;
import models.UserAccount;
import play.cache.Cache;
import play.libs.Codec;
import play.mvc.Scope.Session;
import securesocial.provider.SocialUser;
import securesocial.provider.UserId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class UserService implements securesocial.provider.UserService.Service {

    public final static String COLLECTION_USER         = "user";
    public final static String COLLECTION_USER_ACCOUNT = "useraccount";

    /**
     * Method to find a User from its UserId (ie provider and its id provider)
     * 
     * @param id
     * @return
     */
    public static User findUser(UserId id) {
        // getting the user account collection
        DBCollection userAccountColl = MongoDb.db().getCollection(COLLECTION_USER_ACCOUNT);
        // do a query to find the user.id from user account where user account as provider and userId
        BasicDBObject query = new BasicDBObject();
        query.put(UserAccount.ACCOUNT_ID, id.id);
        query.put(UserAccount.PROVIDER, id.provider.name());
        DBCursor cursor = userAccountColl.find(query);

        if (cursor.hasNext()) {
            DBObject userAccountObject = cursor.next();
            String userUuid = (String) userAccountObject.get(UserAccount.USER_UUID);

            // getting the user collection
            DBCollection userColl = MongoDb.db().getCollection(COLLECTION_USER);
            // do a query to find the user by its uuid
            BasicDBObject userQuery = new BasicDBObject();
            userQuery.put(User.UUID, userUuid);
            DBCursor userCursor = userColl.find(userQuery);
            if (userCursor.hasNext()) {
                return UserService.MongoToUser(userCursor.next());
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    /**
     * Convert a UserSocial object to User object.
     * 
     * @return SocialUser
     */
    public static User UserSocialToUser(SocialUser socialUser) {
        User user = new User();
        user.accessToken = socialUser.accessToken;
        user.avatarUrl = socialUser.avatarUrl;
        user.displayName = socialUser.displayName;
        user.email = socialUser.email;
        user.isAdmin = Boolean.FALSE;
        user.isEmailVerified = socialUser.isEmailVerified;
        user.password = socialUser.password;
        user.secret = socialUser.secret;
        user.token = socialUser.token;

        UserAccount account = new UserAccount();
        account.userId = socialUser.id.id;
        account.provider = socialUser.id.provider.name();

        user.accounts.add(account);

        return user;
    }

    /**
     * Convert a mongo userAccount object to UserAccount object.
     * 
     * @param mongoUser
     * @return
     */
    public static UserAccount MongoToUserAccount(DBObject mongoUser) {
        UserAccount account = new UserAccount();

        if (mongoUser.containsField(UserAccount.UUID))
            account.uuid = (String) mongoUser.get(UserAccount.UUID);

        if (mongoUser.containsField(UserAccount.PROVIDER))
            account.provider = (String) mongoUser.get(UserAccount.PROVIDER);

        if (mongoUser.containsField(UserAccount.ACCOUNT_ID))
            account.userId = (String) mongoUser.get(UserAccount.ACCOUNT_ID);

        return account;
    }

    /**
     * Convert a mongo user object to User object.
     * 
     * @param mongoUser
     * @return
     */
    public static User MongoToUser(DBObject mongoUser) {
        User user = new User();

        // User information
        if (mongoUser.containsField(User.UUID))
            user.uuid = (String) mongoUser.get(User.UUID);

        if (mongoUser.containsField(User.ACCESS_TOKEN))
            user.accessToken = (String) mongoUser.get(User.ACCESS_TOKEN);

        if (mongoUser.containsField(User.AVATAR_URL))
            user.avatarUrl = (String) mongoUser.get(User.AVATAR_URL);

        if (mongoUser.containsField(User.DISPLAY_NAME))
            user.displayName = (String) mongoUser.get(User.DISPLAY_NAME);

        if (mongoUser.containsField(User.EMAIL))
            user.email = (String) mongoUser.get(User.EMAIL);

        if (mongoUser.containsField(User.SITE))
            user.site = (String) mongoUser.get(User.SITE);

        if (mongoUser.containsField(User.IS_ADMIN))
            user.isAdmin = (Boolean) mongoUser.get(User.IS_ADMIN);

        if (mongoUser.containsField(User.IS_EMAIL_OK))
            user.isEmailVerified = (Boolean) mongoUser.get(User.IS_EMAIL_OK);

        if (mongoUser.containsField(User.PASSWORD))
            user.password = (String) mongoUser.get(User.PASSWORD);

        if (mongoUser.containsField(User.TOKEN))
            user.token = (String) mongoUser.get(User.TOKEN);

        // UserAccount information
        // getting the user account collection
        DBCollection userAccountColl = MongoDb.db().getCollection(COLLECTION_USER_ACCOUNT);
        // do a query to find the user.id from user account where user account as provider and userId
        BasicDBObject query = new BasicDBObject();
        query.put(UserAccount.USER_UUID, user.uuid);
        DBCursor cursor = userAccountColl.find(query);
        while (cursor.hasNext()) {
            UserAccount account = UserService.MongoToUserAccount(cursor.next());
            user.accounts.add(account);
        }

        return user;
    }

    /**
     * Find a User by its uuid from the database.
     * 
     * @param uuid
     * 
     * @return User
     */
    public static User findUserByUuid(String uuid) {
        // getting the user collection
        DBCollection userColl = MongoDb.db().getCollection(UserService.COLLECTION_USER);
        // do a query to find admin
        BasicDBObject query = new BasicDBObject();
        query.put(User.UUID, uuid);
        DBCursor cursor = userColl.find(query);

        if (cursor.hasNext()) {
            return UserService.MongoToUser(cursor.next());
        }
        else {
            return null;
        }
    }

    /**
     * Save a user into mongo.
     * 
     * @param user
     * @return
     */
    public static User saveUser(User user) {
        DBCollection userColl = MongoDb.db().getCollection(UserService.COLLECTION_USER);
        BasicDBObject mongoUser = new BasicDBObject();

        mongoUser.put(User.ACCESS_TOKEN, user.accessToken);
        mongoUser.put(User.AVATAR_URL, user.avatarUrl);
        mongoUser.put(User.DISPLAY_NAME, user.displayName);
        mongoUser.put(User.EMAIL, user.email);
        mongoUser.put(User.IS_ADMIN, user.isAdmin);
        mongoUser.put(User.IS_EMAIL_OK, user.isEmailVerified);
        mongoUser.put(User.PASSWORD, user.password);
        mongoUser.put(User.SECRET, user.secret);
        mongoUser.put(User.SITE, user.site);
        mongoUser.put(User.TOKEN, user.token);

        // update
        if (user.uuid != null) {
            mongoUser.put(UserAccount.UUID, user.uuid);
            userColl.update(new BasicDBObject().append(User.UUID, user.uuid), mongoUser);
        }
        // insert
        else {
            user.uuid = Codec.UUID();
            mongoUser.put(User.UUID, user.uuid);
            userColl.insert(mongoUser);
        }
        return findUserByUuid(user.uuid);
    }

    /**
     * Save a user account into mongo.
     * 
     * @param uuid
     * @param account
     */
    private static void saveUserAccount(String uuid, UserAccount account) {
        DBCollection userAccountColl = MongoDb.db().getCollection(UserService.COLLECTION_USER_ACCOUNT);
        BasicDBObject mongoAccount = new BasicDBObject();

        mongoAccount.put(UserAccount.PROVIDER, account.provider);
        mongoAccount.put(UserAccount.ACCOUNT_ID, account.userId);
        mongoAccount.put(UserAccount.USER_UUID, uuid);

        // update
        if (account.uuid != null) {
            mongoAccount.put(UserAccount.UUID, account.uuid);
            userAccountColl.update(new BasicDBObject().append(UserAccount.UUID, account.uuid), mongoAccount);
        }
        // insert
        else {
            mongoAccount.put(UserAccount.UUID, Codec.UUID());
            userAccountColl.insert(mongoAccount);
        }
    }

    @Override
    public SocialUser find(UserId id) {
        User user = this.findUser(id);
        if (user != null) {
            return user.toUserSocial();
        }
        else {
            return null;
        }
    }

    @Override
    public void save(SocialUser socialUser) {
        User user = findUser(socialUser.id);
        if (user == null) {
            if (Session.current().get("member") != null) {
                String uuid = Session.current().get("member");
                user = UserService.findUserByUuid(uuid);
                UserAccount account = new UserAccount();
                account.userId = socialUser.id.id;
                account.provider = socialUser.id.provider.toString();
                UserService.saveUserAccount(uuid, account);
            }
            else {
                user = UserService.UserSocialToUser(socialUser);
                User userSaved = UserService.saveUser(user);
                for (UserAccount account : user.accounts) {
                    UserService.saveUserAccount(userSaved.uuid, account);
                }
            }
        }
    }

    @Override
    public String createActivation(SocialUser user) {
        final String uuid = Codec.UUID();
        Cache.add(uuid, user, "24h");
        return uuid;
    }

    @Override
    public boolean activate(String uuid) {
        SocialUser socialUser = (SocialUser) Cache.get(uuid);
        User user = this.findUser(socialUser.id);
        user.isEmailVerified = true;
        UserService.saveUser(user);
        Cache.delete(uuid);
        return true;
    }

    @Override
    public void deletePendingActivations() {
        // getting the user account collection
        DBCollection userColl = MongoDb.db().getCollection(COLLECTION_USER);
        // do a query to find the user.id from user account where user account as provider and userId
        BasicDBObject query = new BasicDBObject();
        query.put(User.IS_EMAIL_OK, "false");
        DBCursor cursor = userColl.find(query);

        while (cursor.hasNext()) {
            DBObject user = cursor.next();
            userColl.remove(user);
        }
    }

}
