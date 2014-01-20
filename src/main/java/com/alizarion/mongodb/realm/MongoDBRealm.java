package com.alizarion.mongodb.realm;

import com.mongodb.*;
import org.apache.catalina.realm.RealmBase;
import org.bson.types.ObjectId;

import java.net.UnknownHostException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Simple Class that extending <b>RealmBase</b> for MongoDB usage
 */
public class MongoDBRealm extends RealmBase {

    /**
     * MongoDB host
     */
    private String host;

    /**
     * Default DataBase host
     */
    private static final String DEFAULT_HOST = "127.0.0.1";

    /**
     * mongoDB port.
     */
    private int port;

    /**
     * Default DataBase port
     */
    private static final int DEFAULT_PORT = 27017;

    /**
     * DataBase name attribute
     */
    private String dataBaseName;

    /**
     * Default DataBase name
     */
    private static final String DEFAULT_DB_NAME = "credential";


    /**
     * Document where are the credential stored
     */
    private String userAndPasswordCollection;

    /**
     * Default DataBase name
     */
    private static final String DEFAULT_COLLECTION = "user";


    /**
     * User login of the dataBase
     */
    private String mongoDBLogin;

    /**
     * Default DataBase Login
     */
    private static final String DEFAULT_MONGO_LOGIN = "";

    /**
     * Login password of the dataBase
     */
    private String mongoDBPassword;

    /**
     * Default DataBase password
     */
    private static final String DEFAULT_MONGO_PASSWORD = "";

    /**
     * username field in the user and password collection  <br/>
     * \'username\' by default
     */
    private String userNameField;

    /**
     * Default username field name
     */
    private static final String DEFAULT_USERNAME_FIELD= "username";

    /**
     * password field in the user and password collection  <br/>
     * \'password\' by default
     */
    private String passwordField;

    /**
     * Default password field name
     */
    private static final String DEFAULT_PASSWORD_FIELD = "password";


    /**
     * Roles field in the user and password collection  <br/>
     * \'roles\' by default
     */
    private String roleField;

    /**
     * Default password field name
     */
    private static final String DEFAULT_SINGLE_ROLE_FIELD = "role";

    /**
     * Roles field in the user and password collection  <br/>
     * \'roles\' by default
     */
    private String rolesFieldName;

    /**
     * Default password field name
     */
    private static final String DEFAULT_ROLES_FIELD = "roles";



    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort(){
        return this.port;
    }
    public void setPort(int port){
        this.port=port;
    }

    public String getDataBaseName() {
        return dataBaseName;
    }

    public void setDataBaseName(String dataBaseName) {
        this.dataBaseName = dataBaseName;
    }

    public String getMongoDBLogin() {
        return mongoDBLogin;
    }

    public void setMongoDBLogin(String mongoDBLogin) {
        this.mongoDBLogin = mongoDBLogin;
    }

    public String getMongoDBPassword() {
        return mongoDBPassword;
    }

    public void setMongoDBPassword(String mongoDBPassword) {
        this.mongoDBPassword = mongoDBPassword;
    }

    public String getUserAndPasswordCollection() {
        return userAndPasswordCollection;
    }

    public void setUserAndPasswordCollection(String userAndPasswordCollection) {
        this.userAndPasswordCollection = userAndPasswordCollection;
    }

    public String getUserNameField() {
        return userNameField;
    }

    public void setUserNameField(String userNameField) {
        this.userNameField = userNameField;
    }

    public String getPasswordField() {
        return passwordField;
    }

    public void setPasswordField(String passwordField) {
        this.passwordField = passwordField;
    }

    public String getRoleField() {
        return roleField;
    }

    public void setRoleField(String roleField) {
        this.roleField = roleField;
    }

    public String getRolesFieldName() {
        return rolesFieldName;
    }

    public void setRolesFieldName(String rolesFieldName) {
        this.rolesFieldName = rolesFieldName;
    }

    private static Mongo mongo =null;

    @Override
    protected String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected String getPassword(final String username) {
        String password = null;
        try{
            DB db=getMongo().getDB(this.getDataBaseName());
            DBCollection coll = db.getCollection(
                    this.getUserAndPasswordCollection());
            BasicDBObject query = new BasicDBObject();
            query.put(this.getUserNameField() != null ?
                    this.getUserNameField() : this.DEFAULT_USERNAME_FIELD, username);
            DBObject myDoc = coll.findOne(query);
            password=(String)myDoc.get(this.getPasswordField() != null ?
                    this.getPasswordField() :  this.DEFAULT_PASSWORD_FIELD);
        }catch(Exception e){
            e.printStackTrace();
        }
        return password;
    }

    /**
     * Method to get principal by username
     * @param username
     * @return Return the Principal associated with the given user name.
     */
    @Override
    protected Principal getPrincipal(final String username) {
        final List<String> roles = new ArrayList<String>();
        DB db = getMongo().getDB(this.getDataBaseName() != null ?
                this.getDataBaseName() : DEFAULT_DB_NAME  );

        DBCollection dbCollection= db.getCollection(
                this.getUserAndPasswordCollection()
                        != null ? this.getUserAndPasswordCollection() :
                        DEFAULT_COLLECTION);
        BasicDBObject query = new BasicDBObject();
        query.put(this.getUserNameField() != null ?
                this.getUserNameField() :
                DEFAULT_USERNAME_FIELD,username);

        DBObject myDocument = dbCollection.findOne(query);
        BasicDBList roles_obj = (BasicDBList) myDocument.
                get(this.getRolesFieldName() != null ?
                        this.getRolesFieldName() : DEFAULT_ROLES_FIELD);

        Iterator<Object> iterateRoleList = roles_obj.iterator();
        while (iterateRoleList.hasNext()){
            roles.add((String) ((BasicDBObject)
                    iterateRoleList.next()).get(
                    this.getRoleField()!= null ?
                            this.getRoleField() : DEFAULT_SINGLE_ROLE_FIELD));
        }

        Principal p = null;
        try {
            p = new MongoDBPrincipal(username,this.getPassword(username),
                    roles, myDocument.get("_id").toString());
        }  catch (Exception e ){
            e.printStackTrace();
        }
        return p;
    }



    private Mongo getMongo(){
        if(mongo==null){
            try {
                //get mongoDB host
                mongo=new Mongo( this.getHost()!= null ?
                        this.getHost() : DEFAULT_HOST,
                        this.getPort() != 0 ? this.getPort() :
                                DEFAULT_PORT );


                DB db=mongo.getDB(this.getDataBaseName()!= null ?
                        this.getDataBaseName() : DEFAULT_DB_NAME);
                db.authenticate(this.getMongoDBLogin() != null ?
                        this.getMongoDBLogin() : DEFAULT_MONGO_LOGIN,
                        this.getMongoDBPassword() != null ?
                                this.getMongoDBPassword().toCharArray() :
                                DEFAULT_MONGO_PASSWORD.toCharArray());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return mongo;
    }
}
