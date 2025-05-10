import org.example.kursovoi.classes.UserSession;
import org.example.kursovoi.db_classes.Buyer;
import org.example.kursovoi.db_classes.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleTest {
    private Buyer buyer;
    private UserSession userSession;


    @BeforeEach
    public void setUp() throws Exception {
        this.buyer = new Buyer();
        this.userSession = new UserSession();
        userSession.setBuyer(buyer);

    }


    @Test
    void testBuyerInputEmail() {
        assertEquals(0, this.buyer.checkInputType("john@example.com"));
    }

    @Test
    void testBuyerInputPhone() {
        assertEquals(1, this.buyer.checkInputType("8432843783"));
    }

    @Test
    void testBuyerInputTrash() {
        assertEquals(-1, this.buyer.checkInputType("843243783"));
    }

    @Test
    public void testAddProductToCart() {
        this.buyer.clearCart();
        Product product = new Product(1L, "Test Product", 100L);
        this.buyer.getCart().add(product);
        assertEquals(1, this.buyer.getCart().size());
        assertEquals("Test Product", this.buyer.getCart().get(0).getProductName());

    }

    @Test
    public void testCalculateTotalSum() {
        this.buyer.clearCart();
        Product product1 = new Product(1L, "Product1", 100L);
        Product product2 = new Product(2L, "Product2", 200L);
        buyer.getCart().add(product1);
        buyer.getCart().add(product2);
        assertEquals(300L, buyer.calculateTotalSum());
    }

    @Test
    public void testUserAuthorization() {
        this.buyer.setEmail("user@user.ru");
        this.buyer.setPassword("user");
        this.buyer.login(this.userSession);
        assertNotEquals(null, this.userSession.getToken());
        assertEquals(1L,this.buyer.getIdRole());
    }


}