package com.doorunlock.SeosMobileKeys

import android.util.Log
import com.assaabloy.mobilekeys.api.ble.ManualOpeningTrigger
import com.assaabloy.mobilekeys.api.ble.OpeningStatus
import com.assaabloy.mobilekeys.api.ble.OpeningTriggerAction
import com.assaabloy.mobilekeys.api.ble.OpeningType
import com.assaabloy.mobilekeys.api.ble.Reader


/**
 * Custom opening trigger used to open closest reader.
 * Only use custom opening trigger if you are to implement your own trigger, otherwise use the standard triggers
 * available in the SDK.
 */
class ClosestLockTrigger(private val listener: LockInRangeListener) : ManualOpeningTrigger() {
    /**
     * Callback interface
     */
    interface LockInRangeListener {
        /**
         * On lock in range callback
         *
         * @param lockInRange
         */
        fun onLockInRange(lockInRange: Boolean)
    }

    override fun onScanReceived(reader: Reader): OpeningTriggerAction {
        Log.d(TAG, "onScanReceived: called")
        if (reader.isInMotionRange) {
            updateLockInRangeStatus(true)
        }
        return OpeningTriggerAction.noOpening()
    }

    override fun onNoReadersInRange() {
        Log.d(TAG, "onNoReadersInRange: called")
        super.onNoReadersInRange()
        updateLockInRangeStatus(false)
    }

    override fun onStart() {
        Log.d(TAG, "onStart: called")
        super.onStart()
        updateLockInRangeStatus(bleScanner.listReaders().isEmpty())
    }

    override fun onStop() {
        super.onStop()
        updateLockInRangeStatus(false)
    }

    /**
     * Open closest reader
     */
    fun openClosestReader() {
        val closestReader = closestReader
        if (validReader(closestReader)) {
            Log.d(TAG, "Open reader")
            openReader(closestReader, OpeningType.APPLICATION_SPECIFIC)
        }
    }

    /**
     * Verify that the reader is valid
     *
     * @param bleReader
     * @return
     */
    private fun validReader(bleReader: Reader?): Boolean {
        if (bleReader == null || !bleReader.isInMotionRange) {
            Log.d(TAG, "Reader out of range")
            connectionListener().onReaderConnectionFailed(
                null, OpeningType.APPLICATION_SPECIFIC, OpeningStatus.OUT_OF_RANGE
            )
            return false
        }
        return true
    }

    /**
     * Update lock in range status
     *
     * @param lockInRange
     */
    private fun updateLockInRangeStatus(lockInRange: Boolean) {
        listener.onLockInRange(lockInRange)
    }

    companion object {
        private val TAG: String = "${ClosestLockTrigger::class.java.name} Debug"
    }
}

//import android.util.Log
//import com.assaabloy.mobilekeys.api.ble.*
//
///**
// * Custom opening trigger used to open closest reader.
// * Only use custom opening trigger if you are to implement your own trigger, otherwise use the standard triggers
// * available in the SDK.
// */
//class ClosestLockTrigger(listener: LockInRangeListener) :
//    ManualOpeningTrigger() {
//
//    /**
//     * Callback interface
//     */
//    interface LockInRangeListener {
//        /**
//         * On lock in range callback
//         *
//         * @param lockInRange
//         */
//        fun onLockInRange(lockInRange: Boolean)
//    }
//    private val TAG: String = "SeosMobileKeyModule MobileKeyController ClodeLockTrigger Debug"
//    private val listener: LockInRangeListener
//
//    init {
//        Log.d(TAG, "intit called: from closestLockTrigger ")
//        this.listener = listener
//    }
//
//    override fun onScanReceived(reader: Reader): OpeningTriggerAction {
//        Log.d(TAG, "onScanReceived: ")
//        if (reader.isInMotionRange) {
//            updateLockInRangeStatus(true)
//        }
//        return OpeningTriggerAction.noOpening()
//    }
//
//    override fun onNoReadersInRange() {
//        Log.d(TAG, "onNoReadersInRange: ")
//        super.onNoReadersInRange()
//        updateLockInRangeStatus(false)
//    }
//
//    override fun onStart() {
//        Log.d(TAG, "onStart: ")
//        super.onStart()
//        updateLockInRangeStatus(bleScanner.listReaders().isEmpty())
//    }
//
//    override fun onStop() {
//        Log.d(TAG, "onStop: ")
//        super.onStop()
//        updateLockInRangeStatus(false)
//    }
//
//    /**
//     * Open closest reader
//     */
//    fun openClosestReader() {
//        Log.d(TAG, "openClosestReader: ")
//        val closestReader = closestReader
//        if (validReader(closestReader)) {
//            Log.d(TAG, "Open reader")
//            if(closestReader.readerSupportedOpeningTypes().contains(OpeningType.APPLICATION_SPECIFIC)){
//                openReader(closestReader,OpeningType.APPLICATION_SPECIFIC)
//            } else if(closestReader.readerSupportedOpeningTypes().contains(OpeningType.PROXIMITY)){
//                openReader(closestReader,OpeningType.PROXIMITY)
//            }else if(closestReader.readerSupportedOpeningTypes().contains(OpeningType.SEAMLESS)){
//                openReader(closestReader,OpeningType.SEAMLESS)
//            }
//
//        }
//    }
//
//    /**
//     * Verify that the reader is valid
//     *
//     * @param bleReader
//     * @return
//     */
//    private fun validReader(bleReader: Reader?): Boolean {
//        Log.d(TAG, "validReader: ")
//        if (bleReader == null ||/* !bleReader.isInMotionRange || !bleReader.isInSeamlessRange ||*/ !bleReader.isInProximityRange) {
//            Log.d(TAG, "Reader out of range")
//            connectionListener().onReaderConnectionFailed(null,
//                OpeningType.APPLICATION_SPECIFIC,
//                OpeningStatus.OUT_OF_RANGE)
//            return false
//        }
//        return true
//    }
//
//    /**
//     * Update lock in range status
//     *
//     * @param lockInRange
//     */
//    private fun updateLockInRangeStatus(lockInRange: Boolean) {
//        Log.d(TAG, "updateLockInRangeStatus: ")
//        listener.onLockInRange(lockInRange)
//    }
//
//    companion object {
//        private val TAG = ClosestLockTrigger::class.java.name
//    }
//}