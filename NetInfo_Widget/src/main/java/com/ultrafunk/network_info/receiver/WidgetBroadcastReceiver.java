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

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.ultrafunk.network_info.Constants;
import com.ultrafunk.network_info.WidgetProvider;

public abstract class WidgetBroadcastReceiver extends BroadcastReceiver
{
	protected boolean updateMobileDataViews = false;
	protected boolean updateWifiViews = false;

	protected void partiallyUpdateWidgets(Context context)
	{
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));

		for (int appWidgetId : appWidgetIds)
		{
			partiallyUpdateWidget(context, appWidgetManager, appWidgetId);
		}
	}

	protected void partiallyUpdateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
	{
		if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID)
		{
			Bundle widgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
			int layoutId = widgetOptions.getInt(Constants.PREF_LAYOUT_ID, 0);

			if ((layoutId != 0) && updateThisWidget(widgetOptions))
			{
				RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);
				updateView(context, remoteViews, widgetOptions);
				appWidgetManager.partiallyUpdateAppWidget(appWidgetId, remoteViews);
			}
		}
	}

	private boolean updateThisWidget(Bundle widgetOptions)
	{
		boolean mobileDataWidget = widgetOptions.getBoolean(Constants.PREF_MOBILE_DATA_WIDGET, false);
		boolean wifiWidget = widgetOptions.getBoolean(Constants.PREF_WIFI_WIDGET, false);

		if ((updateMobileDataViews == mobileDataWidget) || (updateWifiViews == wifiWidget))
			return true;

		return false;
	}

	protected abstract void updateView(Context context, RemoteViews remoteViews, Bundle widgetOptions);
}
