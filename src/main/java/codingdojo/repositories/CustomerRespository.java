package codingdojo.repositories;

import codingdojo.models.Customer;
import codingdojo.models.ShoppingList;

public interface CustomerRespository {

    Customer update(Customer customer);

    Customer create(Customer customer);

    Customer updateShoppingList(ShoppingList consumerShoppingList);

    Customer findByExternalId(String externalId);

    Customer findByMasterExternalId(String externalId);

    Customer findByCompanyNumber(String companyNumber);
}
