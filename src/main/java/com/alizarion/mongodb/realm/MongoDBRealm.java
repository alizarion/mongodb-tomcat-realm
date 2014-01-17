package com.alizarion.mongodb.realm;

import com.mongodb.*;
import org.apache.catalina.realm.RealmBase;

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
     * mongoDB port.
     */
    private int port;

    /**
     * DataBase name attribute
     */
    private String dataBaseName;

    /**
     * Document where are the credential stored
     */
    private String userAndPasswordCollection;

    /**
     * Document where roles are stored
     */
    private String rolesCollection;

    /**
     * User login of the dataBase
     */
    private String mongoDBLogin;

    /**
     * username field in the user and password collection  <br/>
     * \'username\' by default
     */
    private String userNameField;


    /**
     * password field in the user and password collection  <br/>
     * \'password\' by default
     */
    private String passwordField;



    /**
     * Login password of the dataBase
     */
    private String mongoDBPassword;

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

    public String getRolesCollection() {
        return rolesCollection;
    }

    public void setRolesCollection(String rolesCollection) {
        this.rolesCollection = rolesCollection;
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
                    this.getUserNameField() : "username", username);
            DBObject myDoc = coll.findOne(query);
            password=(String)myDoc.get(this.getPasswordField() != null ?
                    this.getPasswordField() :  "password");
        }catch(Exception e){
            e.printStackTrace();
        }
        return password;
    }

    @Override
    protected Principal getPrincipal(final String username) {
        final List<String> roles = new ArrayList<String>();
        DB db=getMongo().getDB(this.getDataBaseName());

        DBCollection coll = db.getCollection(this.
                getUserAndPasswordCollection() != null ?
        this.getUserAndPasswordCollection() : "user" );

        BasicDBObject query = new BasicDBObject();
        query.put(this.getUserNameField()!= null? this.getUserNameField()
                : "username", username);

        DBCollection collRoles = db.getCollection(this.
                getRolesCollection() != null ?
                this.getRolesCollection() :
                (this.getUserAndPasswordCollection() != null
                        ? this.getUserAndPasswordCollection() : "user" ));

        BasicDBObject rolesQuery = new BasicDBObject();
        query.put(this.getUserNameField()!= null? this.getUserNameField()
                : "username", username);

        DBObject myDoc = collRoles.findOne(query);
        BasicDBList roles_obj = (BasicDBList)myDoc.get("roles");
        Iterator<Object> it=roles_obj.iterator();
        while(it.hasNext()){
            roles.add((String)it.next());
        }

        Principal p = null;
        try{
            p = new MongoDBPrincipal(username,
                    this.getPassword(username),
                    roles,(String)myDoc.get("_id"));
        }catch(Exception e){
            e.printStackTrace();
        }
        return p;
    }
    private Mongo getMongo(){
        if(mongo==null){
            try {
                mongo=new Mongo( this.getConnectionString(),this.getPort() );
                DB db=mongo.getDB(this.getUserDbName());
                db.authenticate(this.getMongoDbUserName(),
                        this.getMongoDbUserPassword().toCharArray());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return mongo;
    }
}
