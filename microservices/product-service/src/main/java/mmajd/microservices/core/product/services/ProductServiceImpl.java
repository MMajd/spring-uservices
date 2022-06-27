package mmajd.microservices.core.product.services;

import mmajd.microservices.core.product.presistence.ProductEntity;
import mmajd.microservices.core.product.presistence.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import mmajd.api.core.product.Product;
import mmajd.api.core.product.ProductService;
import mmajd.api.exceptions.InvalidInputException;
import mmajd.api.exceptions.NotFoundException;
import mmajd.util.http.ServiceUtil;

@RestController
public class ProductServiceImpl implements ProductService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

  private final ServiceUtil serviceUtil;

  private final ProductRepository productRepository;

  private final ProductMapper productMapper;


  @Autowired
  public ProductServiceImpl(ServiceUtil serviceUtil, ProductRepository productRepository, ProductMapper productMapper) {
    this.serviceUtil = serviceUtil;
    this.productRepository = productRepository;
    this.productMapper = productMapper;
  }

  @Override
  public Product getProduct(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    ProductEntity entity = productRepository.findByProductId(productId).orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));

    Product p = productMapper.entityToApi(entity);
    p.setServiceAddress(serviceUtil.getServiceAddress());

    LOG.debug("getProduct: found productId: {}", p.getProductId());

    return p;
  }

  @Override
  public Product createProduct(Product body) {
    try {
      ProductEntity entity = productRepository.save(productMapper.apiToEntity(body));

      LOG.debug("createProduct: entity created for productId: {}", body.getProductId());
      return productMapper.entityToApi(entity);
    } catch (DuplicateKeyException ex) {
      throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId());
    }
  }

  @Override
  public void deleteProduct(int productId) {
    LOG.debug("deleteProduct: tries to delete product with productId: {}", productId);
    productRepository.findByProductId(productId).ifPresent(productRepository::delete);
  }
}
