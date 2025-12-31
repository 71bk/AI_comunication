package tw.bk.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tw.bk.ai.entity.DocumentChunk;

import java.util.List;

/**
 * Document chunk repository.
 */
@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocument_IdOrderByChunkIndexAsc(Long documentId);

    long countByDocument_Id(Long documentId);
}
