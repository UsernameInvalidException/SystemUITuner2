package com.zacharee1.systemuituner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Zacha on 4/19/2017.
 */

public class SetThings {
    public final boolean Dark;
    public final boolean setup;

    public final int titleText;
    public final int style;
    final ColorStateList drawerItem;

    public final SharedPreferences sharedPreferences;
    public final SharedPreferences.Editor editor;

    final ArrayList<Integer> pages;

    private final Activity currentActivity;

    private final Context context;

    private final Exceptions exceptions;

    SetThings(Activity activity) {
        //set all variables
        sharedPreferences = activity.getSharedPreferences(activity.getResources().getText(R.string.sharedprefs_id).toString(), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
        Dark = sharedPreferences.getBoolean("isDark", false);
        setup = sharedPreferences.getBoolean("isSetup", false);
        exceptions = new Exceptions();

        titleText = activity.getResources().getColor(Dark ? android.R.color.primary_text_dark : android.R.color.primary_text_light);
        drawerItem = Dark ? activity.getResources().getColorStateList(R.color.drawer_item_dark) : activity.getResources().getColorStateList(R.color.drawer_item_light);

//        activity.setTheme(SetupActivity.class == activity.getClass() || NoRootSystemSettingsActivity.class == activity.getClass() ? Dark ? R.style.DARK : R.style.AppTheme : Dark ? R.style.DARK_NoAppBar : R.style.AppTheme_NoActionBar);
        activity.setTheme(Dark ? R.style.DARK : R.style.AppTheme);

        style = Dark ? R.style.DARK_NoAppBar : R.style.AppTheme_NoActionBar; //is dark mode on?

        pages = new ArrayList<Integer>() {{ //all (currently used) fragments
                add(R.id.nav_home);
                add(R.id.nav_statusbar);
                add(R.id.nav_demo_mode);
                add(R.id.nav_about);
                add(R.id.nav_settings);
                add(R.id.nav_misc);
            }};

        currentActivity = activity;

        context = currentActivity; //kinda pointless...
    }

    public void buttons(final Button button, final String name) { //set button listeners
        button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("InlinedApi")
            @Override
            public void onClick(View v) {
                Intent intent;
                try {
                    switch (name) {
                        case "setup":
                            intent = new Intent(currentActivity.getApplicationContext(), SetupActivity.class);
                            currentActivity.startActivity(intent);
                            currentActivity.finish();
                            break;
                        case "enableDemo":
                            settings("global", "sysui_demo_allowed", "1");
                            break;
                        case "setupDoneRoot":
                            if (testSudo()) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        sudo("pm grant com.zacharee1.systemuituner android.permission.DUMP ; pm grant com.zacharee1.systemuituner android.permission.WRITE_SECURE_SETTINGS");
                                    }
                                }).start();
                                editor.putBoolean("isRooted", true);
                                editor.putBoolean("isSetup", true);
                                editor.apply();
                                intent = new Intent(currentActivity.getApplicationContext(), MainActivity.class);
                                currentActivity.startActivity(intent);
                                currentActivity.finish();
                                break;
                            } else {
                                intent = new Intent(currentActivity.getApplicationContext(), NoRootActivity.class);
                                currentActivity.startActivity(intent);
                                currentActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(currentActivity.getApplicationContext(), currentActivity.getResources().getText(R.string.root_test_failed), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                currentActivity.finish();
                                break;
                            }
                        case "setupDone":
                            editor.putBoolean("isRooted", false);
                            editor.putBoolean("isSetup", true);
                            editor.apply();
                            intent = new Intent(currentActivity.getApplicationContext(), MainActivity.class);
                            currentActivity.startActivity(intent);
                            currentActivity.finish();
                            break;
                        case "SystemSettingsPerms":
                            intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            intent.setData(Uri.parse("package:" + currentActivity.getPackageName()));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            currentActivity.startActivity(intent);
                            break;
                        case "WriteSystemSettings":
                            Settings.System.putString(currentActivity.getContentResolver(), sharedPreferences.getString("systemSettingKey", ""), sharedPreferences.getString("isSystemSwitchEnabled", "0"));
                            break;
                        case "reset_blacklist":
                            Settings.Secure.putString(currentActivity.getContentResolver(), "icon_blacklist", "");
                            intent = new Intent("check_statbar_toggles");
                            currentActivity.sendBroadcast(intent);
                            break;
                        case "root_setup":
                            intent = new Intent(currentActivity.getApplicationContext(), RootActivity.class);
                            currentActivity.startActivity(intent);
                            currentActivity.finish();
                            break;
                        case "no_root_setup":
                            intent = new Intent(currentActivity.getApplicationContext(), NoRootActivity.class);
                            currentActivity.startActivity(intent);
                            currentActivity.finish();
                            break;
                    }
                } catch (Exception e) {
                    exceptions.systemSettings(context, currentActivity.getApplicationContext(), e.getMessage(), "SetThings");
                }
            }
        });
    }

    public void switches(final Switch toggle, final String pref, final String settingType, final View view) { //set switch listeners

        //check to see if switch should be toggled
        int setting = 0;
        switch (settingType) {
            case "global":
                setting = Settings.Global.getInt(currentActivity.getContentResolver(), pref, 0);
                break;
            case "secure":
                setting = Settings.Secure.getInt(currentActivity.getContentResolver(), pref, 0);
                break;
            case "system":
                setting = Settings.System.getInt(currentActivity.getContentResolver(), pref, 0);
                break;
            case "icon_blacklist":
                String blacklist = Settings.Secure.getString(currentActivity.getContentResolver(), "icon_blacklist") != null ? Settings.Secure.getString(currentActivity.getContentResolver(), "icon_blacklist") : "nada";
                if (!blacklist.contains("," + pref + ",") && !blacklist.contains("," + pref)) {
                    setting = 1;
                } else setting = 0;
                break;
            case "dark_mode":
                setting = Dark ? 1 : 0;
                break;
        }
        toggle.setChecked(setting == 1);

        //set switch listeners
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                try {
                    switch (settingType) {
                        case "icon_blacklist":
                            String blacklist = Settings.Secure.getString(currentActivity.getContentResolver(), "icon_blacklist");
                            if (!isChecked) {
                                if (blacklist != null && !blacklist.equals("")) blacklist = blacklist.concat("," + pref);
                                else blacklist = pref;
                            } else {
                                if (blacklist != null) {
                                    blacklist = blacklist.replace("," + pref, ",");
                                    blacklist = blacklist.replace("," + pref + ",", ",");
                                }
                            }

                            final String blacklist2 = blacklist;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Looper.prepare();
                                    try {
                                        if (blacklist2 != null && !blacklist2.equals("")) {
                                            try {
                                                Settings.Secure.putString(currentActivity.getContentResolver(), "icon_blacklist", blacklist2);
                                                Settings.Secure.putString(currentActivity.getContentResolver(), "icon_blacklist2", blacklist2);
                                            } catch (final Exception e) {
                                                currentActivity.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        exceptions.secureSettings(view.getContext(), currentActivity.getApplicationContext(), e.getMessage(), "icon_blacklist");
                                                    }
                                                });
                                            }
                                        } else {
                                            try {
                                                Settings.Secure.putString(currentActivity.getContentResolver(), "icon_blacklist", "");
                                                Settings.Secure.putString(currentActivity.getContentResolver(), "icon_blacklist2", "");
                                            } catch (final Exception e) {
                                                currentActivity.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        exceptions.secureSettings(view.getContext(), currentActivity.getApplicationContext(), e.getMessage(), "icon_blacklist");
                                                    }
                                                });
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e("idk", e.getMessage());
                                    }
                                }
                            }).start();
                            break;
                        case "dark_mode":
                            editor.putBoolean("isDark", isChecked);
                            editor.apply();
//                            currentActivity.finish();
//                            currentActivity.startActivity(new Intent(currentActivity, currentActivity.getClass()));
                            currentActivity.recreate();
                            break;
                        default:
                            settings(settingType, pref, isChecked ? "1" : "0");
                            break;
                    }
                } catch (Exception e) {
                    exceptions.secureSettings(view.getContext(), currentActivity.getApplicationContext(), e.getMessage(), "Status Bar");
                }
            }
        });
    }

    public void settings(final String type, final String pref, final String value) { //write to settings
        try {
            switch (type) {
                case "global":
                    Settings.Global.putString(currentActivity.getContentResolver(), pref, value);
                    break;
                case "secure":
                    Settings.Secure.putString(currentActivity.getContentResolver(), pref, value);
                    break;
                case "system":
                    if (sharedPreferences.getBoolean("isRooted", true)) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                sudo("settings put system " + pref + " " + value);
                            }
                        }).start();
                    }
                    else {
                        editor.putString("isSystemSwitchEnabled", value);
                        editor.putString("systemSettingKey", pref);
                        editor.apply();
                        Intent intent = new Intent(currentActivity.getApplicationContext(), NoRootSystemSettingsActivity.class);
                        currentActivity.startActivity(intent);
                        currentActivity.finish();
                    }
            }
        } catch (Exception e) {
            exceptions.secureSettings(context, currentActivity.getApplicationContext(), e.getMessage(), "SetThings");
        }
    }

    boolean isPackageInstalled(String packagename, PackageManager packageManager) { //check to see if a
        try {
            packageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void sudo(String... strings) {
        try{
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            for (String s : strings) {
                outputStream.writeBytes(s+"\n");
                outputStream.flush();
            }

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            try {
                su.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("No Root?", e.getMessage());
            }
            outputStream.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private boolean testSudo() {
        StackTraceElement[] stackTrace = new StackTraceElement[] { null };
        try{
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();

            //    DataInputStream inputStream = new DataInputStream(su.getInputStream());
            //    BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            //    StringBuilder total = new StringBuilder();
            //    String line;
            //            while ((line = r.readLine()) != null) {
            //        total.append(line).append('\n');
            //    }
            //
            //    inputStream = new DataInputStream(su.getErrorStream());
            //    r = new BufferedReader(new InputStreamReader(inputStream));
            //            while ((line = r.readLine()) != null) {
            //        total.append(line).append('\n');
            //    }
        } catch (Exception e) {
            stackTrace = e.getStackTrace();
            e.printStackTrace();
        }

        return stackTrace[0] == null;
    }
}
