package com.ksol.mesc.domain.group.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ksol.mesc.domain.group.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Integer> {
	@Modifying
	@Query("update Group g set g.sequence=:sequence where g.id=:groupId")
	void updateGroupSequence(@Param("groupId") Integer groupId, @Param("sequence") Integer sequence);

	@Query("select g from Group g where g.userId=:userId")
	List<Group> findByUserId(@Param("userId") Integer userId);

	@Query("select g from Group g where g.id=:groupId and g.userId=:userId")
	Optional<Group> findByUserAndGroup(@Param("groupId") Integer groupId, @Param("userId") Integer userId);

}