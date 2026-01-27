package org.example.mopl.content.entity.repository;

import org.example.mopl.content.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagCommandReposiotry extends JpaRepository<Tag, Long> {

}
