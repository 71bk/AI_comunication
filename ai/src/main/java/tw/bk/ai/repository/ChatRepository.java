package tw.bk.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tw.bk.ai.entity.Chat;

import java.util.List;
import java.util.Optional;

/**
 * 對話 Repository
 */
@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findByUser_IdOrderByUpdatedAtDesc(Long userId);

    Optional<Chat> findByIdAndUser_Id(Long id, Long userId);

    @Query("SELECT c FROM Chat c LEFT JOIN FETCH c.messages WHERE c.id = :chatId AND c.user.id = :userId")
    Optional<Chat> findByIdAndUserIdWithMessages(Long chatId, Long userId);

    boolean existsByIdAndUser_Id(Long id, Long userId);
}
