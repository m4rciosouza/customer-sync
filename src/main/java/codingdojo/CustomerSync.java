package codingdojo;

import codingdojo.models.Customer;
import codingdojo.models.ExternalCustomer;
import codingdojo.models.ShoppingList;
import codingdojo.exceptions.ConflictException;
import codingdojo.repositories.CustomerRespository;
import codingdojo.services.CustomerService;
import codingdojo.services.CustomerMatchesService;
import codingdojo.types.CustomerType;

public class CustomerSync {

    private final CustomerService customerService;

    public CustomerSync(CustomerRespository customerRespository) {
        customerService = new CustomerService(customerRespository);
    }

    public boolean syncExternalCustomer(ExternalCustomer externalCustomer) throws ConflictException {
        boolean created = false;
        CustomerMatchesService customerMatchesService;

        if (externalCustomer.isCompany()) {
            customerMatchesService = loadCompany(externalCustomer);
        } else {
            customerMatchesService = loadPerson(externalCustomer);
        }

        Customer customer = customerMatchesService.getCustomer();

        if (customer == null) {
            customer = new Customer();
            customer.setExternalId(externalCustomer.getExternalId());
            customer.setMasterExternalId(externalCustomer.getExternalId());
        }

        populateFields(externalCustomer, customer);
        updatePreferredStore(externalCustomer, customer);
        updateContactInfo(externalCustomer, customer);

        if (customer.getInternalId() == null) {
            customer = createCustomer(customer);
            created = true;
        } else {
            customer = updateCustomer(customer);
        }

        updateRelations(externalCustomer, customer);

        if (customerMatchesService.hasDuplicates()) {
            for (Customer duplicate : customerMatchesService.getDuplicates()) {
                updateDuplicate(externalCustomer, duplicate);
            }
        }

        return created;
    }

    private void updateRelations(ExternalCustomer externalCustomer, Customer customer) {
        for (ShoppingList consumerShoppingList : externalCustomer.getShoppingLists()) {
            this.customerService.updateShoppingList(customer, consumerShoppingList);
        }
    }

    private Customer updateCustomer(Customer customer) {
        return this.customerService.updateCustomerRecord(customer);
    }

    private void updateDuplicate(ExternalCustomer externalCustomer, Customer duplicate) {
        if (duplicate == null) {
            duplicate = new Customer();
            duplicate.setExternalId(externalCustomer.getExternalId());
            duplicate.setMasterExternalId(externalCustomer.getExternalId());
        }

        duplicate.setName(externalCustomer.getName());

        if (duplicate.getInternalId() == null) {
            createCustomer(duplicate);
        } else {
            updateCustomer(duplicate);
        }
    }

    private void updatePreferredStore(ExternalCustomer externalCustomer, Customer customer) {
        customer.setPreferredStore(externalCustomer.getPreferredStore());
    }

    private Customer createCustomer(Customer customer) {
        return this.customerService.createCustomerRecord(customer);
    }

    private void populateFields(ExternalCustomer externalCustomer, Customer customer) {
        customer.setName(externalCustomer.getName());

        if (externalCustomer.isCompany()) {
            customer.setCompanyNumber(externalCustomer.getCompanyNumber());
            customer.setCustomerType(CustomerType.COMPANY);
        } else {
            customer.setCustomerType(CustomerType.PERSON);
            customer.setBonusPointsBalance(externalCustomer.getBonusPointsBalance());
        }
    }

    private void updateContactInfo(ExternalCustomer externalCustomer, Customer customer) {
        customer.setAddress(externalCustomer.getPostalAddress());
    }

    public CustomerMatchesService loadCompany(ExternalCustomer externalCustomer) throws ConflictException {
        String externalId = externalCustomer.getExternalId();
        String companyNumber = externalCustomer.getCompanyNumber();

        CustomerMatchesService customerMatchesService = customerService.loadCompanyCustomer(externalId, companyNumber);

        if (customerMatchesService.getCustomer() != null && !CustomerType.COMPANY.equals(customerMatchesService.getCustomer().getCustomerType())) {
            throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a company");
        }

        if (CustomerService.EXTERNAL_ID.equals(customerMatchesService.getMatchTerm())) {
            String customerCompanyNumber = customerMatchesService.getCustomer().getCompanyNumber();
            if (!companyNumber.equals(customerCompanyNumber)) {
                customerMatchesService.getCustomer().setMasterExternalId(null);
                customerMatchesService.addDuplicate(customerMatchesService.getCustomer());
                customerMatchesService.setCustomer(null);
                customerMatchesService.setMatchTerm(null);
            }
        } else if (CustomerService.COMPANY_NUMBER.equals(customerMatchesService.getMatchTerm())) {
            String customerExternalId = customerMatchesService.getCustomer().getExternalId();
            if (customerExternalId != null && !externalId.equals(customerExternalId)) {
                throw new ConflictException("Existing customer for externalCustomer " + companyNumber +
                        " doesn't match external id " + externalId + " instead found " + customerExternalId);
            }

            Customer customer = customerMatchesService.getCustomer();
            customer.setExternalId(externalId);
            customer.setMasterExternalId(externalId);
            customerMatchesService.addDuplicate(null);
        }

        return customerMatchesService;
    }

    public CustomerMatchesService loadPerson(ExternalCustomer externalCustomer) throws ConflictException {
        String externalId = externalCustomer.getExternalId();
        CustomerMatchesService customerMatchesService = customerService.loadPersonCustomer(externalId);

        if (customerMatchesService.getCustomer() == null) {
            return customerMatchesService;
        }

        if (!CustomerType.PERSON.equals(customerMatchesService.getCustomer().getCustomerType())) {
            throw new ConflictException("Existing customer for externalCustomer " +
                    externalId + " already exists and is not a person");
        }

        if (!CustomerService.EXTERNAL_ID.equals(customerMatchesService.getMatchTerm())) {
            Customer customer = customerMatchesService.getCustomer();
            customer.setExternalId(externalId);
            customer.setMasterExternalId(externalId);
        }

        return customerMatchesService;
    }
}
