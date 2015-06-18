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

public class Constants
{
	public static final String APP_ID = BuildConfig.APPLICATION_ID;

	public static final String ACTION_UPDATE_SERVICE_STATE			= "action.UPDATE_SERVICE_STATE";
	public static final String EXTRA_ENABLED_WIDGETS_MOBILE_DATA	= "extra.ENABLED_WIDGETS_MOBILE_DATA";
	public static final String EXTRA_ENABLED_WIDGETS_WIFI			= "extra.ENABLED_WIDGETS_WIFI";

	// Broadcasts
	// ToDo: Should use permissions to exclude other apps from sending this broadcast to us
	public static final String ACTION_UPDATE_WIDGET	= APP_ID + ".action.UPDATE_WIDGET";
	public static final String EXTRA_APPWIDGET_ID	= APP_ID + ".extra.APPWIDGET_ID";

	public static final String ONCLICK_MOBILE_DATA_ONOFF = APP_ID + ".onclick.MOBILE_DATA_ONOFF";
	public static final String ONCLICK_WIFI_ONOFF		 = APP_ID + ".onclick.WIFI_ONOFF";

	// Local broadcasts
	public static final String ACTION_SERVICE_STATE_CHANGED		= "action.SERVICE_STATE_CHANGED";
	public static final String ACTION_DATA_CONNECTION_CHANGED	= "action.DATA_CONNECTION_CHANGED";
	public static final String ACTION_DATA_STATE_CHANGED		= "action.DATA_STATE_CHANGED";

	public static final String ACTION_WIFI_CONNECTING	= "action.WIFI_CONNECTING";
	public static final String ACTION_WIFI_CONNECTED 	= "action.WIFI_CONNECTED";
	public static final String ACTION_WIFI_SCANNING 	= "action.WIFI_SCANNING";
	public static final String ACTION_WIFI_LINK_SPEED	= "action.WIFI_LINK_SPEED";

	// Prefs
	public static final String PREFS_NAME						= APP_ID + "_";
	public static final String PREF_IS_LOCKSCREEN_WIDGET		= "isLockscreenWidget";
	public static final String PREF_MOBILE_DATA_WIDGET			= "mobileDataWidget";
	public static final String PREF_WIFI_WIDGET					= "wifiWidget";
	public static final String PREF_LAYOUT_ID					= "layoutId";
	public static final String PREF_MOBILE_DATA_SETTINGS_SCREEN	= "mobileDataSettingsScreen";
	public static final String PREF_LOCKSCREEN_GRAVITY			= "lockscreenGravity";
	public static final String PREF_BACKGROUND_TRANSPARENCY		= "backgroundTransparency";
}
