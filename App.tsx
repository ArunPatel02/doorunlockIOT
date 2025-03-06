/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, {useEffect} from 'react';
import {
  StatusBar,
  // StyleSheet,
  useColorScheme,
  NativeModules,
  Button,
  SafeAreaView,
  NativeEventEmitter,
  Alert,
  Image,
  View,
} from 'react-native';

import {Colors} from 'react-native/Libraries/NewAppScreen';

function App(): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
    flex: 1,
  };

  useEffect(() => {
    const {SeosMobileKeysModule} = NativeModules;
    console.log({SeosMobileKeysModule});
    const eventEmitter = new NativeEventEmitter(SeosMobileKeysModule);

    eventEmitter.addListener('onMessageFromSeos', event => {
      console.log('DEBUG ---- From React App', event);
      switch (event?.withName) {
        case 'mobileKeysDidFailtoSetupEndpoint':
          Alert.alert(event.body);
          break;

        case 'mobileKeysDidSetupEndpointtoStartup':
          SeosMobileKeysModule.setupEndpoint();
          break;

        default:
          break;
      }
    });

    SeosMobileKeysModule?.initializeSDK()
      .then((res: any) => {
        console.log(res); // Output: "SeosMobileKeys"
      })
      .catch((err: any) => {
        console.error(err);
      });
  }, []);

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <View style={{alignItems: 'center' , marginTop : 30}}>
        <Image
          source={require('./asserts/Key.gif')}
          style={{width: 300, height: 300}}
        />
      </View>
      <View
        style={{
          flexGrow: 1,
          justifyContent: 'center',
          alignItems: 'center',
          gap: 20,
        }}>
        <Button
          title="Enable Sdk"
          onPress={() => {
            const {SeosMobileKeysModule} = NativeModules;
            SeosMobileKeysModule?.enableSDK('AP6E-GQ4B-XDZU-BDX5');
          }}
        />
        <Button
          title="setupEndpoint"
          onPress={() => {
            const {SeosMobileKeysModule} = NativeModules;
            SeosMobileKeysModule?.setupEndpoint();
          }}
        />

        <Button
          title="Fetch keys"
          onPress={() => {
            const {SeosMobileKeysModule} = NativeModules;
            SeosMobileKeysModule?.getTheMobileKeys()
              .then((res: any) => {
                console.log('DEBUG ---- getTheMobileKeys', res); // Output: "SeosMobileKeys"
              })
              .catch((err: any) => {
                console.error('DEBUG ---- getTheMobileKeys Error ---- ', err);
              });
          }}
        />

        <Button
          title="Get Saved Key"
          onPress={() => {
            const {SeosMobileKeysModule} = NativeModules;
            SeosMobileKeysModule?.getSavedKey()
              .then((res: any) => {
                console.log('DEBUG ---- getSavedKey', res); // Output: "SeosMobileKeys"
              })
              .catch((err: any) => {
                console.error('DEBUG ---- getSavedKey Error ---- ', err);
              });
          }}
        />

        <Button
          title="Toggle Scanning"
          onPress={() => {
            const {SeosMobileKeysModule} = NativeModules;
            SeosMobileKeysModule?.toggleScanning()
              .then((res: any) => {
                console.log('DEBUG ---- toggleScanning', res); // Output: "SeosMobileKeys"
              })
              .catch((err: any) => {
                console.error('DEBUG ---- toggleScanning Error ---- ', err);
              });
          }}
        />

        <Button
          title="scanning State"
          onPress={() => {
            const {SeosMobileKeysModule} = NativeModules;
            SeosMobileKeysModule?.scanningState()
              .then((res: any) => {
                console.log('DEBUG ---- scanningState', res); // Output: "SeosMobileKeys"
              })
              .catch((err: any) => {
                console.error('DEBUG ---- scanningState Error ---- ', err);
              });
          }}
        />
      </View>
    </SafeAreaView>
  );
}

// const styles = StyleSheet.create({
//   sectionContainer: {
//     marginTop: 32,
//     paddingHorizontal: 24,
//   },
//   sectionTitle: {
//     fontSize: 24,
//     fontWeight: '600',
//   },
//   sectionDescription: {
//     marginTop: 8,
//     fontSize: 18,
//     fontWeight: '400',
//   },
//   highlight: {
//     fontWeight: '700',
//   },
// });

export default App;
