package codingdojo;

import codingdojo.models.Address;
import codingdojo.models.Customer;
import codingdojo.models.ExternalCustomer;
import codingdojo.models.ShoppingList;
import codingdojo.exceptions.ConflictException;
import codingdojo.repositories.CustomerRespository;
import codingdojo.types.CustomerType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomerSyncTest {

    @Test
    public void testSyncCompanyByExternalId() {
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);

        CustomerRespository db = mock(CustomerRespository.class);
        when(db.findByExternalId(externalId)).thenReturn(customer);
        when(db.update(any(Customer.class))).thenReturn(customer);
        CustomerSync sut = new CustomerSync(db);

        // ACT
        boolean created = sut.syncExternalCustomer(externalCustomer);

        // ASSERT
        ArgumentCaptor<Customer> argument = ArgumentCaptor.forClass(Customer.class);
        verify(db, atLeastOnce()).update(argument.capture());
        Customer updatedCustomer = argument.getValue();

        assertFalse(created);
        assertEquals(externalCustomer.getName(), updatedCustomer.getName());
        assertEquals(externalCustomer.getExternalId(), updatedCustomer.getExternalId());
        assertNull(updatedCustomer.getMasterExternalId());
        assertEquals(externalCustomer.getCompanyNumber(), updatedCustomer.getCompanyNumber());
        assertEquals(externalCustomer.getPostalAddress(), updatedCustomer.getAddress());
        assertEquals(externalCustomer.getShoppingLists(), updatedCustomer.getShoppingLists());
        assertEquals(CustomerType.COMPANY, updatedCustomer.getCustomerType());
        assertNull(updatedCustomer.getPreferredStore());
    }

    @Test
    public void testSyncNewCompanyByExternalId() {
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);

        CustomerRespository db = mock(CustomerRespository.class);
        when(db.findByExternalId(externalId)).thenReturn(null);
        when(db.create(any(Customer.class))).thenReturn(customer);
        CustomerSync sut = new CustomerSync(db);

        // ACT
        boolean created = sut.syncExternalCustomer(externalCustomer);

        // ASSERT
        ArgumentCaptor<Customer> argument = ArgumentCaptor.forClass(Customer.class);
        verify(db, atLeastOnce()).update(argument.capture());
        Customer updatedCustomer = argument.getValue();

        assertTrue(created);
        assertEquals(externalCustomer.getExternalId(), updatedCustomer.getExternalId());
        assertNull(updatedCustomer.getMasterExternalId());
        assertEquals(externalCustomer.getCompanyNumber(), updatedCustomer.getCompanyNumber());
        assertEquals(externalCustomer.getShoppingLists(), updatedCustomer.getShoppingLists());
        assertEquals(CustomerType.COMPANY, updatedCustomer.getCustomerType());
        assertNull(updatedCustomer.getPreferredStore());
    }

    @Test
    public void testSyncPersonByExternalId() {
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalPerson();
        externalCustomer.setExternalId(externalId);

        Customer customer = createPerson(externalCustomer);
        customer.setExternalId(externalId);

        CustomerRespository db = mock(CustomerRespository.class);
        when(db.findByExternalId(externalId)).thenReturn(customer);
        when(db.update(any(Customer.class))).thenReturn(customer);
        CustomerSync sut = new CustomerSync(db);

        // ACT
        boolean created = sut.syncExternalCustomer(externalCustomer);

        // ASSERT
        ArgumentCaptor<Customer> argument = ArgumentCaptor.forClass(Customer.class);
        verify(db, atLeastOnce()).update(argument.capture());
        when(db.update(any(Customer.class))).thenReturn(customer);
        Customer updatedCustomer = argument.getValue();

        assertFalse(created);
        assertEquals(externalCustomer.getName(), updatedCustomer.getName());
        assertEquals(externalCustomer.getExternalId(), updatedCustomer.getExternalId());
        assertNull(updatedCustomer.getMasterExternalId());
        assertEquals(externalCustomer.getPostalAddress(), updatedCustomer.getAddress());
        assertEquals(externalCustomer.getShoppingLists(), updatedCustomer.getShoppingLists());
        assertEquals(CustomerType.PERSON, updatedCustomer.getCustomerType());
        assertNull(updatedCustomer.getPreferredStore());
    }

    @Test
    public void testSyncNewPersonByExternalId() {
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalPerson();
        externalCustomer.setExternalId(externalId);

        Customer customer = createPerson(externalCustomer);
        customer.setExternalId(externalId);

        CustomerRespository db = mock(CustomerRespository.class);
        when(db.findByExternalId(externalId)).thenReturn(null);
        when(db.create(any(Customer.class))).thenReturn(customer);
        CustomerSync sut = new CustomerSync(db);

        // ACT
        boolean created = sut.syncExternalCustomer(externalCustomer);

        // ASSERT
        ArgumentCaptor<Customer> argument = ArgumentCaptor.forClass(Customer.class);
        verify(db, atLeastOnce()).update(argument.capture());
        when(db.update(any(Customer.class))).thenReturn(customer);
        Customer updatedCustomer = argument.getValue();

        assertTrue(created);
        assertEquals(externalCustomer.getExternalId(), updatedCustomer.getExternalId());
        assertNull(updatedCustomer.getMasterExternalId());
        assertEquals(externalCustomer.getShoppingLists(), updatedCustomer.getShoppingLists());
        assertEquals(CustomerType.PERSON, updatedCustomer.getCustomerType());
        assertNull(updatedCustomer.getPreferredStore());
    }

    @Test
    public void testSyncShoppingLists() {
        String externalId = "12345";
        List<ShoppingList> newShoppingList = Collections.singletonList(new ShoppingList("eyeliner", "blusher"));

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);
        customer.setShoppingLists(newShoppingList);

        CustomerRespository db = mock(CustomerRespository.class);
        when(db.findByExternalId(externalId)).thenReturn(customer);
        when(db.update(any(Customer.class))).thenReturn(customer);
        CustomerSync sut = new CustomerSync(db);

        // ACT
        boolean created = sut.syncExternalCustomer(externalCustomer);

        // ASSERT
        ArgumentCaptor<Customer> argument = ArgumentCaptor.forClass(Customer.class);
        verify(db, atLeastOnce()).update(argument.capture());
        Customer updatedCustomer = argument.getValue();

        assertFalse(created);
        assertEquals(2, updatedCustomer.getShoppingLists().size());
        assertEquals(newShoppingList.get(0).getProducts(), updatedCustomer.getShoppingLists().get(0).getProducts());
        assertEquals(externalCustomer.getShoppingLists().get(0).getProducts(), updatedCustomer.getShoppingLists().get(1).getProducts());
    }

    @Test
    public void testSyncPersonBonusPointsByExternalId() {
        String externalId = "12345";
        Integer bonusPointsBalance = 100;

        ExternalCustomer externalCustomer = createExternalPerson();
        externalCustomer.setExternalId(externalId);
        externalCustomer.setBonusPointsBalance(bonusPointsBalance);

        Customer customer = createPerson(externalCustomer);
        customer.setExternalId(externalId);

        CustomerRespository db = mock(CustomerRespository.class);
        when(db.findByExternalId(externalId)).thenReturn(customer);
        when(db.update(any(Customer.class))).thenReturn(customer);
        CustomerSync sut = new CustomerSync(db);

        // ACT
        boolean created = sut.syncExternalCustomer(externalCustomer);

        // ASSERT
        ArgumentCaptor<Customer> argument = ArgumentCaptor.forClass(Customer.class);
        verify(db, atLeastOnce()).update(argument.capture());
        Customer updatedCustomer = argument.getValue();

        assertFalse(created);
        assertEquals(CustomerType.PERSON, updatedCustomer.getCustomerType());
        assertEquals(bonusPointsBalance, updatedCustomer.getBonusPointsBalance());
    }

    @Test
    public void testConflictExceptionWhenNotCompany() {
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);
        customer.setCustomerType(CustomerType.PERSON);

        CustomerRespository db = mock(CustomerRespository.class);
        when(db.findByExternalId(externalId)).thenReturn(customer);
        when(db.update(any(Customer.class))).thenReturn(customer);
        CustomerSync sut = new CustomerSync(db);

        // ACT
        ConflictException thrown = Assertions.assertThrows(ConflictException.class, () -> {
            sut.syncExternalCustomer(externalCustomer);
        });

        // ASSERT
        Assertions.assertEquals(
                "Existing customer for externalCustomer 12345 already exists and is not a company",
                thrown.getMessage()
        );
    }

    @Test
    public void testConflictExceptionWhenNotPerson() {
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalPerson();
        externalCustomer.setExternalId(externalId);

        Customer customer = createPerson(externalCustomer);
        customer.setExternalId(externalId);
        customer.setCustomerType(CustomerType.COMPANY);

        CustomerRespository db = mock(CustomerRespository.class);
        when(db.findByExternalId(externalId)).thenReturn(customer);
        when(db.update(any(Customer.class))).thenReturn(customer);
        CustomerSync sut = new CustomerSync(db);

        // ACT
        ConflictException thrown = Assertions.assertThrows(ConflictException.class, () -> {
            sut.syncExternalCustomer(externalCustomer);
        });

        // ASSERT
        Assertions.assertEquals(
                "Existing customer for externalCustomer 12345 already exists and is not a person",
                thrown.getMessage()
        );
    }

    private ExternalCustomer createExternalCompany() {
        ExternalCustomer externalCustomer = new ExternalCustomer();
        externalCustomer.setExternalId("12345");
        externalCustomer.setName("Acme Inc.");
        externalCustomer.setAddress(new Address("123 main st", "Helsingborg", "SE-123 45"));
        externalCustomer.setCompanyNumber("470813-8895");
        externalCustomer.setShoppingLists(Collections.singletonList(new ShoppingList("lipstick", "blusher")));

        return externalCustomer;
    }

    private Customer createCustomerWithSameCompanyAs(ExternalCustomer externalCustomer) {
        Customer customer = new Customer();
        customer.setCompanyNumber(externalCustomer.getCompanyNumber());
        customer.setCustomerType(CustomerType.COMPANY);
        customer.setInternalId("45435");

        return customer;
    }

    private ExternalCustomer createExternalPerson() {
        ExternalCustomer externalCustomer = new ExternalCustomer();
        externalCustomer.setExternalId("12345");
        externalCustomer.setName("Acme Inc.");
        externalCustomer.setAddress(new Address("123 main st", "Helsingborg", "SE-123 45"));
        externalCustomer.setShoppingLists(Arrays.asList(new ShoppingList("lipstick", "blusher")));

        return externalCustomer;
    }

    private Customer createPerson(ExternalCustomer externalCustomer) {
        Customer customer = new Customer();
        customer.setCustomerType(CustomerType.PERSON);
        customer.setInternalId("45435");

        return customer;
    }

}
