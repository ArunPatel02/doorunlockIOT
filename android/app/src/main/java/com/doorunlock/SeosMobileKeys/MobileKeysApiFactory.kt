package com.doorunlock.SeosMobileKeys

import com.assaabloy.mobilekeys.api.MobileKeys
import com.assaabloy.mobilekeys.api.ReaderConnectionController
import com.assaabloy.mobilekeys.api.ble.ScanConfiguration

interface MobileKeysApiFactory {
    /**
     * Get the a mobile keys api instance
     * @return
     */
    fun getMobileKeys(): MobileKeys?

    /**
     * Get the a reader connection controller instance
     * @return
     */
    fun getReaderConnectionController(): ReaderConnectionController?

    /**
     * Get the scan configuration instance
     * @return
     */
    fun getScanConfiguration(): ScanConfiguration?
}