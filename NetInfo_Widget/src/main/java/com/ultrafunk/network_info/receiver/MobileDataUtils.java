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
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.ultrafunk.network_info.R;

import java.lang.reflect.Method;
import java.text.DecimalFormat;

public class MobileDataUtils
{
	static boolean isMobileDataEnabled(Context context)
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

	static void setMobileDataEnabled(Context context, boolean enable)
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

	static boolean isAirplaneModeOn(Context context)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
			return Settings.System.getInt(context.getContentResolver(),	Settings.System.AIRPLANE_MODE_ON, 0) == 1;
		else
			return Settings.Global.getInt(context.getContentResolver(),	Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
	}

	public static boolean isDataRoaming(Context context)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
			return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.DATA_ROAMING, 0) == 1;
		else
			return Settings.Global.getInt(context.getContentResolver(), Settings.Global.DATA_ROAMING, 0) == 1;
	}

	static String getNetworkTypeString(int networkType, boolean shortString)
	{
		switch (networkType)
		{
			case TelephonyManager.NETWORK_TYPE_GPRS:	return shortString ? "2G"	: "GPRS 2G";
			case TelephonyManager.NETWORK_TYPE_EDGE:	return shortString ? "2G"	: "EDGE 2G";
			case TelephonyManager.NETWORK_TYPE_UMTS:	return shortString ? "3G"	: "UMTS 3G";
			case TelephonyManager.NETWORK_TYPE_CDMA:	return shortString ? "3G"	: "CDMA 3G";
			case TelephonyManager.NETWORK_TYPE_EVDO_0:	return shortString ? "3G"	: "EVDO 0 3G";
			case TelephonyManager.NETWORK_TYPE_EVDO_A:	return shortString ? "3G"	: "EVDO A 3G";
			case TelephonyManager.NETWORK_TYPE_EVDO_B:	return shortString ? "3G"	: "EVDO B 3G";
			case TelephonyManager.NETWORK_TYPE_1xRTT:	return shortString ? "3G"	: "1xRTT 3G";
			case TelephonyManager.NETWORK_TYPE_HSDPA:	return shortString ? "3G"	: "HSDPA 3G";
			case TelephonyManager.NETWORK_TYPE_HSUPA:	return shortString ? "3G"	: "HSUPA 3G";
			case TelephonyManager.NETWORK_TYPE_HSPA:	return shortString ? "3G"	: "HSPA 3G";
			case TelephonyManager.NETWORK_TYPE_LTE:		return shortString ? "4G"	: "LTE 4G";
			case TelephonyManager.NETWORK_TYPE_EHRPD:	return shortString ? "3/4G"	: "eHRPD 3/4G";
			case TelephonyManager.NETWORK_TYPE_HSPAP:	return shortString ? "3G"	: "HSPA+ 3G";
		}

		return shortString ? "N/A" : "Unknown";
	}

	private static String readableSize(long size)
	{
		if(size <= 0)
			return "0";

		final String[] units = new String[] { "bytes", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

		return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	static String getDataUsageString(Context context, long dataUsageBytes)
	{
		if (dataUsageBytes <= 0)
			return context.getString(R.string.data_usage_na);

		long elapsedTime = SystemClock.elapsedRealtime();
		int hours = (int) (elapsedTime / (1000 * 60 * 60));

		if (hours < 1)
			return String.format("~ %s", readableSize(dataUsageBytes));

		// Avoid division by zero
		long usagePerHour = dataUsageBytes / hours;

		if (hours < 24)
			return String.format("~ %s / %s", readableSize(usagePerHour), context.getString(R.string.hour));

		int days = hours / 24;

		if (days < 7)
			return String.format("~ %s / %s", readableSize(usagePerHour * 24), context.getString(R.string.day));

		return String.format("~ %s / %s", readableSize(usagePerHour * 24 * 7), context.getString(R.string.week));
	}
}
