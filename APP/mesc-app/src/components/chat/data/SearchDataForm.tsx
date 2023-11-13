import React, {useState, useEffect} from 'react';
import * as S from './SearchDataForm.styles';
import {ScrollView} from 'react-native-gesture-handler';
import SearchBtn from '../../../assets/icons/searchbtn.svg';
import NextBtn from '../../../assets/icons/nextBtn.svg';
import {useRecoilValue, useRecoilState} from 'recoil';
import {BlockResponseData} from '../../../states/BlockResponseState';
import {getBlock} from '../../../../Api';
import {TouchableOpacity} from '@gorhom/bottom-sheet';
import {ChatbotHistoryState} from '../../../states/ChatbotHistoryState';
import UserMessage from '../UserMessage';

type ButtonItem = {
  id: number;
  name: string;
  link: string;
  actionId?: number;
};

const SearchDataForm = () => {
  const [block, setBlock] = useRecoilState(BlockResponseData);
  const [chatbotHistory, setChatbotHistory] =
    useRecoilState(ChatbotHistoryState);
  // console.log('block', block);
  const cardList = block.cardList;
  const mlCard = cardList.find(card => card.cardType === 'ML');

  const handleButtonClick = async (button: ButtonItem) => {
    setChatbotHistory([
      ...chatbotHistory,
      <UserMessage message={button.name} />,
    ]);
    const body = {
      actionId: 23,
      title: button.name,
      conditions: '',
    };
    const block = await getBlock(4, body);
    // console.log(block);
    setBlock(block);
  };

  return (
    <S.Container>
      <S.SearchInput>
        <S.ImageBox>
          <SearchBtn />
        </S.ImageBox>
        <S.SearchText placeholder="검색어를 입력하세요" />
      </S.SearchInput>
      <S.ButtonContainer>
        <ScrollView showsVerticalScrollIndicator={false}>
          {mlCard &&
            mlCard.button &&
            mlCard.button.map((button: ButtonItem) => (
              <S.ButtonRow key={button.id}>
                <TouchableOpacity onPress={() => handleButtonClick(button)}>
                  <S.ButtonName>{button.name}</S.ButtonName>
                  <S.ImageBox>
                    <NextBtn />
                  </S.ImageBox>
                </TouchableOpacity>
              </S.ButtonRow>
            ))}
        </ScrollView>
      </S.ButtonContainer>
    </S.Container>
  );
};

export default SearchDataForm;
