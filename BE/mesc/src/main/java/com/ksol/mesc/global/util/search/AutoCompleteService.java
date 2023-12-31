package com.ksol.mesc.global.util.search;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoCompleteService {

	private static String AUTOCOMPLETE_KEY = "";
	private static final String UNICODE_MAX_CHAR = "\ufff0";
	private static final String WORD_TERMINATOR = "*";

	private final RedisTemplate<String, String> redisTemplate;

	public List<String> getSql(String prefix) {
		AUTOCOMPLETE_KEY = "query";
		return autocomplete(prefix, AUTOCOMPLETE_KEY);
	}

	public List<String> getTable(String prefix) {
		AUTOCOMPLETE_KEY = "table";
		return autocompleteSuggestion(prefix, AUTOCOMPLETE_KEY);
	}

	public List<String> getColumn(String prefix) {
		AUTOCOMPLETE_KEY = "column2";
		return autocompleteSuggestion(prefix, AUTOCOMPLETE_KEY);
	}

	public List<String> autocomplete(String prefix, String AUTOCOMPLETE_KEY) {
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

		Range<String> range = Range.from(Range.Bound.inclusive(prefix))
			.to(Range.Bound.exclusive(prefix + UNICODE_MAX_CHAR));

		Set<String> results = zSetOperations.rangeByLex(AUTOCOMPLETE_KEY, range);

		// 결과를 필터링하여 *로 끝나는 항목만 반환하고 리스트로 변환
		List<String> autocompleteList = results.stream()
			.filter(word -> word.endsWith(WORD_TERMINATOR))
			.map(word -> word.substring(0, word.length() - 1)) // *를 제거
			.collect(Collectors.toList());
		Collections.sort(autocompleteList);
		log.info("autoList : {}", autocompleteList);
		return autocompleteList;
	}

	public List<String> autocompleteSuggestion(String prefix, String AUTOCOMPLETE_KEY) {
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

		// AUTOCOMPLETE_KEY를 사용하여 모든 항목 검색
		Set<String> results = zSetOperations.rangeByLex(AUTOCOMPLETE_KEY, Range.unbounded());

		// 결과 필터링: 선택적으로 prefix로 시작하는 항목만 반환
		List<String> autocompleteList = results.stream()
											   .filter(word -> word.startsWith(prefix) && word.endsWith(WORD_TERMINATOR))
											   .map(word -> word.substring(0, word.length() - 1)) // *를 제거
											   .collect(Collectors.toList());

		Collections.sort(autocompleteList);
		log.info("autoList : {}", autocompleteList);
		return autocompleteList;
	}


}
