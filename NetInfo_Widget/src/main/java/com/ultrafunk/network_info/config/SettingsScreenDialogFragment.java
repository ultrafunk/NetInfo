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

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;


import com.ultrafunk.network_info.Constants;
import com.ultrafunk.network_info.R;

import java.util.ArrayList;

public class SettingsScreenDialogFragment extends DialogFragment
{
	public interface DialogListener
	{
		void onDialogSelectionChanged(int selected);
	}

	DialogListener mListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
		Bundle bundle = getArguments();

		alertDialog.setTitle(getString(R.string.settings_screen));
		alertDialog.setPositiveButton(getString(android.R.string.cancel), new PositiveButtonClickListener());

		ArrayList<String> arrayList = new ArrayList<>();
		arrayList.add(getString(R.string.mobile_network_settings));
		arrayList.add(getString(R.string.data_usage));

		alertDialog.setSingleChoiceItems(arrayList.toArray(new CharSequence[arrayList.size()]),
			bundle.getInt(Constants.PREF_MOBILE_DATA_SETTINGS_SCREEN, WidgetConfig.MOBILE_DATA_SETTINGS_MOBILE_NETWORK_SETTINGS),
			selectItemListener);

		return alertDialog.create();
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		mListener = (DialogListener) activity;
	}

	class PositiveButtonClickListener implements DialogInterface.OnClickListener
	{
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			dialog.dismiss();
		}
	}

	DialogInterface.OnClickListener selectItemListener = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			mListener.onDialogSelectionChanged(which);
			dialog.dismiss();
		}
	};
}