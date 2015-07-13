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

package com.ultrafunk.network_info.util;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.ultrafunk.network_info.Constants;
import com.ultrafunk.network_info.WidgetProvider;

public class Utils
{
	public static boolean isWifiConnected(Context context)
	{
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

		if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
		{
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();

			if (wifiInfo.getIpAddress() != 0)
				return true;
		}

		return false;
	}

	public static EnabledWidgets getEnabledWidgets(Context context, AppWidgetManager appWidgetManager)
	{
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
		boolean mobileDataWidgets = false, wifiWidgets = false;

		if (appWidgetIds != null)
		{
			for (int appWidgetId : appWidgetIds)
			{
				Bundle widgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);

				if (widgetOptions.getBoolean(Constants.PREF_MOBILE_DATA_WIDGET, false))
					mobileDataWidgets = true;

				if (widgetOptions.getBoolean(Constants.PREF_WIFI_WIDGET, false))
					wifiWidgets = true;
			}
		}

		return new EnabledWidgets(mobileDataWidgets, wifiWidgets);
	}
}
