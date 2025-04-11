package com.doorunlock.SeosMobileKeys

import android.util.Log
import android.os.Handler
import android.os.Looper
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import  com.assaabloy.mobilekeys.api.*
import  com.assaabloy.*
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.modules.core.DeviceEventManagerModule

class SeosMobileKeysModule(private val reactContext: ReactApplicationContext): ReactContextBaseJavaModule(reactContext) , LifecycleEventListener {
    private val TAG: String = "${this.javaClass.simpleName} Debug"

    init {
        instance = this
        reactContext.addLifecycleEventListener(this)
    }

    companion object {
        private var instance: SeosMobileKeysModule? = null

        fun getInstance(): SeosMobileKeysModule? {
            return instance
        }
    }

    lateinit var MobileKeysController: MobileKeysController


    override fun getName(): String {
        Log.d(TAG, "SeosMobileKeysModule Native module initialized")
        return "SeosMobileKeysModule"
    }

    @ReactMethod
    fun initializeSDK(promise : Promise){
        Log.d(TAG, "initializeSDK: called")
        try {
            reactContext.currentActivity?.let {
                Log.e(TAG, "initializeSDK: ${reactContext.applicationContext}")
                MobileKeysController = MobileKeysController(it)
                MobileKeysController.start()
            }
            promise.resolve("mobile keys controller sdk started successfully")
        }catch (e: Exception) {
            promise.reject("START_ERROR", e)
        }
    }
    @ReactMethod
    fun enableSDK(invitationCode : String){
        Log.d(TAG, "enableSDK: called ${invitationCode}")
            MobileKeysController.didPressRegistrationButton(invitationCode)
    }

    @ReactMethod
    fun setupEndpoint(){
        Log.d(TAG, "setupEndpoint: called")
        MobileKeysController.setupEndpoint()
    }

    @ReactMethod
    fun getTheMobileKeys(promise: Promise){
        try {
            val data = MobileKeysController.getTheMobileKeys()?.listMobileKeys()

            val mobileKeysArray = Arguments.createArray()

            if (data != null) {
                for (mMobileKey in data) {
                    val keyMap = Arguments.createMap()
                    keyMap.putString("cardNumber", mMobileKey.cardNumber)
                    keyMap.putString("configurationURL", mMobileKey.configurationURL)
                    keyMap.putString("externalId", mMobileKey.externalId)
                    keyMap.putString("issuer", mMobileKey.issuer)
                    keyMap.putString("label", mMobileKey.label)
                    keyMap.putString("readbackURL", mMobileKey.readbackURL)
                    keyMap.putString("type", mMobileKey.type)
                    keyMap.putString("identifier", mMobileKey.identifier.toString())
                    keyMap.putDouble("endDate", mMobileKey.endDate.timeInMillis.toDouble())
                    keyMap.putDouble("beginDate", mMobileKey.beginDate.timeInMillis.toDouble())
                    mobileKeysArray.pushMap(keyMap)
                }
            }

            promise.resolve(mobileKeysArray)

        } catch (e: Exception) {
            promise.reject("ERROR_GETTING_KEYS", "Failed to fetch mobile keys", e)
        }
    }

    @ReactMethod
    fun getSavedKey(promise: Promise){
        try {
            val data = MobileKeysController.getSavedKey()

            val mobileKeysArray = Arguments.createArray()

            if (data.isNotEmpty()) {
                for (mMobileKey in data) {
                    val keyMap = Arguments.createMap()
                    keyMap.putString("cardNumber", mMobileKey.cardNumber)
                    keyMap.putString("configurationURL", mMobileKey.configurationURL)
                    keyMap.putString("externalId", mMobileKey.externalId)
                    keyMap.putString("issuer", mMobileKey.issuer)
                    keyMap.putString("label", mMobileKey.label)
                    keyMap.putString("readbackURL", mMobileKey.readbackURL)
                    keyMap.putString("type", mMobileKey.type)
                    keyMap.putString("identifier", mMobileKey.identifier.toString())
                    keyMap.putDouble("endDate", mMobileKey.endDate.timeInMillis.toDouble())
                    keyMap.putDouble("beginDate", mMobileKey.beginDate.timeInMillis.toDouble())
                    mobileKeysArray.pushMap(keyMap)
                }
            }

            promise.resolve(mobileKeysArray)

        } catch (e: Exception) {
            promise.reject("ERROR_GETTING_KEYS", "Failed to fetch mobile keys", e)
        }
    }

    @ReactMethod
        fun toggleScanning(promise: Promise){
            val state = MobileKeysController.toggleScanning()
            promise.resolve(state)
        }

    @ReactMethod
        fun scanningState(promise: Promise){
            val state = MobileKeysController.scanningState()
            promise.resolve(state)
        }

        @ReactMethod
    fun getEndpointSetupState(promise: Promise){
        val state = MobileKeysController?.isEndpointSetup()
        promise.resolve(state)
    }

    @ReactMethod
    fun refreshTheKeys(){
//        MobileKeysController?.refreshTheKeys()
    }

    @ReactMethod
    fun mobileKeyStartup(promise: Promise){
//        val state = MobileKeysController?.getMobileKeyStartup()
        val state = true
        promise.resolve(state)
    }

    @ReactMethod
    fun stopScanning(){
        MobileKeysController?.stopScanning()
    }

    @ReactMethod
    fun unregisterEndpoint(){
//        MobileKeysController?.unregisterEndpoint()
    }

//    fun getSupportedEvents(): List<String> {
//        return listOf("onMessageFromSeos")
//    }

    fun sendSeosSdkEvent(eventName: String, eventData: Any) {
        val eventBody = Arguments.createMap()
        eventBody.putString("withName", eventName)
        eventBody.putString("body", eventData.toString())

        Handler(Looper.getMainLooper()).post {
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("onMessageFromSeos", eventBody)
        }
        Log.d("onMessageFromSeos", "Event Sent: $eventName -> $eventData")
    }

    override fun onHostResume() {}
    override fun onHostPause() {}
    override fun onHostDestroy() {
        reactContext.removeLifecycleEventListener(this)
    }


}