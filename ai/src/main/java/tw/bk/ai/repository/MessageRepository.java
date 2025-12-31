package tw.bk.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tw.bk.ai.entity.Message;

import java.util.List;

/**
 * 訊息 Repository
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChat_IdOrderByCreatedAtAsc(Long chatId);

    long countByChat_Id(Long chatId);
}
