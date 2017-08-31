package com.mastercard.gateway.android.sampleapp;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        getFragmentManager().beginTransaction()
                .replace( android.R.id.content, new PrefsFragment() )
                .commit();
    }

    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate( Bundle savedInstanceState ) {
            super.onCreate( savedInstanceState );
            addPreferencesFromResource( R.xml.preferences );
        }
    }
}
