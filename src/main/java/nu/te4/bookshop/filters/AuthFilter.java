/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.te4.bookshop.filters;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Daniel
 */
@Provider
public class AuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        //filter should not affect GET-Methods
        if (requestContext.getMethod().equals("GET")) {
            return;
        }
        List<String> authHeaders = requestContext.getHeaders().get("Authorization");
        if (authHeaders.size() > 0) { //if excist
            try { //just for demonstrate auth-filters
                String basicAuth = authHeaders.get(0); //get first entry
                basicAuth = basicAuth.substring(6).trim(); //remove "Basic "
                byte[] bytes = Base64.getDecoder().decode(basicAuth);
                basicAuth = new String(bytes); //test:test
                int colon = basicAuth.indexOf(":");
                String username = basicAuth.substring(0, colon);
                String password = basicAuth.substring(colon + 1);
                if(username.equals("test") && password.equals("test")){
                    return;
                }
            } catch (Exception e) {
                //TODO: Logging
            }
        }

        Response unAuth = Response.status(Response.Status.UNAUTHORIZED).entity("Wrong credentials").build();
        requestContext.abortWith(unAuth);
    }

}
