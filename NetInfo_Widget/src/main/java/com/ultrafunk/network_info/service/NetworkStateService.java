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

package com.ultrafunk.network_info.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.ultrafunk.network_info.Constants;
import com.ultrafunk.network_info.util.EnabledWidgets;
import com.ultrafunk.network_info.util.Utils;
import com.ultrafunk.network_info.receiver.MobileDataStatusReceiver;
import com.ultrafunk.network_info.receiver.WifiStatusReceiver;

public class NetworkStateService extends Service
{
	private LocalBroadcastManager localBroadcastManager;
	private TelephonyManager telephonyManager;
	private WifiManager wifiManager;

	private MobileDataStatusReceiver mobileDataStatusReceiver;
	private MobileDataStateListener mobileDataStateListener;
	private ContentObserver mobileDataSettingObserver;
	private Uri mobileDataSettingUri;

	private WifiStatusReceiver wifiStatusReceiver;

	private static boolean isMobileOutOfService = false;
	private static String wifiSecurityString = null;

	@Override
	public void onCreate()
	{
		super.onCreate();

	//	Log.e(this.getClass().getSimpleName(), "onCreate()");

		localBroadcastManager = LocalBroadcastManager.getInstance(this);
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
			mobileDataSettingUri = Uri.withAppendedPath(Settings.System.CONTENT_URI, "mobile_data");
		else
			mobileDataSettingUri = Uri.withAppendedPath(Settings.Global.CONTENT_URI, "mobile_data");

		initEnabledWidgets(Utils.GetEnabledWidgets(this, AppWidgetManager.getInstance(this)));
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if ((intent != null) && (intent.getAction() != null))
		{
			final String action = intent.getAction();

		//	Log.e(this.getClass().getSimpleName(), "onStartCommand(): " + action);

			if (Constants.ACTION_UPDATE_SERVICE_STATE.equals(action))
			{
				initEnabledWidgets(new EnabledWidgets(intent.getBooleanExtra(Constants.EXTRA_ENABLED_WIDGETS_MOBILE_DATA, false),
													  intent.getBooleanExtra(Constants.EXTRA_ENABLED_WIDGETS_WIFI, false)));
			}
			else
			{
				final Handler handler = new Handler();

				if (Constants.ACTION_WIFI_CONNECTING.equals(action))
				{
					handler.postDelayed(new Runnable()
					{
						public void run()
						{
							WifiInfo wifiInfo = wifiManager.getConnectionInfo();

							if ((wifiInfo != null) && (wifiInfo.getSupplicantState() == SupplicantState.SCANNING))
								localBroadcastManager.sendBroadcastSync(new Intent(Constants.ACTION_WIFI_SCANNING));
						}
					}, 5 * 1000);
				}
				else if (Constants.ACTION_WIFI_CONNECTED.equals(action))
				{
					handler.postDelayed(new Runnable()
					{
						public void run()
						{
							WifiInfo wifiInfo = wifiManager.getConnectionInfo();

							if ((wifiInfo != null) && (wifiInfo.getLinkSpeed() != -1))
								localBroadcastManager.sendBroadcastSync(new Intent(Constants.ACTION_WIFI_LINK_SPEED));
						}
					}, 3 * 1000);
				}
			}
		}

		return Service.START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

	//	Log.e(this.getClass().getSimpleName(), "onDestroy()");

		mobileDataDestroy();
		wifiDestroy();
	}

	@Override
    public IBinder onBind(Intent intent)
	{
		return null;
	}

	public static boolean isMobileOutOfService() 						{ return isMobileOutOfService; }
	public static void setMobileOutOfService(boolean isOutOfService)	{ NetworkStateService.isMobileOutOfService = isOutOfService; }

	public static String getWifiSecurityString() 						{ return wifiSecurityString; }
	public static void setWifiSecurityString(String wifiSecurityString) { NetworkStateService.wifiSecurityString = wifiSecurityString; }

	private void initEnabledWidgets(EnabledWidgets enabledWidgets)
	{
		mobileDataDestroy();
		wifiDestroy();

		if (enabledWidgets.mobileData)
			mobileDataInit();

		if (enabledWidgets.wifi)
			wifiInit();
	}

	private void mobileDataInit()
	{
		mobileDataStatusReceiver = new MobileDataStatusReceiver();
		mobileDataStateListener = new MobileDataStateListener(this);
		mobileDataSettingObserver = new MobileDataSettingObserver(this);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_SERVICE_STATE_CHANGED);
		intentFilter.addAction(Constants.ACTION_DATA_CONNECTION_CHANGED);
		intentFilter.addAction(Constants.ACTION_DATA_STATE_CHANGED);
		localBroadcastManager.registerReceiver(mobileDataStatusReceiver, intentFilter);

		telephonyManager.listen(mobileDataStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE | PhoneStateListener.LISTEN_SERVICE_STATE);
		getContentResolver().registerContentObserver(mobileDataSettingUri, false, mobileDataSettingObserver);
	}

	private void mobileDataDestroy()
	{
		if (mobileDataStatusReceiver != null)
		{
			telephonyManager.listen(mobileDataStateListener, PhoneStateListener.LISTEN_NONE);
			mobileDataStateListener = null;

			getContentResolver().unregisterContentObserver(mobileDataSettingObserver);
			mobileDataSettingObserver = null;

			localBroadcastManager.unregisterReceiver(mobileDataStatusReceiver);
			mobileDataStatusReceiver = null;
		}
	}

	private void wifiInit()
	{
		wifiStatusReceiver = new WifiStatusReceiver();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_WIFI_SCANNING);
		intentFilter.addAction(Constants.ACTION_WIFI_LINK_SPEED);
		localBroadcastManager.registerReceiver(wifiStatusReceiver, intentFilter);

		registerReceiver(wifiStatusReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
	}

	private void wifiDestroy()
	{
		if (wifiStatusReceiver != null)
		{
			localBroadcastManager.unregisterReceiver(wifiStatusReceiver);
			unregisterReceiver(wifiStatusReceiver);
			wifiStatusReceiver = null;
		}
	}
}
