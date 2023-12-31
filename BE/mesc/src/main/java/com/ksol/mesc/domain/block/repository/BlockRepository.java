package com.ksol.mesc.domain.block.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ksol.mesc.domain.block.entity.Block;
import com.ksol.mesc.domain.common.EntityState;

public interface BlockRepository extends JpaRepository<Block, Integer> {
	@Modifying
	@Query("update Block b set b.name=:name where b.id=:id")
	void updateBlockName(@Param("id") Integer id, @Param("name") String name);

	@Query("select b from Block b where b.state=:state")
	List<Block> findAllByActive(@Param("state") EntityState state);

	Block save(Block block);

	@Query("select b from Block b where b.id=:id and b.state=:state")
	Optional<Block> findActiveById(@Param("id") Integer id, @Param("state") EntityState state);

	@Modifying
	@Query("update Block b set b.state=:state where b.id=:id")
	void updateState(@Param("id") Integer id, @Param("state") EntityState state);
}
