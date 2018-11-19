/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.te4.bookshop.utilities;

import com.mysql.jdbc.Connection;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Daniel
 */
public class BookFacadeTest {

    private static Connection connection;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    public BookFacadeTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        try {
            connection = ConnectionFactory.make(""); //no database selected
            ScriptRunner scriptRunner = new ScriptRunner(connection);
            String sqlScriptPath = "C:\\bookshop\\bookshop_test.sql";
            Reader reader = new BufferedReader(new FileReader(sqlScriptPath));
            scriptRunner.runScript(reader);
        } catch (Exception e) { //catch both IO and SQL-Exceptions
            System.out.println("Error create test-database: " + e.getMessage());
        }
        ConnectionFactory.setDatabase("test"); //set database to testmode
    }

    @AfterClass
    public static void tearDownClass() {
        try {//clean up!
            Statement stmt = connection.createStatement();
            stmt.execute("DROP DATABASE bookshop_test");
            connection.close();
        } catch (Exception e) {
            System.out.println("Error clean test-database: " + e.getMessage());
        }
        ConnectionFactory.setDatabase("production"); //set database back to prod
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of list method, of class BookFacade.
     */
    @Test
    public void testList_1() {
        String title = "Java";
        String author = "Daniel";
        String searchString = String.format("{'title':'%s','author':'%s'}", title, author);
        BookFacade instance = new BookFacade();
        Book[] result = instance.list(searchString);
        assertThat("Controlling size equal than 3", result.length, is(3));
    }

    @Test
    public void testList_2() {
        String title = "";
        String author = "Does Not excist";
        String searchString = String.format("{'title':'%s','author':'%s'}", title, author);
        BookFacade instance = new BookFacade();
        Book[] result = instance.list(searchString);
        assertThat("Controlling size equal than 0", result.length == 0, is(true));
    }

    @Test
    public void testList_3() {
        String title = "Java";
        String author = "Daniel";
        String searchString = String.format("{'title':'%s','author':'%s'}", title, author);
        BookFacade instance = new BookFacade();
        Book[] result = instance.list(searchString);
        List<String> titles = new ArrayList<>();
        for (Book book : result) {
            titles.add(book.getTitle());
        }
        assertThat("Titles are correct: ", titles, containsInAnyOrder("Java", "Java2", "Java3"));
    }

    @Test
    public void testList_4() {
        System.out.println("Testing exceptions");
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("bad format");
        BookFacade instance = new BookFacade();
        instance.list("bad request");
    }

    /**
     * Test of add method, of class BookFacade.
     */
    @Test
    public void testAdd_1() {
        Book book = new Book("goto", "Basic", 100);
        int quantity = 100;
        BookFacade instance = new BookFacade();
        boolean result = instance.add(book, quantity);
        String searchString = String.format("{'title':'%s','author':'%s'}", book.getTitle(), book.getAuthor());
        Book resultBook = instance.list(searchString)[0];

        assertThat("Book excist", resultBook.equals(book) && result, is(true));
    }

    @Test
    public void testAdd_2() {
        Book book = new Book("Best of Python", "noone", 10);
        int quantity = 0;
        BookFacade instance = new BookFacade();
        instance.add(book, quantity);
        int[] result = instance.buy(book);
        List<Integer> resultList = new ArrayList<>();
        for (int i : result) {
            resultList.add(i);
        }
        assertThat("Book cant be bought", resultList, hasItems(1, 0));
    }

    @Test
    public void testAdd_3() {
        BookFacade instance = new BookFacade();
        boolean result = instance.add(null, 0);
        String searchString = String.format("{'title':'%s','author':'%s'}", "Generic", "");
        Book[] books = instance.list(searchString);

        assertThat("Should be two Generic books", books.length, is(2));
    }

    @Test
    public void testAdd_4() {
        BookFacade instance = new BookFacade();
        Book book = new Book("", "", 0);
        boolean result = instance.add(book, 0);
        assertThat("Should not be added", result, is(false));
    }

    /**
     * Test of buy method, of class BookFacade.
     */
    @Test
    public void testBuy() {
        System.out.println("buy");
        Book book0 = new Book("C#", "Olle", 50); //OK
        Book book1 = new Book("Java", "Daniel", 100); //OK
        Book book2 = new Book("Secrets of Java", "Daniel", 150); //Not_excist
        Book book3 = new Book("Java3", "Daniel", 200); //OUT_OF_STOCK
        BookFacade instance = new BookFacade();
        int[] result = instance.buy(book0, book1, book2, book3);

        //convert int[] -> List<Integer>
        List<Integer> resultList = new ArrayList<>();
        for (int i : result) {
            resultList.add(i);
        }
        assertThat("Check data: ", resultList, hasItems(0, 0, 2, 1, 150));
    }

}
