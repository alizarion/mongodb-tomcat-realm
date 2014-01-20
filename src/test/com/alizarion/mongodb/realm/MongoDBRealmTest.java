package com.alizarion.mongodb.realm;

import com.mongodb.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Simple test class
 */
public class MongoDBRealmTest {

    MongoDBRealm  mongoDBRealm;

    private static final String CREDENTIAL_PASSWORD = "toto";

    private static final String CREDENTIAL_USERNAME = "eridan";

    @Before
    public void setUp() throws Exception {
        //Connect to local database
        MongoClient mongoClient = new MongoClient();

        DB db = mongoClient.getDB("test");

        DBCollection dbCollection = db.getCollection("credential");
        dbCollection.drop();
        BasicDBList roles  = new BasicDBList();
        roles.add(new BasicDBObject("role", "admin"));

        //insert default credential value in the database
        BasicDBObject credential = new BasicDBObject("username",
                this.CREDENTIAL_USERNAME).
                append("password",this.getSHA1(this.CREDENTIAL_PASSWORD)).
                append("roles", roles);
        dbCollection.insert(credential);

        this.mongoDBRealm = new MongoDBRealm();
        mongoDBRealm.setDataBaseName("test");
        mongoDBRealm.setUserAndPasswordCollection("credential");

    }

    @After
    public void tearDown() throws UnknownHostException {
        MongoClient mongoClient = new MongoClient();

        DB db = mongoClient.getDB("test");

        DBCollection dbCollection = db.getCollection("credential");
        dbCollection.drop();
    }


    @Test
    public void getPrincipal(){

        MongoDBPrincipal mongoDBPrincipal =
                (MongoDBPrincipal)
                        this.mongoDBRealm.getPrincipal(this.CREDENTIAL_USERNAME);

        assertThat(mongoDBPrincipal, is(notNullValue()));

    }

    @Test
    public void getPassword() throws NoSuchAlgorithmException {

        MongoDBRealm  mongoDBRealm =
                new MongoDBRealm();
        mongoDBRealm.setDataBaseName("test");
        mongoDBRealm.setUserAndPasswordCollection("credential");
        String password =  mongoDBRealm.getPassword(this.CREDENTIAL_USERNAME);
        assertEquals(password, getSHA1(this.CREDENTIAL_PASSWORD));

    }


    /**
     * Method SHA1 from simple String
     * @param input
     * @return
     * @throws NoSuchAlgorithmException
     */
    private  String getSHA1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
