/*
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
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.ultrafunk.network_info.R;
import com.ultrafunk.network_info.util.Utils;

import java.lang.reflect.Method;

public class MobileDataUtils
{
	public static boolean getMobileDataEnabled(Context context)
	{
		try
		{
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			Method method = connectivityManager.getClass().getDeclaredMethod("getMobileDataEnabled");
			return (Boolean) method.invoke(connectivityManager);
		}
		catch (Exception exception)
		{
			Toast.makeText(context, "Failed to get Mobile data enabled state!", Toast.LENGTH_LONG).show();
		}

		return false;
	}

	public static void setMobileDataEnabled(Context context, boolean enable)
	{
		try
		{
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			Method setMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
			setMobileDataEnabledMethod.setAccessible(true);
			setMobileDataEnabledMethod.invoke(connectivityManager, enable);
		}
		catch (Exception exception)
		{
			Toast.makeText(context, "Failed to enable or disable Mobile data!", Toast.LENGTH_LONG).show();
		}
	}

	public static boolean getAirplaneModeOn(Context context)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
			return Settings.System.getInt(context.getContentResolver(),	Settings.System.AIRPLANE_MODE_ON, 0) == 1;
		else
			return Settings.Global.getInt(context.getContentResolver(),	Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
	}

	public static boolean getDataRoaming(Context context)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
			return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.DATA_ROAMING, 0) == 1;
		else
			return Settings.Global.getInt(context.getContentResolver(), Settings.Global.DATA_ROAMING, 0) == 1;
	}

	public static String getNetworkTypeString(TelephonyManager telephonyManager)
	{
		switch (telephonyManager.getNetworkType())
		{
			case TelephonyManager.NETWORK_TYPE_GPRS:	return "GPRS 2G"; 		// "GPRS 2G ~ 50 kbps"; 	// NETWORK_TYPE_GPRS ~ 100 kbps
			case TelephonyManager.NETWORK_TYPE_EDGE:	return "EDGE 2G"; 		// "EDGE 2.5G ~ 100 kbps";	// NETWORK_TYPE_EDGE ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_UMTS:	return "UMTS 3G"; 		// "UMTS 3G ~ 1 Mbps";		// NETWORK_TYPE_UMTS ~ 400-7000 kbps
			case TelephonyManager.NETWORK_TYPE_CDMA:	return "CDMA 3G"; 		// "CDMA 2G ~ 64 kbps";		// NETWORK_TYPE_CDMA ~ 14-64 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_0:	return "EVDO 0 3G"; 	// "EVDO 0 3G ~ 700 kbps";	// NETWORK_TYPE_EVDO_0 ~ 400-1000 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_A:	return "EVDO A 3G"; 	// "EVDO A 3G ~ 1 Mbps";	// NETWORK_TYPE_EVDO_A ~ 600-1400 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_B:	return "EVDO B 3G";		// "EVDO B 3G ~ 5 Mbps";	// NETWORK_TYPE_EVDO_B ~ 5 Mbps
			case TelephonyManager.NETWORK_TYPE_1xRTT:	return "1xRTT 3G";		// "1xRTT 3G ~ 100 kbps";	// NETWORK_TYPE_1xRTT ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_HSDPA:	return "HSDPA 3G";		// "HSDPA 3G ~ 2-12 Mbps";	// NETWORK_TYPE_HSDPA ~ 2-14 Mbps
			case TelephonyManager.NETWORK_TYPE_HSUPA:	return "HSUPA 3G";		// "HSUPA 3G ~ 1-20 Mbps";	// NETWORK_TYPE_HSUPA ~ 1-23 Mbps
			case TelephonyManager.NETWORK_TYPE_HSPA:	return "HSPA 3G"; 		// "HSPA 3G ~ 1.3 Mbps";	// NETWORK_TYPE_HSPA ~ 700-1700 kbps
			case TelephonyManager.NETWORK_TYPE_LTE:		return "LTE 4G"; 		// "LTE 4G ~ 10+ Mbps";		// NETWORK_TYPE_LTE ~ 10+ Mbps
			case TelephonyManager.NETWORK_TYPE_EHRPD:	return "eHRPD 3/4G"; 	// "eHRPD 3/4G ~ 1.5 Mbps";	// NETWORK_TYPE_EHRPD ~ 1-2 Mbps
			case TelephonyManager.NETWORK_TYPE_HSPAP:	return "HSPA+ 3G"; 		// "HSPA+ 4G ~ 10+ Mbps";	// NETWORK_TYPE_HSPAP ~ 10-20 Mbps
		}

		return "Unknown";
	}

	public static String getDataUsageString(Context context)
	{
		long dataUsageBytes = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();

		// Ugly hack to get around a known bug in Android 5.x:
		// https://code.google.com/p/android/issues/detail?id=78924
		if (dataUsageBytes == 0)
		{
			try
			{
				Thread.sleep(50);
				dataUsageBytes = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();
			}
			catch (Exception e)
			{
			}
		}

		if (dataUsageBytes == 0)
			return context.getString(R.string.no_data_usage);

		long elapsedTime = SystemClock.elapsedRealtime();
		int hours = (int) (elapsedTime / (1000 * 60 * 60));

		if (hours < 1)
			return String.format("~ %s", Utils.readableSize(dataUsageBytes));

		// Avoid division by zero
		long usagePerHour = dataUsageBytes / hours;

		if (hours < 24)
			return String.format("~ %s / %s", Utils.readableSize(usagePerHour), context.getString(R.string.hour));

		int days = hours / 24;

		if (days < 7)
			return String.format("~ %s / %s", Utils.readableSize(usagePerHour * 24), context.getString(R.string.day));

		return String.format("~ %s / %s", Utils.readableSize(usagePerHour * 24 * 7), context.getString(R.string.week));
	}
}
