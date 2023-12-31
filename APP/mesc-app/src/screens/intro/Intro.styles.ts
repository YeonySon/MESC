import styled from 'styled-components/native';
import {Image} from 'react-native';
import {colors} from '../../components/common/Theme';

export const Container = styled.View`
  background-color: white;
  flex: 1;
  align-items: center;
  background-color: pink;
`;

export const BackgroundImg = styled(Image)`
  position: absolute;
  object-fit: cover;
  top: -30%;
`;

export const Div = styled.View`
  height: 100%;
  // background-color: red;
`;

export const Top = styled.View`
  padding-right: 10%;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 10%;
  // background-color: red;
`;

export const SkipBox = styled.TouchableOpacity`
  width: 100%;
`;

export const Skip = styled.Text`
  font-size: 16px;
  font-weight: bold;
  color: ${colors.primary};
  text-align: right;
`;

export const Body = styled.View`
  display: flex;
  align-items: center;
  height: 75%;
  width: 100%;
  // background-color: blue;
`;

export const Img = styled(Image)`
  width: 300px;
  height: 300px;
`;

export const View = styled.View`
  display: flex;
  align-items: center;
  justify-content: space-evenly;
  // height: 35%;
  // background-color: blue;
`;

export const MainText = styled.Text`
  font-size: 30px;
  font-weight: bold;
  color: ${colors.primary};
  text-align: center;
`;

export const SubText = styled.Text`
  font-size; 16px;
  color: ${colors.secondary}
  text-align: center;
`;

export const Circles = styled.View`
  display: flex;
  flex-direction: row;
`;

export const CircleSelected = styled.View`
  width: 14px;
  height: 14px;
  background-color: ${colors.tertiary};
  border-radius: 7px;
  margin: 0px 7px;
`;

export const Circle = styled.View`
  width: 14px;
  height: 14px;
  background-color: ${colors.quaternary};
  border-radius: 7px;
  margin: 0px 7px;
`;

export const Bottom = styled.View`
  align-items: center;
  height: 15%;
  // background-color: green;
`;

export const Button = styled.TouchableOpacity`
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 20px;
  width: 100%;
  height: 56px;
  background-color: ${colors.iris};
  border-radius: 14px;
`;

export const ButtonText = styled.Text`
  font-size: 16px;
  font-weight: bold;
  color: white;
`;
