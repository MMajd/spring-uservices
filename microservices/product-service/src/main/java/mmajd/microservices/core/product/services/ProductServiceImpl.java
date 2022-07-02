package mmajd.microservices.core.product.services;

import mmajd.api.core.product.Product;
import mmajd.api.core.product.ProductService;
import mmajd.api.exceptions.InvalidInputException;
import mmajd.api.exceptions.NotFoundException;
import mmajd.microservices.core.product.presistence.ProductEntity;
import mmajd.microservices.core.product.presistence.ProductRepository;
import mmajd.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static java.util.logging.Level.FINE;

@RestController
public class ProductServiceImpl implements ProductService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

  private final ServiceUtil serviceUtil;

  private final ProductRepository repository;

  private final ProductMapper mapper;


  @Autowired
  public ProductServiceImpl(ServiceUtil serviceUtil, ProductRepository repository, ProductMapper mapper) {
    this.serviceUtil = serviceUtil;
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Mono<Product> getProduct(int productId) {
    if (productId < 1) {
      throw new InvalidInputException(String.format("Invalid productId: %d", productId));
    }

    LOG.debug("Will get product info for id: {}", productId);

    return repository
            .findByProductId(productId)
            .switchIfEmpty(Mono.error(new NotFoundException(String.format("No product found for productId: %d", productId))))
            .log(LOG.getName(), FINE)
            .map(mapper::entityToApi)
            .map(this::setServiceAddress);
  }

  @Override
  public Mono<Product>  createProduct(Product body) {
    if (body.getProductId() < 1) throw new InvalidInputException(String.format("Invalid productId: %d ", body.getProductId()));

    ProductEntity entity = mapper.apiToEntity(body);

    return repository
            .save(entity)
            .log(LOG.getName(), FINE)
            .onErrorMap(
                    DuplicateKeyException.class,
                    ex -> new InvalidInputException(String.format("Duplicate key, Product Id: %d", body.getProductId()))
            )
            .map(mapper::entityToApi);
  }

  @Override
  public Mono<Void> deleteProduct(int productId) {
    if (productId < 1) {
      throw new InvalidInputException(String.format("Invalid productId: %d", productId));
    }

    LOG.debug("deleteProduct: tries to delete product with productId: {}", productId);

    return repository
            .findByProductId(productId)
            .log(LOG.getName(), FINE)
            .map(repository::delete)
            .flatMap(e -> e);
  }


  private Product setServiceAddress(Product e) {
    e.setServiceAddress(serviceUtil.getServiceAddress());
    return e;
  }
}
