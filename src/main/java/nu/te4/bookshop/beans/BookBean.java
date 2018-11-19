/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.te4.bookshop.beans;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import nu.te4.bookshop.utilities.Book;
import nu.te4.bookshop.utilities.BookFacade;
import nu.te4.bookshop.utilities.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author TE4-Lärardator
 */
@Stateless
public class BookBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookBean.class);
    
    /**
     * <h2>Add External Books</h2>
     * <p>Method to populate database from external resource</p>
     * @return true if success otherwise false
     */
    public boolean addExternalBooks(){
        BookFacade bookFacade = new BookFacade();
        return bookFacade.add(null, 0);
    }

    /**
     * <h2>Buy Books</h2>
     * <p>Method for buy books</p>
     * @param body json-array [{'title':'','author':'',price:Nr},..]
     * @return json-object {totalPrice:Nr, Results:[{'bookname':status},..]
     */
    public JsonObject buyBooks(String body) {
        BookFacade bookFacade = new BookFacade();
        //Create List of Books
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Book>>() {}.getType();
        List<Book> bookList = gson.fromJson(body, listType);
        int[] result = bookFacade.buy(bookList.toArray(new Book[bookList.size()]));
        //create customn json-object instead of create specific class and use Gson
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (int i = 0; i < bookList.size(); i++) { 
            jsonArrayBuilder.add(
                    Json.createObjectBuilder().add(bookList.get(i).getTitle(), 
                               Status.values()[result[i]].toString()).build()
            );
        }
        return Json.createObjectBuilder().add("TotalPrice", result[result.length-1])
                                   .add("results", jsonArrayBuilder.build()).build();
    }

    /**
     * <h2>Add Book</h2>
     * <p>Method to add book</p>
     * @param body in json-format {'title':'','author':'',price:Nr,quantity:Nr}
     * @return true if success otherwise false
     */
    public boolean addBook(String body) {
        int quantity = 0; //default value
        try {//temp solution, better add quantity in book-object
            quantity = Integer.parseInt(body.substring(body.lastIndexOf(":") 
                                                + 1, body.lastIndexOf("}")));
        } catch (NumberFormatException e) {
            LOGGER.error("Error from {}, message: {}", BookBean.class, e.getMessage());
        }
        BookFacade bookFacade = new BookFacade();
        Gson gson = new Gson();
        Book book = gson.fromJson(body, Book.class);
        return bookFacade.add(book, quantity);
    }

    /**
     * <h2>Search Books</h2>
     * <p>Method to search after books</p>
     * @param title part of title you want to search after
     * @param author part of authorname you want to search after
     * @return json-formated string
     */
    public String searchBooks(String title, String author) {
        BookFacade bookFacade = new BookFacade();
        String searchString = String.format("{'title':'%s','author':'%s'}", title, author);
        Book[] books = bookFacade.list(searchString);
        Gson gson = new Gson();
        return gson.toJson(books);
    }
}
