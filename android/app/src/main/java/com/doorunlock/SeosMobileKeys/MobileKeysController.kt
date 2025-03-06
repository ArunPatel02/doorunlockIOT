package com.doorunlock.SeosMobileKeys

import android.app.Activity
import android.app.Application
import android.app.Notification
import android.bluetooth.le.ScanSettings
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.assaabloy.mobilekeys.api.ApiConfiguration
import com.assaabloy.mobilekeys.api.EndpointSetupConfiguration
import com.assaabloy.mobilekeys.api.MobileKey
import com.assaabloy.mobilekeys.api.MobileKeys
import com.assaabloy.mobilekeys.api.MobileKeysApi
import com.assaabloy.mobilekeys.api.MobileKeysCallback
import com.assaabloy.mobilekeys.api.MobileKeysException
import com.assaabloy.mobilekeys.api.ReaderConnectionController
import com.assaabloy.mobilekeys.api.ReaderConnectionInfoType
import com.assaabloy.mobilekeys.api.ble.BluetoothMode
import com.assaabloy.mobilekeys.api.ble.OpeningResult
import com.assaabloy.mobilekeys.api.ble.OpeningStatus
import com.assaabloy.mobilekeys.api.ble.OpeningTrigger
import com.assaabloy.mobilekeys.api.ble.OpeningType
import com.assaabloy.mobilekeys.api.ble.Reader
import com.assaabloy.mobilekeys.api.ble.ReaderConnectionCallback
import com.assaabloy.mobilekeys.api.ble.ReaderConnectionListener
import com.assaabloy.mobilekeys.api.ble.ReaderConnectionParams
import com.assaabloy.mobilekeys.api.ble.ScanConfiguration
import com.assaabloy.mobilekeys.api.ble.ScanMode
import com.assaabloy.mobilekeys.api.ble.TapOpeningTrigger
import com.assaabloy.mobilekeys.api.hce.HceConnectionCallback
import com.assaabloy.mobilekeys.api.hce.HceConnectionListener
import com.assaabloy.mobilekeys.api.hce.NfcConfiguration
import com.doorunlock.SeosMobileKeys.ClosestLockTrigger
import com.assaabloy.mobilekeys.api.internal.MobileKeysFactory
import com.doorunlock.BuildConfig
import kotlin.math.log

class MobileKeysController (val activity : Activity) : MobileKeysApiFactory ,
    ReaderConnectionListener, HceConnectionListener , ClosestLockTrigger.LockInRangeListener {

    private val TAG: String = "SeosMobileKeyModule MobileKeyController Debug"

    private lateinit var mobileKeysManager : MobileKeysApi

    private  lateinit var  keys :  List<MobileKey>

    private var scanConfiguration: ScanConfiguration? = null
    private var closestLockTrigger: ClosestLockTrigger? = null

    private var readerConnectionCallback: ReaderConnectionCallback? = null
    private var hceConnectionCallback: HceConnectionCallback? = null
    private var readerConnectionController: ReaderConnectionController? = null

    init {
        Log.d(TAG, "init: calling the init function")
//        mobileKeysManager = createInitializedMobileKeysManager()
        createInitializedMobileKeysManager()
    }

    fun createInitializedMobileKeysManager() {
        Log.d(TAG, "createInitializedMobileKeysManager: calling the createInitializedMobileKeysManager function")
        val openingTriggers = arrayOf<OpeningTrigger>(TapOpeningTrigger(activity.application))

        val readerConnectionParams = ReaderConnectionParams()
        readerConnectionParams.setNegotiateMtu(true)

        scanConfiguration = ScanConfiguration.Builder(
            openingTriggers,
            1,
        )
            .setAllowBackgroundScanning(true)
            .setBluetoothModeIfSupported(BluetoothMode.DUAL)
            .setReaderConnectionParams(readerConnectionParams)
            .setScanMode(ScanMode.OPTIMIZE_PERFORMANCE)
            .build()

        val apiConfiguration = ApiConfiguration.Builder()
            .setApplicationId("EXAMPLE")
            .setApplicationDescription("ASSA ABLOY Mobile Keys Example Implementation")
            .setNfcParameters(
                NfcConfiguration.Builder()
                    .unsafe_setAttemptNfcWithScreenOff(false)
                    .build()
            )
            .build()

        mobileKeysManager = MobileKeysApi.getInstance()
        mobileKeysManager.initialize(activity.application, apiConfiguration, scanConfiguration)
        check(mobileKeysManager.isInitialized != false)

        val nfcParameters = mobileKeysManager.readerConnectionController.nfcParameters
        Log.d("MainApplication Debug", "NfcParams Config " +
                "numberOfNfcTransactionsNeeded: " + nfcParameters.numberOfNfcTransactionsNeeded() +
                ", transactionBackOff: " + nfcParameters.transactionBackOff()
        )

        MobileKeysApi.getInstance().readerConnectionController.enableHce()
    }

    fun start(){
        Log.d(TAG, "start: calling the start function")
        Log.d(TAG, "calling is endpoint setup ${isEndpointSetup()}")
        if(isEndpointSetup()){
            Log.d(TAG ,"Personalized and showing KeyView")

            keys = mobileKeysManager.mobileKeys.listMobileKeys()

        }else{
            Log.d(TAG, "Not Personalized, showing PersonalizationView")
        }
        mobileKeysManager.mobileKeys.applicationStartup(mobileKeysCallbackInitialSetup)
        readerConnectionCallback = ReaderConnectionCallback(activity.applicationContext)
        readerConnectionCallback!!.registerReceiver(this)

        hceConnectionCallback = HceConnectionCallback(activity.applicationContext)
        hceConnectionCallback!!.registerReceiver(this)
        closestLockTrigger = ClosestLockTrigger(this)
        scanConfiguration?.rootOpeningTrigger?.add(closestLockTrigger)
    }

    fun isEndpointSetup() : Boolean{
        try {
            Log.d(TAG, "endpointInfo: ${mobileKeysManager.mobileKeys?.endpointInfo}")
            Log.d(TAG , "isEndpointSetupComplete: ${mobileKeysManager.mobileKeys?.isEndpointSetupComplete}")
//            return  mobileKeysManager.mobileKeys.endpointInfo.isPersonalized
            return  mobileKeysManager.mobileKeys.isEndpointSetupComplete
        } catch (exception: MobileKeysException) {
            Log.e(TAG, "exception: $exception")
            Log.d(TAG, "MobileKeysException cause: %s\nMobileKeysException: ErrorCode: %s ${exception.causeMessage}, ${exception.errorCode} ")
            exception.printStackTrace()
            return  false
        }
    }

    fun getTheMobileKeys(): MobileKeys? {
        Log.d(TAG, "getMobileKeys: called")
        return  mobileKeysManager.mobileKeys
    }

    fun getSavedKey() : List<MobileKey>{
        return  keys
    }

    private val mobileKeysCallbackInitialSetup = object : MobileKeysCallback {
        override fun handleMobileKeysTransactionCompleted() {
            Log.d(TAG, "handleMobileKeysTransactionCompleted: ")
            onStartUpComplete()
        }

        override fun handleMobileKeysTransactionFailed(mobileKeysException: MobileKeysException) {
            Log.d(TAG ,"handleMobileKeysTransactionFailed: endPointSetup: ${mobileKeysException.causeMessage}, error Code: ${mobileKeysException.errorCode}")
        }
    }

    fun onStartUpComplete() {
        Log.d(TAG, "onStartUpComplete: steupcompleted")
    }

    fun didPressRegistrationButton(code : String){
        Log.d(TAG, "didPressRegistrationButton: invitaioncode ${code}")
        if (code.isNotEmpty()) {
            mobileKeysManager.mobileKeys.endpointSetup(
                mobileKeysCallbackForEndPointSetupInitially,
                code,
                EndpointSetupConfiguration.Builder().build()
            )
        }else{
            Log.e(TAG, "INVITATION_CODE empty")
        }
    }

    fun setupEndpoint() {
        if(mobileKeysManager.isInitialized){
            this.getTheMobileKeys()?.endpointUpdate(mobileKeysCallbackEndpointUpdate)
        }
    }

    fun onEndpointSetUpComplete() {
        Log.d(TAG, "onEndpointSetUpComplete: called")
        setupEndpoint()
    }

    fun toggleScanning() : Boolean {
        Log.d(TAG, "startScanning: called ${scanConfiguration}")
        val controller = mobileKeysManager.readerConnectionController
        if (scanConfiguration != null && mobileKeysManager.mobileKeys.listMobileKeys().count() > 0) {
            try {
                val lockServiceCode: Int = mobileKeysManager.mobileKeys.listMobileKeys().firstOrNull()?.cardNumber?.toIntOrNull() ?: 1
                Log.d(TAG,"lockServiceCode: $lockServiceCode")
                scanConfiguration!!.setLockServiceCodes(lockServiceCode)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                scanConfiguration!!.setLockServiceCodes(1)
            }
            if(controller.isScanning){
                controller.stopScanning()
            }else{
                controller.startScanning()
            }
            Log.d(TAG, "startScanning: ${controller.isScanning}")
        }
        return controller.isScanning
    }

    fun stopScanning(){
        Log.d(TAG, "stopScanning: called")
        mobileKeysManager.readerConnectionController.stopScanning()
    }

    fun scanningState() : Boolean{
        return MobileKeysApi.getInstance().readerConnectionController.isScanning
    }

    private val mobileKeysCallbackForEndPointSetupInitially = object : MobileKeysCallback {
        override fun handleMobileKeysTransactionCompleted() {
            Log.d(TAG , "EndpointSetup: mobileKeysCallbackForEndPointSetupInitially => handleMobileKeysTransactionCompleted, isEndPointSetup: %s ${isEndpointSetup()}")
            SeosMobileKeysModule.getInstance()?.sendSeosSdkEvent("mobileKeysDidSetupEndpointtoStartup" , "mobile Keys Did Setup Endpoint to Startup")
            onEndpointSetUpComplete()
        }

        override fun handleMobileKeysTransactionFailed(mobileKeysException: MobileKeysException) {
            Log.e(TAG,"isEndPointSetup: ${isEndpointSetup()}")
            Log.e(TAG, "EndpointSetup: mobileKeysCallbackForEndPointSetupInitially => handleMobileKeysTransactionFailed, %s ${mobileKeysException.causeMessage}")
            SeosMobileKeysModule.getInstance()?.sendSeosSdkEvent("mobileKeysDidFailtoSetupEndpoint" , "mobile key setup up end point fail")
        }
    }

    private val mobileKeysCallbackEndpointUpdate = object : MobileKeysCallback {
        override fun handleMobileKeysTransactionCompleted(){
            Log.d(TAG, "handleMobileKeysTransactionCompleted: called mobileKeysDidUpdateEndpoint")
            SeosMobileKeysModule.getInstance()?.sendSeosSdkEvent("mobileKeysDidUpdateEndpoint" , "mobile key did update end point")
        }

        override fun handleMobileKeysTransactionFailed(mobileKeysException: MobileKeysException){
            Log.d(TAG, "handleMobileKeysTransactionFailed: ${mobileKeysException.causeMessage} mobileKeysDidUpdateEndpoint")
            SeosMobileKeysModule.getInstance()?.sendSeosSdkEvent("mobileKeysDidFailtoSetupEndpoint" , "mobile key setup up end point fail")
        }

    }

    override fun getMobileKeys(): MobileKeys? {
        return  mobileKeysManager.mobileKeys
    }

    override fun getReaderConnectionController(): ReaderConnectionController? {
        return mobileKeysManager.readerConnectionController
    }

    override fun getScanConfiguration(): ScanConfiguration? {
        return scanConfiguration
    }

    override fun onReaderConnectionOpened(p0: Reader?, p1: OpeningType?) {
        Log.d(TAG, "onReaderConnectionOpened: called ${p1}")
    }

    override fun onReaderConnectionClosed(p0: Reader?, p1: OpeningResult?) {
        val byteArray = p1?.statusPayload
        val hexString = byteArray?.joinToString("") { String.format("%02X", it) }
        Log.d(TAG, "onReaderConnectionClosed: called ${p1} ${hexString}")
        if(p1?.openingStatus === OpeningStatus.SUCCESS){
            stopScanning()
            SeosMobileKeysModule.getInstance()?.sendSeosSdkEvent("Unlock" , "door unlock succesfully")
        }else{
            stopScanning()
            p1?.openingStatus?.let {
                SeosMobileKeysModule.getInstance()?.sendSeosSdkEvent("DidNotUnlock" ,
                    it
                )
            }
        }
    }

    override fun onReaderConnectionFailed(p0: Reader?, p1: OpeningType?, p2: OpeningStatus?) {
        Log.d(TAG, "onReaderConnectionFailed: called ${p1} ${p2}")
    }

    override fun onHceSessionOpened() {
        Log.d(TAG, "onHceSessionOpened: called")
    }

    override fun onHceSessionClosed(p0: Int) {
        Log.d(TAG, "onHceSessionClosed: called")
    }

    override fun onHceSessionInfo(p0: ReaderConnectionInfoType?) {
        Log.d(TAG, "onHceSessionInfo: called")
    }

    override fun onLockInRange(lockInRange: Boolean) {
        Log.d(TAG, "onLockInRange: ${lockInRange}")
    }

}