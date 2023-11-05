import React from 'react';
import {View} from 'react-native';
import Back from '../../../assets/icons/chatbotback2.svg';
import Dots from '../../../assets/icons/dots.svg';
import styled from 'styled-components/native';

export const HeaderBody = styled.View`
  position: 'fixed';
  background: #242529;
  top: 0;
  height: 7%;
  display: flex;
  //border: 1px solid red;
  flex-direction: row;
  align-items: center;
  justify-content: center;
`;

export const Text = styled.Text`
  font-size: 25px;
  font-weight: bold;
  color: white;
  text-align: center;
  justify-content: center;
  align-items: center;
`;

function Header() {
  return (
    <HeaderBody>
      <View
        style={{
          width: '10%',
          display: 'flex',
          justifyContent: 'center',
        }}>
        <Back style={{marginLeft: 20, justifyContent: 'center'}} />
      </View>
      <View style={{width: '80%', display: 'flex', justifyContent: 'center'}}>
        <Text>MESC</Text>
      </View>
      <View
        style={{
          width: '10%',
          justifyContent: 'center',
        }}>
        <Dots fill="white" width={24} height={24} style={{marginRight: 30}} />
      </View>
    </HeaderBody>
  );
}

export default Header;
