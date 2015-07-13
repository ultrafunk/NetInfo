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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.RemoteViews;

import com.ultrafunk.network_info.Constants;
import com.ultrafunk.network_info.R;
import com.ultrafunk.network_info.service.NetworkStateService;

public class MobileDataStatusReceiver extends WidgetBroadcastReceiver
{
	private TelephonyManager telephonyManager = null;
	private int dataState = -1;
	private String networkOperatorAndServiceProvider = null;
	private boolean isMobileDataEnabled = false;
	private boolean isAirplaneModeOn = false;
	private boolean isOutOfService = false;
	private boolean isRoaming = false;
	private long dataUsageBytes = 0;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		final String action = intent.getAction();

	//	Log.e(this.getClass().getSimpleName(), "onReceive(): " + action);

		updateMobileDataViews = true;

		telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		dataState = telephonyManager.getDataState();
		networkOperatorAndServiceProvider = getNetworkOperatorAndServiceProvider(context);
		isMobileDataEnabled = MobileDataUtils.getMobileDataEnabled(context);
		isAirplaneModeOn = MobileDataUtils.getAirplaneModeOn(context);
		isOutOfService = NetworkStateService.isMobileOutOfService();
		isRoaming = getRoaming(context);
		dataUsageBytes = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();

		if (Constants.ACTION_DATA_CONNECTION_CHANGED.equals(action) ||
			Constants.ACTION_DATA_STATE_CHANGED.equals(action) ||
			Constants.ACTION_SERVICE_STATE_CHANGED.equals(action))
		{
			// Needed to get around a known bug in Android 5.x: https://code.google.com/p/android/issues/detail?id=78924
			if ((dataState == TelephonyManager.DATA_CONNECTED) && (dataUsageBytes == 0) && !NetworkStateService.isWaitingForDataUsage())
			{
				NetworkStateService.setWaitingForDataUsage(true);
				Intent serviceIntent = new Intent(context, NetworkStateService.class);
				serviceIntent.setAction(Constants.ACTION_DATA_CONNECTED);
				context.startService(serviceIntent);
			}

			partiallyUpdateWidgets(context);
		}
		else if (Constants.ACTION_DATA_USAGE_UPDATE.equals(action))
		{
			partiallyUpdateWidgets(context);
		}
		else if (Intent.ACTION_SCREEN_ON.equals(action))
		{
			if (dataState == TelephonyManager.DATA_CONNECTED)
				partiallyUpdateWidgets(context);
		}
		else if (Constants.ACTION_UPDATE_WIDGET.equals(action))
		{
			partiallyUpdateWidget(context, AppWidgetManager.getInstance(context), intent.getIntExtra(Constants.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID));
		}
	}

	private boolean getRoaming(Context context)
	{
		if ((dataState == TelephonyManager.DATA_CONNECTED) && telephonyManager.isNetworkRoaming())
		{
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

			if (networkInfo != null)
				return networkInfo.isRoaming();
		}

		return false;
	}

	private String getNetworkOperatorAndServiceProvider(Context context)
	{
		String networkOperatorName = telephonyManager.getNetworkOperatorName();

		if ((networkOperatorName == null) || (networkOperatorName.isEmpty()))
			networkOperatorName = context.getString(R.string.unknown_network);

		if (telephonyManager.isNetworkRoaming())
		{
			String simOperatorName = telephonyManager.getSimOperatorName();

			if ((simOperatorName != null) && !simOperatorName.isEmpty() && !simOperatorName.equalsIgnoreCase(networkOperatorName))
				return networkOperatorName + " - " + simOperatorName;
		}

		return networkOperatorName;
	}

	private void setStateColor(Context context, RemoteViews remoteViews, int state)
	{
		int color = (state == STATE_ON) ? context.getResources().getColor(android.R.color.white) : context.getResources().getColor(R.color.medium_gray);
		remoteViews.setTextColor(R.id.mobileNameTextView, color);
		remoteViews.setInt(R.id.mobileHeaderSpacerTextView, "setBackgroundColor", color);
		remoteViews.setTextColor(R.id.mobileInfoTopTextView, color);
		remoteViews.setTextColor(R.id.mobileInfoBottomTextView, color);
	}

	@Override
	protected void updateView(Context context, RemoteViews remoteViews, Bundle widgetOptions)
	{
		if (isMobileDataEnabled && isOutOfService)
		{
			setStateColor(context, remoteViews, STATE_ON);
			remoteViews.setTextViewText(R.id.mobileNameTextView, context.getString(R.string.mobile_data));
			remoteViews.setImageViewResource(R.id.mobileStateImageView, R.drawable.ic_signal_cellular_enabled);
			remoteViews.setViewVisibility(R.id.mobileInfoTopTextView, View.VISIBLE);
			remoteViews.setTextViewText(R.id.mobileInfoTopTextView, context.getString(R.string.no_service));
			remoteViews.setViewVisibility(R.id.mobileInfoBottomTextView, View.GONE);
			return;
		}

		if (dataState == TelephonyManager.DATA_DISCONNECTED)
		{
			remoteViews.setViewVisibility(R.id.mobileInfoTopTextView, View.VISIBLE);
			remoteViews.setViewVisibility(R.id.mobileInfoBottomTextView, View.GONE);

			if (isMobileDataEnabled && !isAirplaneModeOn)
			{
				setStateColor(context, remoteViews, STATE_ON);
				remoteViews.setTextViewText(R.id.mobileNameTextView, networkOperatorAndServiceProvider);
				remoteViews.setImageViewResource(R.id.mobileStateImageView, R.drawable.ic_signal_cellular_enabled);
				remoteViews.setTextViewText(R.id.mobileInfoTopTextView, context.getString(R.string.not_connected));
			}
			else
			{
				setStateColor(context, remoteViews, STATE_OFF);
				remoteViews.setTextViewText(R.id.mobileNameTextView, context.getString(R.string.mobile_data));
				remoteViews.setImageViewResource(R.id.mobileStateImageView, R.drawable.ic_signal_cellular_off);

				if (isAirplaneModeOn)
					remoteViews.setTextViewText(R.id.mobileInfoTopTextView, context.getString(R.string.flight_mode));
				else
					remoteViews.setTextViewText(R.id.mobileInfoTopTextView, context.getString(R.string.tap_to_change));
			}
		}
		else
		{
			setStateColor(context, remoteViews, STATE_ON);
			remoteViews.setTextViewText(R.id.mobileNameTextView, networkOperatorAndServiceProvider);
			remoteViews.setImageViewResource(R.id.mobileStateImageView, R.drawable.ic_signal_cellular_on);
			remoteViews.setViewVisibility(R.id.mobileInfoTopTextView, View.VISIBLE);
			remoteViews.setTextViewText(R.id.mobileInfoTopTextView, MobileDataUtils.getNetworkTypeString(telephonyManager) + (isRoaming ? " - ".concat(context.getString(R.string.roaming)) : ""));
			remoteViews.setViewVisibility(R.id.mobileInfoBottomTextView, View.VISIBLE);

			final boolean isConnecting = ((dataState == TelephonyManager.DATA_CONNECTING) || NetworkStateService.isWaitingForDataUsage());
			remoteViews.setTextViewText(R.id.mobileInfoBottomTextView, isConnecting ? context.getString(R.string.connecting) : MobileDataUtils.getDataUsageString(context, dataUsageBytes));
		}
	}
}
