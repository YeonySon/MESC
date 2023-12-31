import React, {useCallback, useEffect, useState} from 'react';
//Axios
import customAxios from '../../../Api';
//Recoil
import {useRecoilState} from 'recoil';
import {checkContactState} from '../../states/CheckContact';
// style
import {AboutContainer} from '../common/about/AboutContainer';
import * as S from './ContactListStyle';
import {TouchableOpacity} from 'react-native';
import {colors} from '../common/Theme';
// icon
import Filter from '../../assets/icons/filter.svg';
import Search from '../../assets/icons/search.svg';
import CheckBoxIcon from '../../assets/icons/checkbox.svg';
import Check from '../../assets/icons/check.svg';
import Close from '../../assets/icons/x.svg';
// BottomSheet
import {BottomSheetScrollView} from '@gorhom/bottom-sheet';
import {ReportForm} from '../message/Report/ReportForm';

export const ContactListForm = () => {
  useEffect(() => {
    customAxios
      .get('https://www.mesc.kr/api/mesc/user/members', {})
      .then(res => {
        const contactList = res.data.data;
        // contactList를 Contacts에 추가
        setContacts(contactList.userList);
      })
      .catch(error => {
        console.error('Error fetching logs: ', error);
      });
  }, []);

  const [Contacts, setContacts] = useState([]);

  // Contacts.map(contact => {
  //   console.log(contact);
  // });

  const [checkContact, setCheckContact] = useRecoilState(checkContactState);
  const handleCheckBoxClick = (
    userId: number,
    email: string,
    name: string,
    phoneNumber: string,
    role: string,
  ) => {
    setCheckContact(prev => {
      // 이미 선택된 사용자인지 확인
      const isUserSelected = prev.users.some(user => user.userId === userId);

      if (!isUserSelected) {
        const updatedUsers = [
          ...prev.users,
          {email, name, phoneNumber, role, userId, checkContact: true},
        ];

        return {users: updatedUsers};
      } else {
        const updatedUsers = prev.users.filter(user => user.userId !== userId);

        return {users: updatedUsers};
      }
    });
  };

  // // 출력문
  // console.log('checkContact: ', checkContact);

  const [complite, setComplite] = useState(false);

  const handleClosePress = () => {
    setComplite(!complite);
    // console.log('완료 버튼 눌림');
  };

  interface CheckContactProps {
    email: string;
    name: string;
    phoneNumber: string;
    role: string;
    userId: number;
    checkContact: boolean;
  }

  const renderItem = useCallback(
    (item: CheckContactProps) => {
      const isChecked = checkContact.users.some(
        user => user.userId === item.userId,
      );

      return (
        <S.ContactDiv
          key={item.userId}
          onPress={() =>
            handleCheckBoxClick(
              item.userId,
              item.email,
              item.name,
              item.phoneNumber,
              item.role,
            )
          }>
          <S.ContactBox>
            <S.NameBox>
              <S.BoldText size={17} color={colors.primary}>
                {item.name}
              </S.BoldText>
            </S.NameBox>
            <S.EmailBox>
              <S.BoldText size={15} color={colors.primary}>
                {item.email}
              </S.BoldText>
            </S.EmailBox>
          </S.ContactBox>
          <S.CheckboxContainer>
            <S.CheckBox
              key={item.userId}
              onPress={() =>
                handleCheckBoxClick(
                  item.userId,
                  item.email,
                  item.name,
                  item.phoneNumber,
                  item.role,
                )
              }>
              {isChecked && <Check />}
            </S.CheckBox>
          </S.CheckboxContainer>
        </S.ContactDiv>
      );
    },
    [checkContact, handleCheckBoxClick],
  );

  return (
    // 전체를 담는 Container
    <>
      {complite === false ? (
        <AboutContainer
          backgroundColor="skyblue"
          width="100%"
          height="100%"
          flexDirection="column">
          <S.Container>
            <S.Div>
              <S.Top>
                <S.Navigation style={{justifyContent: 'flex-end'}}>
                  <S.TitleBox>
                    <S.Title>연락처</S.Title>
                  </S.TitleBox>

                  <S.SendBtn onPress={() => handleClosePress()}>
                    <S.SendText> 완료 </S.SendText>
                  </S.SendBtn>

                  {/* <S.Right onPress={() => handleClosePress()}>
                    <S.Text size={16} color={colors.primary}>
                      완료
                    </S.Text>
                  </S.Right> */}
                </S.Navigation>
                <S.Search>
                  <Search />
                  <S.SearchText placeholder="검색" />
                </S.Search>
              </S.Top>
              <S.Body>
                <S.FilterDiv>
                  <S.FilterText>멤버 총 {Contacts.length}명</S.FilterText>
                  <TouchableOpacity>
                    <Filter />
                  </TouchableOpacity>
                </S.FilterDiv>
                <BottomSheetScrollView
                  style={{marginBottom: '10%'}}
                  showsVerticalScrollIndicator={false}>
                  {/* 연락처 정보 랜더링 되는 구간 */}
                  <S.Row>{Contacts.map(renderItem)}</S.Row>
                </BottomSheetScrollView>
              </S.Body>
            </S.Div>
          </S.Container>
        </AboutContainer>
      ) : (
        <ReportForm />
      )}
    </>
  );
};
