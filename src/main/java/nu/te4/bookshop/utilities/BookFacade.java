/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.te4.bookshop.utilities;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.mysql.jdbc.Connection;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author Daniel
 */
public class BookFacade implements BookList {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookFacade.class);

    /**
     *<h2>List</h2>
     * <p>Method to get books from the database, depending of the searchquery</p>
     * @param searchString in format of a book {'title'='your search', 'author'='your search'}
     * @return array of books matching the searchquery
     * @throws IllegalArgumentException if searchstring is corrupt
     */
    @Override
    public Book[] list(String searchString) {
        List<Book> books = new ArrayList<>();
        try (Connection connection = ConnectionFactory.make(ConnectionFactory.getDatabase())) {
            Book book = new Gson().fromJson(searchString, Book.class);
            //Since user can modify data we use PreparedStatement
            PreparedStatement stmt = createPreparedSqlSearch(book, "LIKE", connection);
            ResultSet data = stmt.executeQuery();
            while (data.next()) {
                books.add(new Book(data.getString("title"), data.getString("author")
                                                           , data.getDouble("price")));
            }
        } catch (SQLException e) {
            LOGGER.error("Error from {}, message: {}", BookFacade.class, e.getMessage());
        } catch (JsonSyntaxException | JsonIOException e) {
            LOGGER.error("Error from {}, message: {}", BookFacade.class, e.getMessage());
            throw new IllegalArgumentException("bad format");
        } catch (Exception e) {
            LOGGER.error("Error from {}, message: {}", BookFacade.class, e.getMessage());
        }
        return books.toArray(new Book[books.size()]);
    }

    /**
     *<h2>Add</h2>
     * <p>Method for adding book to database</p>
     * @param book id book = null, server will populate database from external resource
     * @param quantity nr of books
     * @return true if successfull else false
     */
    @Override
    public boolean add(Book book, int quantity) {
        if(quantity < 0){quantity =0; }//cant be less than zero
        if (book == null) {
            System.out.println("LOADING");
            try { //populate from "outer"  database
                String url = "https://raw.githubusercontent.com/contribe/contribe/dev/bookstoredata/bookstoredata.txt";
                return loadBookStoreData(url);
            } catch (ParseException e) { //just logg errors
                LOGGER.error("Error from {}, message: {}", BookFacade.class, e.getMessage());
            }
        }else if(book.getAuthor().isEmpty() && book.getTitle().isEmpty()){
            return false; //not accepting empty inputs
        }else {
            try (Connection connection = ConnectionFactory.make(ConnectionFactory.getDatabase())) {
                //check if excist
                PreparedStatement stmt = connection.prepareStatement(
                        "SELECT * FROM book WHERE title = ? AND author = ? AND price = ?");
                stmt.setString(1, book.getTitle());
                stmt.setString(2, book.getAuthor());
                stmt.setDouble(3, book.getPrice().doubleValue());
                ResultSet data = stmt.executeQuery();
                data.last();
                if (data.getRow() == 0) {//not excist
                    stmt = connection.prepareStatement("INSERT INTO book VALUES(?,?,?,?)");
                    stmt.setString(1, book.getTitle());
                    stmt.setString(2, book.getAuthor());
                    stmt.setDouble(3, book.getPrice().doubleValue());
                    stmt.setDouble(4, quantity);
                    stmt.execute();
                    return true; 
                } else { // excist
                    return updateQuantity(book, quantity + data.getInt("quantity"));
                }
            } catch (Exception e) {
                LOGGER.error("Error from {}, message: {}", BookFacade.class, e.getMessage());
            }
        }
        return false;
    }

    /**
     * <h2>Buy</h2>
     * <p>Method to buy books, if in stock one will dissapear from database</p>
     * @param books
     * @return int array, use Enum Status for more meningful code. Last index is total price rounded to integer
     */
    @Override
    public int[] buy(Book... books) {
        double totalPrice = 0;
        int[] response = new int[books.length + 1]; //last is totalPrice
        for (int i = 0; i < books.length; i++) {
            try (Connection connection = ConnectionFactory.make(ConnectionFactory.getDatabase())) {
                PreparedStatement stmt = createPreparedSqlSearch(books[i], "EQUAL", connection);
                ResultSet data = stmt.executeQuery();
                data.last();
                if (data.getRow() == 1) {//just one excist
                    int quantity = data.getInt("quantity");
                    if (quantity > 0) { //if book is in stock
                        if (updateQuantity(books[i], --quantity)) {
                            totalPrice += data.getDouble("price");
                            response[i] = Status.OK.getValue();
                        }
                    } else {
                        response[i] = Status.NOT_IN_STOCK.getValue();
                    }
                } else {
                    response[i] = Status.DOES_NOT_EXIST.getValue();
                }
            } catch (Exception e) {
                LOGGER.error("Error from {}, message: {}", BookFacade.class, e.getMessage());
            }
        }
        response[books.length] = (int) Math.round(totalPrice); //round to integer
        return response;
    }
    /*
    * Really long method
    * Reusability in consern made it longer, but it can be used twice
    * With normal Statements it should have been really small, but with
    * user-indata preparedstatement should be used
    * Other solution is to use an ORM-solution and just used simple JPA-Queries
    * But thats more configuration than programming
    */
    private PreparedStatement createPreparedSqlSearch(Book book, String Pronoun, 
                                    Connection connection) throws SQLException {
        if (book == null) {
            return connection.prepareStatement("SELECT * FROM book");
        }
        boolean hasTitle = (book.getTitle() != null);
        boolean hasAuthor = (book.getAuthor() != null);
        boolean hasPrice = (book.getPrice() != null);
        //1-Build search-String
        String search = "SELECT * FROM book";
        if (hasTitle || hasAuthor || hasPrice) { //atleast one parameter should excist
            search += " WHERE";
            boolean added = false; //if more than one param
            String equalOrLike = (Pronoun.equals("LIKE") ? "LIKE" : "=");
            if (hasTitle) {
                search += String.format(" title %s ?", equalOrLike);
                added = true;
            }
            if (hasAuthor) {
                if (added) {
                    search += " AND";
                }
                search += String.format(" author %s ?", equalOrLike);
                added = true;
            }
            if (hasPrice && !Pronoun.equals("LIKE")) {
                if (added) {
                    search += " AND";
                }
                search += String.format(" price %s ?", equalOrLike);
            }
        }
        //populate statement
        PreparedStatement stmt = connection.prepareStatement(search);
        int index = 1;
        if (hasTitle) {
            String tempSearchString = book.getTitle();
            if (Pronoun.equals("LIKE")) { //add Wildcards
                tempSearchString = "%" + tempSearchString + "%";
            }
            stmt.setString(index++, tempSearchString);
        }
        if (hasAuthor) {
            String tempSearchString = book.getAuthor();
            if (Pronoun.equals("LIKE")) {
                tempSearchString = "%" + tempSearchString + "%";
            }
            stmt.setString(index++, tempSearchString);
        }
        if (hasPrice && !Pronoun.equals("LIKE")) {
            stmt.setDouble(index, book.getPrice().doubleValue());
        }
        return stmt;
    }

    private boolean loadBookStoreData(String url) throws ParseException {
        NumberFormat numberFormatUS = NumberFormat.getInstance(new java.util.Locale("US"));
        Client client = ClientBuilder.newClient();
        try {
            String response = client.target(url).request(MediaType.TEXT_PLAIN).get(String.class);
            String[] lines = response.split("\n");
            for (String line : lines) { //spitting the text
                String[] bookData = line.split(";");
                String author = bookData[1];
                String title = bookData[0];
                Number number = numberFormatUS.parse(bookData[2]);
                BigDecimal price = new BigDecimal(number.doubleValue())
                            .setScale(2, BigDecimal.ROUND_HALF_UP); //2 digits enuff
                int quantity = Integer.parseInt(bookData[3]);
                add(new Book(title, author, price), quantity);
            }
            return true; 
        } catch (ParseException | NumberFormatException | ProcessingException e) {
            LOGGER.error("Error from {}, message: {}", BookFacade.class, e.getMessage());
            throw new ProcessingException("Problem with external datasource.");
        }

    }
    /*
     *are used if book exist in database, that means a book with same
     * title, author and price
     * Since no id/isbn exist prosume that book with same title and author but
     * different price means that it is a different edition
    */
    private boolean updateQuantity(Book book, int quantity) {
        try (Connection connection = ConnectionFactory.make(ConnectionFactory.getDatabase())) {
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE book SET quantity = ? WHERE title = ? AND author = ? AND price= ?");
            stmt.setDouble(1, quantity);
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setDouble(4, book.getPrice().doubleValue());
            stmt.execute();
            return true;
        } catch (Exception e) {
            LOGGER.error("Error from {}, message: {}", BookFacade.class,e.getMessage());
        }
        return false;
    }

}
