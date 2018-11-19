/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.te4.bookshop.services;

import javax.ejb.EJB;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import nu.te4.bookshop.beans.BookBean;

/**
 *
 * @author TE4-Lärardator
 */
@Path("")
public class BookService {
    
    @EJB
    BookBean bookBean; 
    
    /**
     * <h2>GET /Books</h2>
     * <p>GET Method to recive books from database in json-format</p>
     * @param title to search by title-name
     * @param author to search by author-name
     * @return Response ok(200) or servererror(500)
     */
    @GET
    @Path("books")
    @Produces(MediaType.APPLICATION_JSON+ "; charset=UTF-8")
    public Response test(@DefaultValue("")@QueryParam("title")String title,
                         @DefaultValue("")@QueryParam("author") String author){
        return Response.ok(bookBean.searchBooks(title, author)).build();
    }
    
    /**
     *<h2>POST /Books/external</h2>
     * <p>Method to populate database from external resource</p>
     * @return Response created(201), Unauthorized(401) or servererror(500)
     */
    @POST
    @Path("books/external")
    @Produces(MediaType.TEXT_PLAIN+ "; charset=UTF-8")
    public Response test(){
        if(bookBean.addExternalBooks()){
            return Response.status(Response.Status.CREATED)
                                .entity("populated").build();
        }else{
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                  .entity("Something went wrong").build();
        }
        
    }
    
    /**
     *<h2>POST /Book/buy</h2>
     * <p>Method that handles shoping, returns total-price and results in json-array</p>
     * @param body
     * @return Response ok(200), Unauthorized(401) or servererror(500)
     */
    @POST
    @Path("book/buy")
    @Produces(MediaType.APPLICATION_JSON+ "; charset=UTF-8")
    public Response buyBooks(String body){
        return Response.ok(bookBean.buyBooks(body)).build();
    }
    
    /**
     *<h2>GET /Books</h2>
     * <p></p>
     * @param body
     * @return Response created(201), bad request(400), Unauthorized(401) or servererror(500)
     */
    @POST
    @Path("book")
    @Produces(MediaType.TEXT_PLAIN+ "; charset=UTF-8")
    public Response addBook(String body){
        if(bookBean.addBook(body)){
                  return Response.status(Response.Status.CREATED)
                                  .entity("Book inserted").build();
        }else{
            return Response.status(Response.Status.BAD_REQUEST)
                                    .entity("Bad input").build();
        }
  
    }
}
