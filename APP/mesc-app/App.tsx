import React, {useEffect} from 'react';
import {NavigationContainer} from '@react-navigation/native';
import {createStackNavigator} from '@react-navigation/stack';

import Login from './src/screens/login/Login';
import LoginPage from './src/screens/login/LoginPage';
import FindId from './src/screens/login/FindId';
import FindPassword from './src/screens/login/FindPassword';
import ChangePassword from './src/screens/login/ChangePassword';
import Success from './src/screens/login/Success';
import Intro from './src/screens/intro/Intro';
import BottomTab from './src/components/common/bottomTab/BottomTab';
import Main from './src/screens/main/Main';
import Chat from './src/screens/chat/Chat';
import {RecoilRoot} from 'recoil';
import SplashScreen from 'react-native-splash-screen';

const Stack = createStackNavigator();

function App() {
  useEffect(() => {
    //setTimeout을 이용하면 몇초간 스플래시 스크린을 보여주고 싶은지 설정할 수 있다.
    setTimeout(() => {
      SplashScreen.hide();
    }, 1000);
  }, []);

  return (
    <RecoilRoot>
      <NavigationContainer>
        <Stack.Navigator initialRouteName="Login">
          <Stack.Screen
            name="Login"
            component={LoginPage}
            options={{headerShown: false}}
          />
          <Stack.Screen
            name="LoginPage"
            component={LoginPage}
            options={{headerShown: false}}
          />
          <Stack.Screen
            name="FindId"
            component={FindId}
            options={{headerShown: false}}
          />
          <Stack.Screen
            name="FindPassword"
            component={FindPassword}
            options={{headerShown: false}}
          />
          <Stack.Screen
            name="ChangePassword"
            component={ChangePassword}
            options={{headerShown: false}}
          />
          <Stack.Screen
            name="Success"
            component={Success}
            options={{headerShown: false}}
          />
          <Stack.Screen
            name="Intro"
            component={Intro}
            options={{headerShown: false}}
          />
          <Stack.Screen
            name="Main"
            component={Main}
            options={{headerShown: false}}
          />
          <Stack.Screen
            name="Chat"
            component={Chat}
            options={{headerShown: false}}
          />
          <Stack.Screen
            name="BottomTab"
            component={BottomTab}
            options={{headerShown: false}}
          />
        </Stack.Navigator>
      </NavigationContainer>
    </RecoilRoot>
  );
}

export default App;
