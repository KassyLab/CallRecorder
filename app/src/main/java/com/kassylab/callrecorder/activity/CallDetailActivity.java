/*
 * Copyright (C) 2017  KassyLab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kassylab.callrecorder.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.SubtitleCollapsingToolbarLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.kassylab.callrecorder.R;
import com.kassylab.callrecorder.fragment.CallDetailFragment;
import com.kassylab.callrecorder.fragment.CallListFragment;

/**
 * An activity representing a single Call detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link CallListFragment}.
 */
public class CallDetailActivity extends AppCompatActivity implements
		CallDetailFragment.OnCallDetailInteractionListener {
	
	public static final String EXTRA_ITEM_URI =
			CallDetailActivity.class.getCanonicalName() + ".extras.ITEM_URI";
	public static final String EXTRA_ITEM_POSITION =
			CallDetailActivity.class.getCanonicalName() + ".extras.ITEM_POSITION";
	private Menu mOptionMenu;
	
	public static Intent newIntent(Context context, Uri itemUri, int position) {
		Intent intent = new Intent(context, CallDetailActivity.class);
		intent.putExtra(EXTRA_ITEM_URI, itemUri);
		intent.putExtra(EXTRA_ITEM_POSITION, position);
		return intent;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call_detail);
		Toolbar toolbar = findViewById(R.id.detail_toolbar);
		setSupportActionBar(toolbar);
		
		// Show the Up button in the action bar.
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		
		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Uri itemUri = getIntent().getParcelableExtra(EXTRA_ITEM_URI);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.call_detail_container, CallDetailFragment.newInstance(itemUri))
					.commit();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			/*
			 * This ID represents the Home or Up button. In the case of this
			 * activity, the Up button is shown. Use NavUtils to allow users
			 * to navigate up one level in the application structure. For
			 * more details, see the Navigation pattern on Android Design:
			 *
			 * http://developer.android.com/design/patterns/navigation.html#up-vs-back
			 */
			NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void setActionBarTitle(String title, String subtitle) {
		SubtitleCollapsingToolbarLayout toolbarlayout = findViewById(R.id.toolbar_layout);
		toolbarlayout.setTitle(title);
		toolbarlayout.setSubtitle(subtitle);
	}
	
	@Override
	public void onDeleteCall(Uri itemUri) {
		//TODO: Remove file
		getContentResolver().delete(itemUri, null, null);
	}
}
