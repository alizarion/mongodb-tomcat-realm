package com.alizarion.mongodb.realm;

import org.apache.catalina.Realm;
import org.apache.catalina.realm.GenericPrincipal;
import org.ietf.jgss.GSSCredential;

import javax.security.auth.login.LoginContext;
import java.security.Principal;
import java.util.List;

/**
 * Created by selim.bensenouci on 17/01/14.
 */
public class MongoDBPrincipal extends GenericPrincipal {

    private String id;

    public MongoDBPrincipal(String name, String password,
                            List<String> roles,String id) {
        super(name, password, roles);
        this.id = id;
    }

    public MongoDBPrincipal(String name, String password,
                            List<String> roles, Principal userPrincipal) {
        super(name, password, roles, userPrincipal);
        this.id = id;
    }

    public MongoDBPrincipal(String name, String password,
                            List<String> roles, Principal userPrincipal,
                            LoginContext loginContext) {
        super(name, password, roles, userPrincipal, loginContext);
        this.id = id;

    }

    public MongoDBPrincipal(String name, String password,
                            List<String> roles, Principal userPrincipal,
                            LoginContext loginContext,
                            GSSCredential gssCredential) {
        super(name, password, roles, userPrincipal, loginContext, gssCredential);
        this.id = id;
    }


    public String getId() {
        return id;
    }
}
