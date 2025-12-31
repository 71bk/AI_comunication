package tw.bk.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tw.bk.ai.entity.UsageLog;

import java.util.List;

/**
 * Usage log repository.
 */
@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {

    List<UsageLog> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<UsageLog> findByChat_IdOrderByCreatedAtDesc(Long chatId);
}
