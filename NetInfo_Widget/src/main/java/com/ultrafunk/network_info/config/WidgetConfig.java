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

package com.ultrafunk.network_info.config;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;

import com.ultrafunk.network_info.Constants;
import com.ultrafunk.network_info.R;

public class WidgetConfig
{
	private final Context context;
	private SharedPreferences sharedPreferences;

	private boolean isLockscreenWidget;
	private boolean mobileDataWidget;
	private boolean wifiWidget;
	private int layoutId;
	private int lockscreenGravity;
	private int backgroundTransparency;

	public WidgetConfig(Context context)
	{
		this.context = context;
		sharedPreferences = null;

		isLockscreenWidget = false;
		mobileDataWidget = false;
		wifiWidget = false;
		layoutId = R.layout.widget_homescreen;
		lockscreenGravity = Gravity.TOP;
		backgroundTransparency = 25;
	}

	public void read(int appWidgetId)
	{
		sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME + String.valueOf(appWidgetId), Context.MODE_PRIVATE);

		isLockscreenWidget = sharedPreferences.getBoolean(Constants.PREF_IS_LOCKSCREEN_WIDGET, isLockscreenWidget);
		mobileDataWidget = sharedPreferences.getBoolean(Constants.PREF_MOBILE_DATA_WIDGET, mobileDataWidget);
		wifiWidget = sharedPreferences.getBoolean(Constants.PREF_WIFI_WIDGET, wifiWidget);
		layoutId = sharedPreferences.getInt(Constants.PREF_LAYOUT_ID, layoutId);
		lockscreenGravity = sharedPreferences.getInt(Constants.PREF_LOCKSCREEN_GRAVITY, lockscreenGravity);
		backgroundTransparency = sharedPreferences.getInt(Constants.PREF_BACKGROUND_TRANSPARENCY, backgroundTransparency);

		setWidgetOptions(appWidgetId);
	}

	public void write(int appWidgetId)
	{
		if (sharedPreferences != null)
		{
			SharedPreferences.Editor editor = sharedPreferences.edit();

			editor.putBoolean(Constants.PREF_IS_LOCKSCREEN_WIDGET, isLockscreenWidget);
			editor.putBoolean(Constants.PREF_MOBILE_DATA_WIDGET, mobileDataWidget);
			editor.putBoolean(Constants.PREF_WIFI_WIDGET, wifiWidget);
			editor.putInt(Constants.PREF_LAYOUT_ID, layoutId);
			editor.putInt(Constants.PREF_LOCKSCREEN_GRAVITY, lockscreenGravity);
			editor.putInt(Constants.PREF_BACKGROUND_TRANSPARENCY, backgroundTransparency);

			editor.commit();
		}

		setWidgetOptions(appWidgetId);
	}

	private void setWidgetOptions(int appWidgetId)
	{
		if (isLockscreenWidget)
		{
			if (showBothWidgets())
				layoutId = R.layout.widget_keyguard;
			else if (mobileDataWidget)
				layoutId = R.layout.widget_keyguard_mobile;
			else
				layoutId = R.layout.widget_keyguard_wifi;
		}
		else
		{
			if (showBothWidgets())
				layoutId = R.layout.widget_homescreen;
			else if (mobileDataWidget)
				layoutId = R.layout.widget_homescreen_mobile;
			else
				layoutId = R.layout.widget_homescreen_wifi;
		}

		Bundle options = new Bundle();
		options.putBoolean(Constants.PREF_MOBILE_DATA_WIDGET, mobileDataWidget);
		options.putBoolean(Constants.PREF_WIFI_WIDGET, wifiWidget);
		options.putInt(Constants.PREF_LAYOUT_ID, layoutId);

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidgetOptions(appWidgetId, options);
	}

	public boolean isLockscreenWidget()									{ return isLockscreenWidget; }
	public void setLockscreenWidget(boolean isLockscreenWidget)			{ this.isLockscreenWidget = isLockscreenWidget; }

	public boolean showMobileDataWidget()								{ return mobileDataWidget; }
	public void setMobileDataWidget(boolean mobileDataWidget)			{ this.mobileDataWidget = mobileDataWidget; }

	public boolean showWifiWidget()										{ return wifiWidget; }
	public void setWifiWidget(boolean wifiWidget)						{ this.wifiWidget = wifiWidget; }

	public boolean showBothWidgets()									{ return mobileDataWidget && wifiWidget; }
	public void setBothWidgets(boolean bothWidgets)						{ mobileDataWidget = bothWidgets; wifiWidget = bothWidgets; }

	public int getLayoutId()											{ return layoutId; }

	public int getLockscreenGravity()									{ return lockscreenGravity; }
	public void setLockscreenGravity(int lockscreenGravity)				{ this.lockscreenGravity = lockscreenGravity; }

	public int getBackgroundTransparency()								{ return backgroundTransparency; }
	public int getBackgroundTransparencyAlpha()							{ return (int) ((100 - backgroundTransparency) * 2.55); }
	public void setBackgroundTransparency(int backgroundTransparency)	{ this.backgroundTransparency = backgroundTransparency; }
}
