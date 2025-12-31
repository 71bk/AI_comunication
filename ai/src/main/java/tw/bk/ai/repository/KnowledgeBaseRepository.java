package tw.bk.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tw.bk.ai.entity.KnowledgeBase;

import java.util.List;
import java.util.Optional;

/**
 * Knowledge base repository.
 */
@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    List<KnowledgeBase> findByUser_IdOrderByUpdatedAtDesc(Long userId);

    Optional<KnowledgeBase> findByIdAndUser_Id(Long id, Long userId);
}
