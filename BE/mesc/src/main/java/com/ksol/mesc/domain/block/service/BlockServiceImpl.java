package com.ksol.mesc.domain.block.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksol.mesc.domain.api.service.ApiService;
import com.ksol.mesc.domain.block.dto.request.BlockInfoDto;
import com.ksol.mesc.domain.block.dto.request.BlockReqDto;
import com.ksol.mesc.domain.block.dto.request.CardReqDto;
import com.ksol.mesc.domain.block.dto.request.ComponentListDto;
import com.ksol.mesc.domain.block.dto.response.BlockInfoRes;
import com.ksol.mesc.domain.block.dto.response.BlockRes;
import com.ksol.mesc.domain.block.entity.Block;
import com.ksol.mesc.domain.block.repository.BlockRepository;
import com.ksol.mesc.domain.card.dto.request.CardReq;
import com.ksol.mesc.domain.card.dto.rsponse.CardRes;
import com.ksol.mesc.domain.card.entity.Card;
import com.ksol.mesc.domain.card.entity.CardType;
import com.ksol.mesc.domain.card.repository.CardRepository;
import com.ksol.mesc.domain.common.EntityState;
import com.ksol.mesc.domain.common.dto.response.JsonResponse;
import com.ksol.mesc.domain.component.dto.request.ComponentReq;
import com.ksol.mesc.domain.component.dto.response.ComponentRes;
import com.ksol.mesc.domain.component.entity.Component;
import com.ksol.mesc.domain.component.entity.ComponentType;
import com.ksol.mesc.domain.component.repository.ComponentRepository;
import com.ksol.mesc.domain.component.type.button.dto.ButtonRes;
import com.ksol.mesc.domain.component.type.button.entity.Button;
import com.ksol.mesc.domain.component.type.button.repository.ButtonRepository;
import com.ksol.mesc.domain.component.type.checkbox.dto.response.CheckboxRes;
import com.ksol.mesc.domain.component.type.checkbox.entity.Checkbox;
import com.ksol.mesc.domain.component.type.checkbox.repository.CheckboxRepository;
import com.ksol.mesc.domain.component.type.dropdown.dto.response.DropdownRes;
import com.ksol.mesc.domain.component.type.dropdown.entity.Dropdown;
import com.ksol.mesc.domain.component.type.dropdown.repository.DropdownRepository;
import com.ksol.mesc.domain.component.type.values.dto.ValuesRes;
import com.ksol.mesc.domain.component.type.values.entity.ComponentValue;
import com.ksol.mesc.domain.component.type.values.repository.ValuesRepository;
import com.ksol.mesc.domain.log.service.LogSerivce;
import com.ksol.mesc.domain.section.Section;
import com.ksol.mesc.domain.section.repository.SectionRepository;
import com.ksol.mesc.domain.user.service.UserServiceImpl;
import com.ksol.mesc.global.config.jwt.JwtAuthenticationFilter;
import com.ksol.mesc.global.error.exception.EntityNotFoundException;
import com.ksol.mesc.global.error.exception.InvalidValueException;
import com.ksol.mesc.global.error.exception.MesServerException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockServiceImpl implements BlockService {
	//component repository
	private final BlockRepository blockRepository;
	private final CardRepository cardRepository;
	private final ComponentRepository componentRepository;
	private final ButtonRepository buttonRepository;
	private final CheckboxRepository checkboxRepository;
	private final DropdownRepository dropdownRepository;
	private final ValuesRepository valuesRepository;
	private final SectionRepository sectionRepository;

	//userService
	private final UserServiceImpl userService;
	private final LogSerivce logSerivce;
	private final ApiService apiService;

	//export lib
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final WebClient webClient;

	//전체 블록 조회
	@Override
	public List<BlockRes> selectAllBlock() {
		List<Block> blockList = blockRepository.findAllByActive(EntityState.ACTIVE);
		return blockList.stream().map(BlockRes::toResponse).collect(Collectors.toList());
	}

	//블록 조회
	@Override
	public LinkedHashMap<String, Object> selectBlockInfo(Integer blockId, CardReqDto cardReqDto, Integer userId) {
		//블록 조회
		Block block = blockRepository.findActiveById(blockId, EntityState.ACTIVE)
			.orElseThrow(() -> new EntityNotFoundException("Active Block Not Found"));
		LinkedHashMap<String, Object> objMap = new LinkedHashMap<>();

		objMap.put("blockId", block.getId());
		objMap.put("blockName", block.getName());
		objMap.put("isPossible", block.getIsPossible() ? true : false);

		//블록과 연결된 카드 조회
		List<Card> cardList = cardRepository.findByBlockId(blockId, EntityState.ACTIVE);

		//카드 정보 저장
		List<LinkedHashMap<String, Object>> cardMapList = new ArrayList<>();

		//카드 조회
		if (cardList != null) {
			for (Card card : cardList) {
				LinkedHashMap<String, Object> cardMap = selectCardByType(card, cardReqDto, userId);
				if (cardMap.get("result") != null) {
					objMap.put("isPossible", !(Boolean)cardMap.get("result"));
					cardMap.remove("result");
				}
				cardMapList.add(cardMap);
			}
		}

		objMap.put("cardList", cardMapList);

		// 바로가기 버튼 섹션 조회
		List<Section> byBlockId = sectionRepository.findByBlockId(blockId);

		Integer sectionId = 0;
		if (!byBlockId.isEmpty()) {
			sectionId = byBlockId.get(0).getSectionType();
		}
		objMap.put("section", sectionId);
		return objMap;
	}

	//관리자 블록 조회
	public BlockInfoRes selectBlockByAdmin(Integer blockId) {
		//블록 조회
		Block block = blockRepository.findActiveById(blockId, EntityState.ACTIVE)
			.orElseThrow(() -> new EntityNotFoundException("Active Block Not Found"));
		BlockInfoRes blockInfoRes = new BlockInfoRes();

		blockInfoRes.setBlockInfo(BlockRes.toResponse(block));
		// LinkedHashMap<String, Object> objMap = new LinkedHashMap<>();

		//카드 조회 + 컴포넌트 조회
		List<Card> cardList = cardRepository.findByBlockId(blockId, EntityState.ACTIVE);
		List<CardRes> cardResList = new ArrayList<>();
		if (cardList != null) {
			for (Card card : cardList) {
				List<Component> componentList = componentRepository.findByCard(card, EntityState.ACTIVE);
				List<ComponentRes> components = selectComponentByAdmin(componentList);
				cardResList.add(CardRes.toResponse(card, components));
			}
		}
		blockInfoRes.setCardResList(cardResList);

		return blockInfoRes;
	}

	//component type에 따른 조회
	public List<ComponentRes> selectComponentByAdmin(List<Component> componentList) {
		List<ComponentRes> componentResList = new ArrayList<>();

		for (Component component : componentList) {
			ComponentType componentType = component.getComponentType();

			switch (componentType) {
				case BU:
					Optional<Button> button = buttonRepository.findById(component.getLinkId());
					if (button.isEmpty())
						break;
					componentResList.add(ComponentRes.toResponse(component, ButtonRes.toResponse(button.get())));
					break;
				case DD:
					Optional<Dropdown> dropdownOpt = dropdownRepository.findById(component.getLinkId());
					if (dropdownOpt.isEmpty())
						break;
					Dropdown dropdown = dropdownOpt.get();
					List<ComponentValue> valuesList = valuesRepository.findByDropdown(dropdown, EntityState.ACTIVE);
					List<ValuesRes> valuesResList = valuesList.stream()
						.map(ValuesRes::toResponse)
						.collect(Collectors.toList());

					componentResList.add(
						ComponentRes.toResponse(component, DropdownRes.toResponse(dropdown, valuesResList)));
					break;
				default:
					throw new IllegalArgumentException("Invalid ComponentType");

			}
		}

		return componentResList;
	}

	//card type에 따른 조회
	public LinkedHashMap<String, Object> selectCardByType(Card card, CardReqDto cardReqDto, Integer myId) {
		//카드 정보 저장
		LinkedHashMap<String, Object> cardMap = new LinkedHashMap<>();
		CardType cardType = card.getCardType();

		cardMap.put("cardId", card.getId());
		cardMap.put("cardType", card.getCardType());
		cardMap.put("cardName", card.getName());
		cardMap.put("content", card.getContent());
		String content = null;

		switch (cardType) {
			case DT:    //dynamic Text
				content = card.getContent();
				String title = Optional.ofNullable(cardReqDto.getTitle()).orElse("");
				content = content.replace("{title}", title);
				// Map<String, String> map = cardReqDto.getVariables();
				// if (map != null) {
				// 	for (String key : map.keySet()) {
				// 		content = content.replace("{" + key + "}", map.get(key));
				// 	}
				// }
				cardMap.put("cardType", CardType.TX);
				cardMap.put("content", content);
				break;
			case DTX:    //dynamic Text
				content = card.getContent();
				cardMap.put("content", getDynamicString(content, card.getContentKey()));
				cardMap.put("cardType", CardType.TX);
				break;
			case TA:    //공정 Table
				if (cardReqDto.getActionId() == null) {
					break;
				}
				LinkedHashMap<String, Object> tableInfo = (LinkedHashMap<String, Object>)requestPostToMes(
					"/worker/data/",
					cardReqDto, cardType);

				cardMap.put("title", cardReqDto.getTitle());
				cardMap.put("labels", tableInfo.get("label"));
				tableInfo.remove("label");
				cardMap.put("table", tableInfo);

				break;
			// case STA:    //단일 Table
			// 	cardMap.put("singleTable", requestPostToMes("/developer/data", cardReqDto, cardType));
			// 	break;
			case QU:    //select 쿼리 입력(단일 Table, 직접 select 쿼리 입력)
				LinkedHashMap<String, Object> tableByQuery = apiService.getTableByQuery(cardReqDto.getQuery(),
					1, cardReqDto.getQueryList());
				// LinkedHashMap<String, Object> tableByQuery = apiService.getTableByQuery(cardReqDto.getQuery(), 1,
				// 	cardReqDto.getQueryList());
				Boolean result = (Boolean)tableByQuery.get("result");
				cardMap.put("result", result);
				if (result) {
					tableByQuery.remove("result");
					cardMap.put("title", tableByQuery.get("title"));
					cardMap.put("table", tableByQuery);
				} else {
					cardMap.putAll(tableByQuery);
					cardMap.put("cardType", CardType.TX);
				}
				break;
			case QR:    //insert, update, delete 수행 결과 미리보기
				String query = cardReqDto.getQuery();
				// LinkedHashMap<String, Object> tableByQueryRollback = apiService.getTableByQueryRollback(query);
				LinkedHashMap<String, Object> tableByQueryRollback = apiService.getTableByQueryRollback(query, 1);
				result = (Boolean)tableByQueryRollback.get("result");
				cardMap.put("result", result);
				tableByQueryRollback.remove("result");
				if (result) {
					// tableByQueryRollback.remove("tableList");
					if ((Integer)tableByQueryRollback.get("rowCnt") == 0) {
						cardMap.put("content", "해당 조건을 만족하는 데이터가 존재하지 않습니다.");
						cardMap.put("cardType", "TX");
					} else {
						List<String> tableList = (List<String>)tableByQueryRollback.get("tableList");
						log.info("table List : {}", tableList);
						if (tableList != null) {
							cardMap.put("title", tableList.get(0));
						}
						cardMap.put("table", tableByQueryRollback);
					}
				} else {
					cardMap.putAll(tableByQueryRollback);
				}
				break;
			case QTX:    // insert,update,delete 결과0

				cardMap.putAll(apiService.getCountsByQuery(cardReqDto.getQuery()));
				break;
			case RE:    //보고
				LinkedHashMap<String, List<LinkedHashMap<String, Object>>> m = (LinkedHashMap<String, List<LinkedHashMap<String, Object>>>)userService.selectAllUser();
				List<Object> userListWithoutMe = new ArrayList<>();
				if (m != null) {
					List<LinkedHashMap<String, Object>> userList = m.get("userList");
					userList.forEach(user -> {
						Integer userId = (Integer)user.get("userId");

						if (userId != myId) {
							userListWithoutMe.add(user);
						}
					});
				}
				cardMap.put("userList", userListWithoutMe);
				break;
			case LO:    //로그
				String keyword = cardReqDto.getKeyword();
				String date = cardReqDto.getDate();
				List<String> levelList = cardReqDto.getLevelList();
				log.info("keyword : {}, date : {}, levelList : {}", keyword, date, levelList);
				String command = logSerivce.getCommand(keyword, date, levelList);
				String logs = logSerivce.getLogs(command);
				if (logs.isBlank()) {
					cardMap.put("content", "조회된 결과가 없습니다.");
					cardMap.put("cardType", "TX");
				} else {
					cardMap.put("command", command);
					cardMap.put("logs", logs);
				}
				break;
			case CH, CH1, CH2:// 일반 챗봇(버튼 0개, 버튼 1개, 버튼 2개)
				cardMap.put("title", card.getName());
				break;
			// default:
			// 	throw new IllegalArgumentException("Invalid ComponentType");
		}

		//component 조회
		List<Component> componentList = componentRepository.findByCard(card, EntityState.ACTIVE);
		// log.debug("cardType : {}, componentList : {}", cardType, componentList);
		cardMap.putAll(selectComponentByType(componentList));

		return cardMap;
	}

	//component type에 따른 조회
	public LinkedHashMap<String, List<Object>> selectComponentByType(List<Component> componentList) {
		LinkedHashMap<String, List<Object>> compMap = new LinkedHashMap<>();
		String[] cType = {"button", "checkbox", "dropdown"};
		compMap.put("button", new ArrayList<>());
		compMap.put("checkbox", new ArrayList<>());
		compMap.put("dropdown", new ArrayList<>());

		for (Component component : componentList) {
			ComponentType componentType = component.getComponentType();
			switch (componentType) {
				case BU:
					Optional<Button> button = buttonRepository.findById(component.getLinkId());
					if (button.isEmpty())
						break;
					compMap.get("button").add(ButtonRes.toResponse(button.get()));
					break;
				case CB:
					Optional<Checkbox> checkbox = checkboxRepository.findById(component.getLinkId());
					if (checkbox.isEmpty())
						break;
					compMap.get("checkbox").add(CheckboxRes.toResponse(checkbox.get()));
					break;
				case DD:
					Optional<Dropdown> dropdownOpt = dropdownRepository.findById(component.getLinkId());
					if (dropdownOpt.isEmpty())
						break;
					Dropdown dropdown = dropdownOpt.get();
					List<ComponentValue> valuesList = valuesRepository.findByDropdown(dropdown, EntityState.ACTIVE);
					List<ValuesRes> valuesResList = valuesList.stream()
						.map(ValuesRes::toResponse)
						.collect(Collectors.toList());

					compMap.get("dropdown").add(DropdownRes.toResponse(dropdown, valuesResList));
					break;
				default:
					throw new IllegalArgumentException("Invalid ComponentType");

			}
		}

		List<String> keys = new ArrayList<>(compMap.keySet());
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			if (compMap.get(key).isEmpty()) {
				compMap.remove(key);
			}
		}

		return compMap;
	}

	//블록 추가
	@Override
	@Transactional
	public BlockInfoRes addBlockContent(BlockReqDto blockReqDto) {
		BlockInfoRes blockInfoRes = new BlockInfoRes();
		BlockInfoDto blockInfoDto = blockReqDto.getBlockInfo();
		List<CardReq> cardReqList = blockReqDto.getCardReqList();
		// List<ComponentReq> componentReqs = blockReqDto.getComponentList();

		//1. 블록 추가
		Block block = saveBlock(blockInfoDto);
		blockInfoRes.setBlockInfo(BlockRes.toResponse(block));

		//2. 카드 추가 + 관련 컴포넌트 추가
		blockInfoRes.setCardResList(saveCardAndComponent(block, cardReqList));

		return blockInfoRes;

	}

	//블록 수정
	@Transactional
	public void updateBlockContent(Integer blockId, BlockReqDto blockReqDto) {
		BlockInfoDto blockInfoDto = blockReqDto.getBlockInfo();
		List<CardReq> cardReqList = blockReqDto.getCardReqList();
		List<ComponentReq> componentReqs = blockReqDto.getComponentList();

		//수정 요청 블록과 api 블록 번호 비교
		if (!blockId.equals(blockInfoDto.getId())) {
			throw new EntityNotFoundException("Block And Request Block Mismatch");
		}

		//블록 존재 확인
		blockRepository.findActiveById(blockId, EntityState.ACTIVE)
			.orElseThrow(() -> new EntityNotFoundException("Active Block Not Found"));

		//1. 블록 수정
		blockRepository.updateBlockName(blockId, blockInfoDto.getName());
		Block updateBlock = blockRepository.findActiveById(blockId, EntityState.ACTIVE)
			.orElseThrow(() -> new EntityNotFoundException("Active Block Not Found"));

		//2. 카드 + 컴포넌트 수정
		saveCardAndComponent(updateBlock, cardReqList);
	}

	//카드, 컴포넌트 저장 및 수정
	@Transactional(propagation = Propagation.REQUIRED)
	public List<CardRes> saveCardAndComponent(Block block, List<CardReq> cardReqList) {
		//카드 저장 및 수정
		if (cardReqList != null) {
			//카드에 블록 정보 추가
			for (CardReq cardReq : cardReqList) {
				cardReq.setBlock(block);
			}
			List<Card> cardList = cardRepository.findByBlockId(block.getId(), EntityState.ACTIVE);
			return saveCardInfo(cardReqList, cardList);
		}

		//컴포넌트 저장 및 수정
		// if (componentReqs != null) {
		// 	saveComponent(componentReqs);
		// }
		return null;
	}

	// @Transactional(propagation = Propagation.REQUIRED)
	// public void updateButtonLinkId(List<ComponentReq> componentReqList) {
	// 	for (ComponentReq componentReq : componentReqList) {
	// 		saveComponentEntityByType(ComponentType.BU, componentReq.getObject(), componentReq, false);
	//
	// 		// ButtonRes button = (ButtonRes)componentReq.getObject();
	// 		// buttonRepository.updateLinkId(button.getId(), button.getLink());
	// 	}
	// }

	@Transactional(propagation = Propagation.REQUIRED)
	public Block saveBlock(BlockInfoDto blockInfoDto) {
		if (blockInfoDto.getId() != null) {    //id 존재 -> 블록 수정
			blockRepository.findActiveById(blockInfoDto.getId(), EntityState.ACTIVE).orElseThrow(
				() -> new EntityNotFoundException("Block Not Found")
			);
		}
		return blockRepository.save(Block.toEntity(blockInfoDto));
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public List<CardRes> saveCardInfo(List<CardReq> cardReqList, List<Card> originCardList) {
		//2. cardReqList -> id 없으면 생성
		//3. id 존재하면 update
		List<Integer> newCardIdList = new ArrayList<>();
		List<CardRes> cardResList = new ArrayList<>();

		for (CardReq cardReq : cardReqList) {
			log.info("cardReq : {}", cardReq);
			Card savedCard = cardRepository.save(Card.toEntity(cardReq));
			newCardIdList.add(savedCard.getId());

			List<ComponentReq> componentReqList = cardReq.getComponentList();

			//카드의 컴포넌트 추가
			if (componentReqList != null) {
				List<ComponentRes> componentResList = new ArrayList<>();
				for (ComponentReq componentReq : componentReqList) {
					componentReq.setCard(savedCard);    //카드 정보 추가
					Object object = componentReq.getObject();
					ComponentType componentType = componentReq.getType();
					componentResList.add(saveComponentEntityByType(componentType, object, componentReq));
				}
				cardResList.add(CardRes.toResponse(savedCard, componentResList));
			}
		}

		//카드 삭제
		for(int i=0; i<originCardList.size(); i++){
			Card card = originCardList.get(i);
			Integer id = card.getId();
			if(!newCardIdList.contains(id)){
				cardRepository.updateState(id, EntityState.DELETE);

				componentRepository.findByCard(card, EntityState.ACTIVE)
					.forEach(this::updateComponentEntityByType);
			}
		}
		return cardResList;
	}

	@Override
	public List<ComponentRes> addComponentByCard(Integer cardId, ComponentListDto componentList) {
		List<ComponentReq> componentReqList = componentList.getComponentList();
		Card card = cardRepository.findById(cardId).orElseThrow(() -> new EntityNotFoundException("Card Not Found"));
		List<ComponentRes> componentResList = new ArrayList<>();

		if (componentReqList != null) {
			for (ComponentReq componentReq : componentReqList) {
				componentReq.setCard(card);    //카드 정보 추가
				Object object = componentReq.getObject();
				ComponentType componentType = componentReq.getType();
				ComponentRes componentRes = saveComponentEntityByType(componentType, object, componentReq);
				componentResList.add(componentRes);
			}
		}
		return componentResList;
	}

	// @Transactional(propagation = Propagation.REQUIRED)
	// public void saveComponent(List<ComponentReq> componentReqs) {
	// 	for (ComponentReq componentReq : componentReqs) {
	// 		Card card = cardRepository.findById(componentReq.getCardId())
	// 			.orElseThrow(() -> new EntityNotFoundException("Active Card Not Found"));
	// 		componentReq.setCard(card);
	// 		Object object = componentReq.getObject();
	//
	// 		//3-1. 요소 수정 + 컴포넌트 수정
	// 		ComponentType componentType = componentReq.getType();
	// 		saveComponentEntityByType(componentType, object, componentReq, true);
	// 	}
	// }

	//type 별 객체 저장
	@Transactional(propagation = Propagation.REQUIRED)
	public ComponentRes saveComponentEntityByType(ComponentType componentType, Object object, ComponentReq componentReq) {
		ObjectMapper objectMapper = new ObjectMapper();
		ComponentRes componentRes = new ComponentRes();
		try {
			String json = objectMapper.writeValueAsString(object);

			switch (componentType) {
				case BU:
					Button button = saveEntity(json, Button.class, buttonRepository);
					componentReq.setLinkId(button.getId());
					componentRes.setObject(ButtonRes.toResponse(button));
					break;
				// case CB:
				// 	Checkbox checkbox = saveEntity(json, Checkbox.class, checkboxRepository);
				// 	componentReq.setLinkId(checkbox.getId());
				// 	break;
				case DD:
					DropdownRes dropdownRes = objectMapper.readValue(json, DropdownRes.class);
					Dropdown dropdown = Dropdown.toEntity(dropdownRes);
					dropdownRepository.save(dropdown);
					componentReq.setLinkId(dropdown.getId());

					//value 추가
					Optional.ofNullable(dropdownRes.getValuesList())
						.ifPresent(list -> list.forEach(cv -> {
							cv.setDropdown(dropdown);
							valuesRepository.save(ComponentValue.toEntity(cv));
						}));

					//반환
					List<ComponentValue> componentValueList = valuesRepository.findByDropdown(dropdown, EntityState.ACTIVE);
					List<ValuesRes> valuesResList = new ArrayList<>();
					for(ComponentValue componentValue : componentValueList){
						ValuesRes valuesRes = ValuesRes.toResponse(componentValue);
						valuesResList.add(valuesRes);
					}

					componentRes.setObject(DropdownRes.toResponse(dropdown, valuesResList));
					break;
				default:
					throw new IllegalArgumentException("Invalid ComponentType");
			}

			//컴포넌트 추가 or 수정
			// if (flag) {
			Component component = Component.toEntity(componentReq);
			componentRepository.save(component);
			componentRes.setId(component.getId());
			componentRes.setType(component.getComponentType());
			componentRes.setSequence(component.getSequence());
			// }

			return componentRes;

		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public <T> T saveEntity(String json, Class<T> entityClass, JpaRepository<T, Integer> repository) {
		T entity;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			entity = objectMapper.readValue(json, entityClass);
			return repository.save(entity);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	//블록 삭제
	@Override
	@Transactional
	public void deleteBlock(Integer blockId) {
		blockRepository.findActiveById(blockId, EntityState.ACTIVE)
			.orElseThrow(() -> new EntityNotFoundException("Block Not Found"));
		blockRepository.updateState(blockId, EntityState.DELETE);

		List<Card> cardList = cardRepository.findByBlockId(blockId, EntityState.ACTIVE);

		Optional.ofNullable(cardList)
			.ifPresent(list -> list.forEach(card -> {
				cardRepository.updateState(card.getId(), EntityState.DELETE);

				componentRepository.findByCard(card, EntityState.ACTIVE)
					.forEach(component -> {
						componentRepository.updateState(component.getId(), EntityState.DELETE);
						updateComponentEntityByType(component);
					});
			}));
	}

	//블록 component 삭제
	@Override
	@Transactional
	public void deleteBlockContent(Integer blockId, BlockReqDto blockReqDto) {
		// BlockInfoDto blockInfoDto = blockReqDto.getBlockInfo();
		List<CardReq> cardReqList = blockReqDto.getCardReqList();
		List<ComponentReq> componentReqList = blockReqDto.getComponentList();

		//1. 카드 삭제의 경우 -> 연관된 것 삭제
		Optional.ofNullable(cardReqList)
			.ifPresent(list -> list.forEach(cardReq -> {
				cardReq.setBlockId(blockId);
				Card card = Card.toEntity(cardReq);
				cardRepository.updateState(card.getId(), EntityState.DELETE);

				componentRepository.findByCard(card, EntityState.ACTIVE)
					.forEach(this::updateComponentEntityByType);
			}));

		//3. 컴포넌트 삭제의 경우
		Optional.ofNullable(componentReqList)
			.ifPresent(
				list -> list.forEach(componentReq -> updateComponentEntityByType(Component.toEntity(componentReq))));
	}

	//component Type 별 수정
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateComponentEntityByType(Component component) {
		ComponentType componentType = component.getComponentType();
		Integer id = component.getLinkId();
		componentRepository.updateState(component.getId(), EntityState.DELETE);

		switch (componentType) {
			case BU:
				buttonRepository.updateState(id, EntityState.DELETE);
				break;
			// case CB:
			// 	checkboxRepository.updateState(id, EntityState.DELETE);
			// 	break;
			case DD:
				dropdownRepository.updateState(id, EntityState.DELETE);
				Dropdown dropdown = dropdownRepository.findById(id)
					.orElseThrow(() -> new EntityNotFoundException("Dropdown Not Found"));

				List<ComponentValue> cValueList = valuesRepository.findByDropdown(dropdown, EntityState.ACTIVE);

				Optional.ofNullable(cValueList)
					.ifPresent(
						list -> list.forEach(cv -> valuesRepository.updateState(cv.getId(), EntityState.DELETE)));
				break;
			default:
				throw new IllegalArgumentException("Invalid ComponentType");
		}
	}

	//mes에 post api 요청
	@Override
	public Object requestPostToMes(String url, CardReqDto cardReqDto, CardType cardType) {
		String accessToken = jwtAuthenticationFilter.getAccessToken();
		if (cardReqDto.getActionId() == null)
			throw new InvalidValueException("ActionId Not Found");

		url += cardReqDto.getActionId();

		return Objects.requireNonNull(Objects.requireNonNull(webClient.post()
					.uri(url + "/1")
					// .uri(url)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.contentType(MediaType.APPLICATION_JSON)
					.body(BodyInserters.fromValue(cardReqDto))
					.retrieve()
					.toEntity(JsonResponse.class)
					.onErrorMap(e -> new MesServerException(e.getMessage()))
					.block())
				.getBody())
			.getData();
	}

	private String getDynamicString(String url, String key) {
		String accessToken = jwtAuthenticationFilter.getAccessToken();
		Object data = Objects.requireNonNull(Objects.requireNonNull(webClient
					.mutate()
					.baseUrl(url)
					.build()
					.get()
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.retrieve()
					.toEntity(JsonResponse.class)
					.onErrorMap(e -> new MesServerException(e.getMessage()))
					.block())
				.getBody())
			.getData();
		LinkedHashMap<String, Object> hashMap = (LinkedHashMap<String, Object>)data;
		return (String)hashMap.get(key);
	}
}
