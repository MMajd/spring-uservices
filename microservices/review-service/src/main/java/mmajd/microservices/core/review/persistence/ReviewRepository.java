package mmajd.microservices.core.review.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    @Transactional(readOnly = true)
    List<ReviewEntity> findByProductId(int productId);
}
