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
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.ultrafunk.network_info.Constants;
import com.ultrafunk.network_info.R;
import com.ultrafunk.network_info.service.NetworkStateService;

public class WifiStatusReceiver extends WidgetBroadcastReceiver
{
	private WifiManager wifiManager = null;
	private int wifiState = -1;
	private WifiInfo wifiInfo = null;
	private String detailsString = "";
	private NetworkInfo.DetailedState detailedState = null;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		final String action = intent.getAction();

	//	Log.e(this.getClass().getSimpleName(), "onReceive(): " + action);

		updateWifiViews = true;

		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wifiState = wifiManager.getWifiState();
		wifiInfo = wifiManager.getConnectionInfo();

		if ((wifiState == WifiManager.WIFI_STATE_ENABLED) && (wifiInfo.getIpAddress() == 0))
			detailedState = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());

		if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action))
		{
			if (isConnectionReady(intent))
			{
				String securityString = WifiUtils.getSecurityString(context, wifiManager, wifiInfo.getBSSID());
				NetworkStateService.setWifiSecurityString(securityString);
				detailsString = "Security: " + securityString;

				Intent serviceIntent = new Intent(context, NetworkStateService.class);
				serviceIntent.setAction(Constants.ACTION_WIFI_CONNECTED);
				context.startService(serviceIntent);
			}

			partiallyUpdateWidgets(context);
		}
		else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action) ||
				 Constants.ACTION_WIFI_SCANNING.equals(action))
		{
			partiallyUpdateWidgets(context);
		}
		else if (Intent.ACTION_SCREEN_ON.equals(action) ||
				 Constants.ACTION_WIFI_LINK_SPEED.equals(action))
		{
			if (isConnected())
			{
				setDetailsString(context);
				partiallyUpdateWidgets(context);
			}
		}
		else if (Constants.ACTION_UPDATE_WIDGET.equals(action))
		{
			if (isConnected())
				setDetailsString(context);

			partiallyUpdateWidget(context, AppWidgetManager.getInstance(context), intent.getIntExtra(Constants.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID));
		}
	}

	private boolean isConnected()
	{
		return ((wifiState == WifiManager.WIFI_STATE_ENABLED) && (wifiInfo.getIpAddress() != 0));
	}

	private boolean isConnectionReady(Intent intent)
	{
		if (isConnected() && (intent.getExtras() != null))
		{
			NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

			if ((networkInfo != null) && (networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED))
				return true;
		}

		return false;
	}

	private void setDetailsString(Context context)
	{
		String securityString = NetworkStateService.getWifiSecurityString();

		if (securityString == null)
			securityString = WifiUtils.getSecurityString(context, wifiManager, wifiInfo.getBSSID());

		if (wifiInfo.getLinkSpeed() != -1)
			detailsString = String.format("%s - %d%s", securityString, wifiInfo.getLinkSpeed(), WifiInfo.LINK_SPEED_UNITS);
		else
			detailsString = "Security: " + securityString;
	}

	@Override
	protected void updateView(Context context, RemoteViews remoteViews, Bundle widgetOptions)
	{
		if ((wifiState == WifiManager.WIFI_STATE_DISABLED) || (wifiState == WifiManager.WIFI_STATE_UNKNOWN))
		{
			remoteViews.setTextColor(R.id.wifiOnOffTextView, context.getResources().getColor(R.color.medium_gray));
			remoteViews.setTextViewText(R.id.wifiOnOffTextView, "OFF");
			remoteViews.setViewVisibility(R.id.wifiNameTextView, View.GONE);
			remoteViews.setViewVisibility(R.id.wifiDetailsTextView, View.VISIBLE);
			remoteViews.setTextViewText(R.id.wifiDetailsTextView, "tap to change");
		}
		else if (wifiState == WifiManager.WIFI_STATE_ENABLED)
		{
			remoteViews.setTextColor(R.id.wifiOnOffTextView, context.getResources().getColor(R.color.light_green));
			remoteViews.setTextViewText(R.id.wifiOnOffTextView, "ON");
			remoteViews.setViewVisibility(R.id.wifiNameTextView, View.VISIBLE);

			if (wifiInfo.getIpAddress() != 0)
			{
				remoteViews.setTextViewText(R.id.wifiNameTextView, wifiInfo.getSSID().replace("\"", ""));
				remoteViews.setTextViewText(R.id.wifiDetailsTextView, detailsString);
				remoteViews.setViewVisibility(R.id.wifiDetailsTextView, detailsString.isEmpty() ? View.GONE : View.VISIBLE);
			}
			else
			{
				remoteViews.setViewVisibility(R.id.wifiDetailsTextView, View.GONE);

				if (detailedState == NetworkInfo.DetailedState.OBTAINING_IPADDR)
					remoteViews.setTextViewText(R.id.wifiNameTextView, "connecting...");
				else
					remoteViews.setTextViewText(R.id.wifiNameTextView, "no network");
			}
		}
	}
}
