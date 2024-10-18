package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;


@Repository
public interface PostRepository extends CrudRepository<Post, Long> {
    Optional<Post> findById(Long id);

    List<Post> findAll();

    @Query(value = "SELECT * FROM POST WHERE title LIKE %?1%", nativeQuery = true)
    List<Post> findPostByTitle(String keyword);

    @Query(value = "SELECT * FROM POST ORDER BY DATE_POSTED DESC", nativeQuery = true)
    Page<Post> findRecentPosts(Pageable pageable);
}
