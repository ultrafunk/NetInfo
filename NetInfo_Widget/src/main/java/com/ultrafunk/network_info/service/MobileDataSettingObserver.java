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

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;

import com.ultrafunk.network_info.Constants;
import com.ultrafunk.network_info.receiver.MobileDataUtils;
import com.ultrafunk.network_info.util.Utils;

public class MobileDataSettingObserver extends ContentObserver
{
	private final Context context;

	public MobileDataSettingObserver(Context context)
	{
		super(null);
		this.context = context;
	}

	@Override
	public void onChange(boolean selfChange)
	{
		onChange(selfChange, null);
	}

	@Override
	public void onChange(boolean selfChange, Uri uri)
	{
		if (Utils.isWifiConnected(context) || NetworkStateService.isMobileOutOfService() || isMobileDataDisconnectedDueToRoaming())
			LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.ACTION_DATA_STATE_CHANGED));
	}

	// https://android.googlesource.com/platform/packages/apps/Phone/+/ics-mr0/src/com/android/phone/PhoneApp.java
	// Line: 1450 - boolean disconnectedDueToRoaming =
	private boolean isMobileDataDisconnectedDueToRoaming()
	{
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return (MobileDataUtils.getDataRoaming(context) == false) && telephonyManager.isNetworkRoaming();
	}
}
