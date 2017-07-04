package com.example.android.fuelapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

/**
 * Created by vishaal on 29/6/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener
{


    public static void setPreferenceSummary(Preference preference, Object value)
    {
        String stringValue= value.toString();
        String key=preference.getKey();

        if(preference instanceof ListPreference)
        {
            ListPreference listPreference=(ListPreference) preference;
            int prefIndex= listPreference.findIndexOfValue(stringValue);
            if(prefIndex>=0)
            {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
        else
        {
            preference.setSummary(stringValue);
        }
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Preference preference= findPreference(key);
        if(preference!=null)
        {
            if(!(preference instanceof CheckBoxPreference))
            {
                setPreferenceSummary(preference, sharedPreferences.getString(key,""));
            }
        }

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_general);
        SharedPreferences sharedPreferences=getPreferenceScreen().getSharedPreferences();
        PreferenceScreen preferenceScreen=getPreferenceScreen();
        int count= preferenceScreen.getPreferenceCount();
        for(int i=0;i<count;i++)

        {
            Preference p=preferenceScreen.getPreference(i);
            if(!(p instanceof CheckBoxPreference))
            {
                String value= sharedPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
