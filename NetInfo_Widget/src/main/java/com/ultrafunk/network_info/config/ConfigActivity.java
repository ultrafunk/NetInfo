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
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ultrafunk.network_info.Constants;
import com.ultrafunk.network_info.util.EnabledWidgets;
import com.ultrafunk.network_info.R;
import com.ultrafunk.network_info.util.Utils;
import com.ultrafunk.network_info.WidgetProvider;

public class ConfigActivity extends AppCompatActivity implements SettingsScreenDialogFragment.DialogListener
{
	private AppWidgetManager appAppWidgetManager;
	private WidgetConfig widgetConfig;

	private int appWidgetId;

	private CheckedTextView gravityCheckedTextView;
	private TextView bgTransValTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		/*
		ToDo: So we can show the ConfigActivity when started from a lock screen widget
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		*/

		// Set the result to CANCELED. This will cause the widget host to cancel out of the widget placement if they press the back button.
		setResult(RESULT_CANCELED);

		setContentView(R.layout.activity_config);

		// Find the widget id from the intent.
		Bundle extras = getIntent().getExtras();

		if (extras != null)
			appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

		if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
			finish();

		appAppWidgetManager = AppWidgetManager.getInstance(this);

		widgetConfig = new WidgetConfig(this);
		widgetConfig.read(appWidgetId);

		// ToDo: Needs to change if/when the ConfigActivity is started from a widget or home screen
		widgetConfig.setBothWidgets(true);

		TextView configurationTextView = (TextView) findViewById(R.id.configurationTextView);
		configurationTextView.setText((isLockscreenWidget(appAppWidgetManager, appWidgetId) ? getString(R.string.lockscreen_configuration) : getString(R.string.homescreen_configuration)));

		initShowWidgetView();
		initMobileSettingsScreenView();
		initLockscreenGravityView();
		initTransparencyView();
		initOkAndCancelButtons();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		widgetConfig.write(appWidgetId);
	}

	@Override
	public void onDialogSelectionChanged(int selected)
	{
		TextView mobileCurrentSettingsScreenTextView = (TextView) findViewById(R.id.mobileCurrentSettingsScreenTextView);
		widgetConfig.setMobileDataSettingsScreen(selected);

		if (selected == WidgetConfig.MOBILE_DATA_SETTINGS_MOBILE_NETWORK_SETTINGS)
			mobileCurrentSettingsScreenTextView.setText(getString(R.string.mobile_network_settings));
		else if (selected == WidgetConfig.MOBILE_DATA_SETTINGS_DATA_USAGE)
			mobileCurrentSettingsScreenTextView.setText(getString(R.string.data_usage));
		else if (selected == WidgetConfig.MOBILE_DATA_SETTINGS_TETHERING_SETTINGS)
			mobileCurrentSettingsScreenTextView.setText(getString(R.string.tethering_and_portable_hotspot));
	}

	private void initShowWidgetView()
	{
		RadioGroup showWidgetRadioGroup = (RadioGroup) findViewById(R.id.showWidgetRadioGroup);

		if (widgetConfig.showBothWidgets())
			showWidgetRadioGroup.check(R.id.showBothRadioButton);
		else if (widgetConfig.showMobileDataWidget())
			showWidgetRadioGroup.check(R.id.showMobileRadioButton);
		else
			showWidgetRadioGroup.check(R.id.showWifiRadioButton);

		showWidgetRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
		{
			public void onCheckedChanged(RadioGroup radioGroup, int checkedId)
			{
				RelativeLayout mobileSettingsScreenRelativeLayout = (RelativeLayout) findViewById(R.id.mobileSettingsScreenRelativeLayout);

				widgetConfig.setBothWidgets(false);

				if (checkedId == R.id.showBothRadioButton)
				{
					widgetConfig.setBothWidgets(true);
					mobileSettingsScreenRelativeLayout.setVisibility(View.VISIBLE);
				}

				if (checkedId == R.id.showMobileRadioButton)
				{
					widgetConfig.setMobileDataWidget(true);
					mobileSettingsScreenRelativeLayout.setVisibility(View.VISIBLE);
				}

				if (checkedId == R.id.showWifiRadioButton)
				{
					widgetConfig.setWifiWidget(true);
					mobileSettingsScreenRelativeLayout.setVisibility(View.GONE);
				}
			}
		});
	}

	private void initMobileSettingsScreenView()
	{
		LinearLayout mobileSettingsScreenLinearLayout = (LinearLayout) findViewById(R.id.mobileSettingsScreenLinearLayout);
		onDialogSelectionChanged(widgetConfig.getMobileDataSettingsScreen());

		mobileSettingsScreenLinearLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view) {
				DialogFragment dialogFragment = new SettingsScreenDialogFragment();

				Bundle bundle = new Bundle();
				bundle.putInt(Constants.PREF_MOBILE_DATA_SETTINGS_SCREEN, widgetConfig.getMobileDataSettingsScreen());
				dialogFragment.setArguments(bundle);
				dialogFragment.show(getSupportFragmentManager(), "SettingsScreenDialogFragment");
			}
		});
	}

	private void initLockscreenGravityView()
	{
		if (!isLockscreenWidget(appAppWidgetManager, appWidgetId))
		{
			widgetConfig.setLockscreenWidget(false);
			RelativeLayout lockscreenRelativeLayout = (RelativeLayout) findViewById(R.id.lockscreenRelativeLayout);
			lockscreenRelativeLayout.setVisibility(View.GONE);
		}
		else
		{
			widgetConfig.setLockscreenWidget(true);
			gravityCheckedTextView = (CheckedTextView) findViewById(R.id.gravityCheckedTextView);
			gravityCheckedTextView.setChecked((widgetConfig.getLockscreenGravity() == Gravity.TOP) ? false : true);

			gravityCheckedTextView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					if (gravityCheckedTextView.isChecked())
					{
						widgetConfig.setLockscreenGravity(Gravity.TOP);
						gravityCheckedTextView.setChecked(false);
					}
					else
					{
						widgetConfig.setLockscreenGravity(Gravity.CENTER_VERTICAL);
						gravityCheckedTextView.setChecked(true);
					}
				}
			});
		}
	}

	private void initTransparencyView()
	{
		bgTransValTextView = (TextView) findViewById(R.id.bgTransValTextView);
		bgTransValTextView.setText(String.format("%d%%", widgetConfig.getBackgroundTransparency()));

		SeekBar bgTransSeekBar = (SeekBar) findViewById(R.id.bgTransSeekBar);
		bgTransSeekBar.setProgress(widgetConfig.getBackgroundTransparency());

		bgTransSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				widgetConfig.setBackgroundTransparency(progress);
				bgTransValTextView.setText(String.format("%d%%", progress));
			}
		});
	}

	private void initOkAndCancelButtons()
	{
		Button okButton = (Button) findViewById(R.id.okButton);

		okButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				final Context context = ConfigActivity.this;

				widgetConfig.write(appWidgetId);

				EnabledWidgets enabledWidgets = Utils.getEnabledWidgets(context, appAppWidgetManager);
				WidgetProvider.enableDisableReceivers(context, enabledWidgets);
				WidgetProvider.updateWidget(context, appAppWidgetManager, appWidgetId, widgetConfig);
				WidgetProvider.startStopService(context, enabledWidgets);

				// Make sure we pass back the original appWidgetId
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
				setResult(RESULT_OK, resultValue);
				finish();
			}
		});

		Button cancelButton = (Button) findViewById(R.id.cancelButton);

		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}

	private static boolean isLockscreenWidget(AppWidgetManager appWidgetManager, int appWidgetId)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
			Bundle widgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
			int category = widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1);
			return category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD;
		}

		return false;
	}
}
