package mu.majd.microservices.core.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import mu.majd.api.core.product.Product;
import mu.majd.api.core.product.ProductService;
import mu.majd.api.exceptions.InvalidInputException;
import mu.majd.api.exceptions.NotFoundException;
import mu.majd.util.http.ServiceUtil;

@RestController
public class ProductServiceImpl implements ProductService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ServiceUtil serviceUtil;

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil)  {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Product getProduct(int productId) {
        LOG.debug("/Product return the found product for productId={}", productId);

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        if (productId == 13) {
            throw new NotFoundException("No product for this id" + productId);
        }

        return new Product(productId, "name-" + productId, 124, serviceUtil.getServiceAddress());
    }
}