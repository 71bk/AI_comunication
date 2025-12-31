package tw.bk.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tw.bk.ai.entity.Document;

import java.util.List;
import java.util.Optional;

/**
 * Document repository.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByKnowledgeBase_IdOrderByUpdatedAtDesc(Long knowledgeBaseId);

    Optional<Document> findByIdAndKnowledgeBase_Id(Long id, Long knowledgeBaseId);
}
