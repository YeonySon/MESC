package com.ksol.mesc.domain.component.type.card.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ksol.mesc.domain.component.type.card.Card;

public interface CardRepository extends JpaRepository<Card, Integer> {
	//blockid와 연결된 카드 조회
	@Query("select c from Card c where c.block.id=:blockId")
	List<Card> findByBlockId(@Param("blockId") Integer blockId);
}
