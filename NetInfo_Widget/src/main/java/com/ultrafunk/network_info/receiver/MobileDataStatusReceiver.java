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

		if (Constants.ACTION_DATA_CONNECTION_CHANGED.equals(action) ||
			Constants.ACTION_DATA_STATE_CHANGED.equals(action) ||
			Constants.ACTION_SERVICE_STATE_CHANGED.equals(action))
		{
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

			if ((simOperatorName != null) && !simOperatorName.isEmpty())
				return networkOperatorName + " - " + simOperatorName;
		}

		return networkOperatorName;
	}

	@Override
	protected void updateView(Context context, RemoteViews remoteViews, Bundle widgetOptions)
	{
		if (isMobileDataEnabled && isOutOfService)
		{
			remoteViews.setTextColor(R.id.mobileOnOffTextView, context.getResources().getColor(R.color.light_green));
			remoteViews.setTextViewText(R.id.mobileOnOffTextView, context.getString(R.string.on));
			remoteViews.setViewVisibility(R.id.mobileNameTextView, View.VISIBLE);
			remoteViews.setTextViewText(R.id.mobileNameTextView, context.getString(R.string.no_service));
			remoteViews.setViewVisibility(R.id.mobileDetailsTextView, View.GONE);
			return;
		}

		if (dataState == TelephonyManager.DATA_DISCONNECTED)
		{
			remoteViews.setTextColor(R.id.mobileOnOffTextView, context.getResources().getColor(R.color.medium_gray));
			remoteViews.setViewVisibility(R.id.mobileNameTextView, View.VISIBLE);
			remoteViews.setViewVisibility(R.id.mobileDetailsTextView, View.VISIBLE);

			if (isMobileDataEnabled && !isAirplaneModeOn)
			{
				remoteViews.setTextViewText(R.id.mobileOnOffTextView, context.getString(R.string.on));
				remoteViews.setTextViewText(R.id.mobileNameTextView, context.getString(R.string.not_connected));
				remoteViews.setTextViewText(R.id.mobileDetailsTextView, networkOperatorAndServiceProvider);
			}
			else
			{
				remoteViews.setTextViewText(R.id.mobileOnOffTextView, context.getString(R.string.off));

				if (isAirplaneModeOn)
				{
					remoteViews.setTextViewText(R.id.mobileNameTextView, context.getString(R.string.flight_mode));
					remoteViews.setViewVisibility(R.id.mobileDetailsTextView, View.GONE);
				}
				else
				{
					remoteViews.setViewVisibility(R.id.mobileNameTextView, View.GONE);
					remoteViews.setTextViewText(R.id.mobileDetailsTextView, context.getString(R.string.tap_to_change));
				}
			}
		}
		else
		{
			remoteViews.setTextColor(R.id.mobileOnOffTextView, context.getResources().getColor(R.color.light_green));
			remoteViews.setTextViewText(R.id.mobileOnOffTextView, context.getString(R.string.on));
			remoteViews.setViewVisibility(R.id.mobileNameTextView, View.VISIBLE);
			remoteViews.setTextViewText(R.id.mobileNameTextView, networkOperatorAndServiceProvider);
			remoteViews.setViewVisibility(R.id.mobileDetailsTextView, View.VISIBLE);
			remoteViews.setTextViewText(R.id.mobileDetailsTextView, MobileDataUtils.getNetworkTypeString(telephonyManager) + (isRoaming ? " - ".concat(context.getString(R.string.roaming)) : ""));
		}
	}
}
