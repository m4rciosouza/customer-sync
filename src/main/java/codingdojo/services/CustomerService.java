package codingdojo.services;

import codingdojo.models.Customer;
import codingdojo.models.ShoppingList;
import codingdojo.repositories.CustomerRespository;

public class CustomerService {

    public static final String EXTERNAL_ID = "ExternalId";
    public static final String COMPANY_NUMBER = "CompanyNumber";
    private final CustomerRespository customerRespository;

    public CustomerService(CustomerRespository customerRespository) {
        this.customerRespository = customerRespository;
    }

    public CustomerMatchesService loadCompanyCustomer(String externalId, String companyNumber) {
        CustomerMatchesService matches = new CustomerMatchesService();
        Customer matchByExternalId = this.customerRespository.findByExternalId(externalId);

        if (matchByExternalId != null) {
            matches.setCustomer(matchByExternalId);
            matches.setMatchTerm(EXTERNAL_ID);
            Customer matchByMasterId = this.customerRespository.findByMasterExternalId(externalId);
            if (matchByMasterId != null) {
                matches.addDuplicate(matchByMasterId);
            }
        } else {
            Customer matchByCompanyNumber = this.customerRespository.findByCompanyNumber(companyNumber);
            if (matchByCompanyNumber != null) {
                matches.setCustomer(matchByCompanyNumber);
                matches.setMatchTerm(COMPANY_NUMBER);
            }
        }

        return matches;
    }

    public CustomerMatchesService loadPersonCustomer(String externalId) {
        CustomerMatchesService matches = new CustomerMatchesService();
        Customer matchByPersonalNumber = this.customerRespository.findByExternalId(externalId);
        matches.setCustomer(matchByPersonalNumber);

        if (matchByPersonalNumber != null) {
            matches.setMatchTerm(EXTERNAL_ID);
        }

        return matches;
    }

    public Customer updateCustomerRecord(Customer customer) {
        return customerRespository.update(customer);
    }

    public Customer createCustomerRecord(Customer customer) {
        return customerRespository.create(customer);
    }

    public Customer updateShoppingList(Customer customer, ShoppingList consumerShoppingList) {
        customer.addShoppingList(consumerShoppingList);
        customerRespository.updateShoppingList(consumerShoppingList);

        return customerRespository.update(customer);
    }

}
