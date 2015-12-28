/*
 * Copyright 2010 The Android Open Source Project
 * Copyright 2014 ultrafunk.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ultrafunk.network_info.receiver;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.ultrafunk.network_info.R;

public class WifiUtils
{
	public static String getIpAddressString(int ipAddress)
	{
		return String.format("IP: %d.%d.%d.%d",	(ipAddress & 0xff),	(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
	}

	public static String getSecurityString(Context context, WifiManager wifiManager, String connectedBSSID)
	{
		ScanResult connectedScanResult = null;

		for (ScanResult scanResult : wifiManager.getScanResults())
		{
			if (scanResult.BSSID.equals(connectedBSSID))
			{
				connectedScanResult = scanResult;
				break;
			}
		}

		if (connectedScanResult != null)
			return getSecurityString(context, connectedScanResult);
		else
			return context.getString(R.string.not_available);
	}

	// START: android-4.4.4_r2.0.1
	// https://android.googlesource.com/platform/packages/apps/Settings/+/android-4.4.4_r2.0.1/src/com/android/settings/wifi/AccessPoint.java

	private static final int SECURITY_NONE = 0;
	private static final int SECURITY_WEP = 1;
	private static final int SECURITY_PSK = 2;
	private static final int SECURITY_EAP = 3;

	private enum PskType
	{
		UNKNOWN,
		WPA,
		WPA2,
		WPA_WPA2
	}

	private static String getSecurityString(Context context, ScanResult scanResult)	{

		int security = getSecurity(scanResult);
		PskType pskType = getPskType(scanResult);

		switch(security) {
			case SECURITY_EAP:
				return context.getString(R.string.wifi_security_eap);
			case SECURITY_PSK:
				switch (pskType) {
					case WPA:
						return context.getString(R.string.wifi_security_wpa);
					case WPA2:
						return context.getString(R.string.wifi_security_wpa2);
					case WPA_WPA2:
						return context.getString(R.string.wifi_security_wpa_wpa2);
					case UNKNOWN:
					default:
						return context.getString(R.string.wifi_security_psk_generic);
				}
			case SECURITY_WEP:
				return context.getString(R.string.wifi_security_wep);
			case SECURITY_NONE:
			default:
				return context.getString(R.string.wifi_security_none);
		}
	}

	private static int getSecurity(ScanResult result) {
		if (result.capabilities.contains("WEP")) {
			return SECURITY_WEP;
		} else if (result.capabilities.contains("PSK")) {
			return SECURITY_PSK;
		} else if (result.capabilities.contains("EAP")) {
			return SECURITY_EAP;
		}
		return SECURITY_NONE;
	}

	private static PskType getPskType(ScanResult result) {
		boolean wpa = result.capabilities.contains("WPA-PSK");
		boolean wpa2 = result.capabilities.contains("WPA2-PSK");
		if (wpa2 && wpa) {
			return PskType.WPA_WPA2;
		} else if (wpa2) {
			return PskType.WPA2;
		} else if (wpa) {
			return PskType.WPA;
		} else {
			return PskType.UNKNOWN;
		}
	}

	// END: android-4.4.4_r2.0.1
}
