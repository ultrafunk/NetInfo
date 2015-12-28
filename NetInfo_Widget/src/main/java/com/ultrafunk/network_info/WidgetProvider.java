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

package com.ultrafunk.network_info;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.ultrafunk.network_info.config.WidgetConfig;
import com.ultrafunk.network_info.receiver.MobileDataOnOffReceiver;
import com.ultrafunk.network_info.receiver.MobileDataStatusReceiver;
import com.ultrafunk.network_info.receiver.WifiOnOffReceiver;
import com.ultrafunk.network_info.receiver.WifiStatusReceiver;
import com.ultrafunk.network_info.service.NetworkStateService;
import com.ultrafunk.network_info.util.EnabledWidgets;
import com.ultrafunk.network_info.util.Utils;

public class WidgetProvider extends AppWidgetProvider
{
	@Override
	public void onDisabled(Context context)
	{
		super.onDisabled(context);
		setReceiversAndServiceState(context);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds)
	{
		super.onDeleted(context, appWidgetIds);
		setReceiversAndServiceState(context);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		WidgetConfig widgetConfig = new WidgetConfig(context);

		for (int appWidgetId : appWidgetIds)
		{
			widgetConfig.read(appWidgetId);
			updateWidget(context, appWidgetManager, appWidgetId, widgetConfig);
		}

		setReceiversAndServiceState(context);
	}

	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
	{
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

		if (newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1) == AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN)
		{
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), newOptions.getInt(Constants.PREF_LAYOUT_ID, 0));
			int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);

			setWidgetTextSize(remoteViews, newOptions, (minWidth > 175) ? 14 : 12);
			appWidgetManager.partiallyUpdateAppWidget(appWidgetId, remoteViews);
		}
	}

	private void setWidgetTextSize(RemoteViews remoteViews, Bundle newOptions, int textSize)
	{
		if (newOptions.getBoolean(Constants.PREF_MOBILE_DATA_WIDGET, false))
		{
			remoteViews.setTextViewTextSize(R.id.mobileNameTextView, TypedValue.COMPLEX_UNIT_SP, textSize + 1);
			remoteViews.setTextViewTextSize(R.id.mobileInfoTopTextView, TypedValue.COMPLEX_UNIT_SP, textSize);
			remoteViews.setTextViewTextSize(R.id.mobileInfoBottomTextView, TypedValue.COMPLEX_UNIT_SP, textSize);
		}

		if (newOptions.getBoolean(Constants.PREF_WIFI_WIDGET, false))
		{
			remoteViews.setTextViewTextSize(R.id.wifiNameTextView, TypedValue.COMPLEX_UNIT_SP, textSize + 1);
			remoteViews.setTextViewTextSize(R.id.wifiInfoTopTextView, TypedValue.COMPLEX_UNIT_SP, textSize);
			remoteViews.setTextViewTextSize(R.id.wifiInfoBottomTextView, TypedValue.COMPLEX_UNIT_SP, textSize);
		}
	}

	public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, WidgetConfig widgetConfig)
	{
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), widgetConfig.getLayoutId());

		if (widgetConfig.isLockscreenWidget())
			remoteViews.setInt(widgetConfig.showBothWidgets() ? R.id.keyguardLinearLayout : R.id.containerRelativeLayout, "setGravity", widgetConfig.getLockscreenGravity());

		if (widgetConfig.showMobileDataWidget())
		{
			remoteViews.setInt(R.id.mobileParentRelativeLayout, "setBackgroundColor", Color.argb(widgetConfig.getBackgroundTransparencyAlpha(), 0, 0, 0));

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
				remoteViews.setOnClickPendingIntent(R.id.mobileOnOffRelativeLayout, getBroadcastPendingIntent(context, MobileDataOnOffReceiver.class, Constants.ONCLICK_MOBILE_DATA_ONOFF));
			else
				remoteViews.setOnClickPendingIntent(R.id.mobileOnOffRelativeLayout, getDataUsagePendingIntent(context, getBroadcastPendingIntent(context, MobileDataOnOffReceiver.class, Constants.ONCLICK_MOBILE_DATA_ONOFF)));

			if (widgetConfig.getMobileDataSettingsScreen() == WidgetConfig.MOBILE_DATA_SETTINGS_MOBILE_NETWORK_SETTINGS)
				remoteViews.setOnClickPendingIntent(R.id.mobileChangeRelativeLayout, getSettingsPendingIntent(context, Settings.ACTION_DATA_ROAMING_SETTINGS));
			else
				remoteViews.setOnClickPendingIntent(R.id.mobileChangeRelativeLayout, getDataUsagePendingIntent(context, getSettingsPendingIntent(context, Settings.ACTION_DATA_ROAMING_SETTINGS)));

			broadcastUpdateWidget(context, MobileDataStatusReceiver.class, appWidgetId);
		}

		if (widgetConfig.showWifiWidget())
		{
			remoteViews.setInt(R.id.wifiParentRelativeLayout, "setBackgroundColor", Color.argb(widgetConfig.getBackgroundTransparencyAlpha(), 0, 0, 0));
			remoteViews.setOnClickPendingIntent(R.id.wifiOnOffRelativeLayout, getBroadcastPendingIntent(context, WifiOnOffReceiver.class, Constants.ONCLICK_WIFI_ONOFF));
			remoteViews.setOnClickPendingIntent(R.id.wifiChangeRelativeLayout, getSettingsPendingIntent(context, Settings.ACTION_WIFI_SETTINGS));
			broadcastUpdateWidget(context, WifiStatusReceiver.class, appWidgetId);
		}

		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}

	private void setReceiversAndServiceState(Context context)
	{
		EnabledWidgets enabledWidgets = Utils.getEnabledWidgets(context, AppWidgetManager.getInstance(context));
		enableDisableReceivers(context, enabledWidgets);
		startStopService(context, enabledWidgets);
	}

	public static void enableDisableReceivers(Context context, EnabledWidgets enabledWidgets)
	{
		PackageManager packageManager = context.getPackageManager();

		int receiverEnableDisable = enabledWidgets.mobileData ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		packageManager.setComponentEnabledSetting(new ComponentName(context, MobileDataStatusReceiver.class), receiverEnableDisable, PackageManager.DONT_KILL_APP);

		receiverEnableDisable = enabledWidgets.wifi ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		packageManager.setComponentEnabledSetting(new ComponentName(context, WifiStatusReceiver.class), receiverEnableDisable, PackageManager.DONT_KILL_APP);
	}

	public static void startStopService(Context context, EnabledWidgets enabledWidgets)
	{
		if ((enabledWidgets.mobileData) || (enabledWidgets.wifi))
		{
			Intent intent = new Intent(context, NetworkStateService.class);
			intent.setAction(Constants.ACTION_UPDATE_SERVICE_STATE);
			intent.putExtra(Constants.EXTRA_ENABLED_WIDGETS_MOBILE_DATA, enabledWidgets.mobileData);
			intent.putExtra(Constants.EXTRA_ENABLED_WIDGETS_WIFI, enabledWidgets.wifi);
			context.startService(intent);
		}
		else
		{
			context.stopService(new Intent(context, NetworkStateService.class));
		}
	}

	private static PendingIntent getBroadcastPendingIntent(Context context, Class<?> intentClass, String action)
	{
		Intent intent = new Intent(context, intentClass);
		intent.setAction(action);
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}

	private static PendingIntent getDataUsagePendingIntent(Context context, PendingIntent defaultPendingIntent)
	{
		Intent intent = new Intent();
		intent.setClassName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity");

		PackageManager packageManager = context.getPackageManager();
		ResolveInfo resolveInfo = packageManager.resolveActivity(intent, 0);

		if (resolveInfo != null)
			return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		return defaultPendingIntent;
	}

	private static PendingIntent getSettingsPendingIntent(Context context, String action)
	{
		return PendingIntent.getActivity(context, 0, new Intent(action), PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private static void broadcastUpdateWidget(Context context, Class<?> intentClass, int appWidgetId)
	{
		Intent intent = new Intent(context, intentClass);
		intent.setAction(Constants.ACTION_UPDATE_WIDGET);
		intent.putExtra(Constants.EXTRA_APPWIDGET_ID, appWidgetId);
		context.sendBroadcast(intent);
	}
}
