package com.agile.board.repo;

import com.agile.board.domain.Project;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("select p from Project p where p.deleted = false")
    List<Project> findAllActive();

    @Query("select p from Project p where p.id = :id and p.deleted = false")
    Optional<Project> findActiveById(@Param("id") Long id);

    @Query("select p from Project p where p.key = :key and p.deleted = false")
    Optional<Project> findActiveByKey(@Param("key") String key);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Project p set p.deleted = true where p.id = :id")
    int softDeleteById(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Project p set p.deleted = false where p.id = :id")
    int restoreById(@Param("id") Long id);
}
