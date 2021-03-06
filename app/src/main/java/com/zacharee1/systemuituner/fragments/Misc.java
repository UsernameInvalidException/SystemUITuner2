package com.zacharee1.systemuituner.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.zacharee1.systemuituner.MainActivity;
import com.zacharee1.systemuituner.R;

/**
 * Created by Zacha on 4/18/2017.
 */

public class Misc extends Fragment {
    private View view;
    private MainActivity activity;

    private Switch show_full_zen;
    private Switch hu_notif;
    private Switch vol_warn;
    private Switch clock_seconds;
    private Switch battery_percent;
    private Switch power_notifs;

    private Button animApply;
    private Button transApply;
    private Button winApply;
    private Button globalApply;
    private Button secureApply;
    private Button systemApply;

    private TextInputEditText anim;
    private TextInputEditText trans;
    private TextInputEditText win;
    private TextInputEditText custom_global;
    private TextInputEditText custom_secure;
    private TextInputEditText custom_system;

    private CardView custom_settings;

    private final int alertRed = R.drawable.ic_warning_red;

    private String animScale;
    private String transScale;
    private String winScale;
    private String global;
    private String secure;
    private String system;

    private CardView power_notif_controls;

    private boolean customSettingsEnabled;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getActivity() instanceof MainActivity) {
            activity = (MainActivity) getActivity();
        }

        customSettingsEnabled = activity.setThings.sharedPreferences.getBoolean("customSettings", false);

        global = "";
        secure = "";
        system = "";

        view = inflater.inflate(R.layout.fragment_misc, container, false);

        show_full_zen = (Switch) view.findViewById(R.id.show_full_zen);
        hu_notif = (Switch) view.findViewById(R.id.hu_notif);
        vol_warn = (Switch) view.findViewById(R.id.vol_warn);
        power_notifs = (Switch) view.findViewById(R.id.power_notifications);

        custom_settings = (CardView) view.findViewById(R.id.custom_settings);
        custom_settings.setVisibility(customSettingsEnabled ? View.VISIBLE : View.GONE);

        animApply = (Button) view.findViewById(R.id.apply_anim);
        transApply = (Button) view.findViewById(R.id.apply_trans);
        winApply = (Button) view.findViewById(R.id.apply_win);
        globalApply = (Button) view.findViewById(R.id.apply_global);
        secureApply = (Button) view.findViewById(R.id.apply_secure);
        systemApply = (Button) view.findViewById(R.id.apply_system);

        clock_seconds = (Switch) view.findViewById(R.id.clock_seconds);
        battery_percent = (Switch) view.findViewById(R.id.battery_percent);

        //custom switch text
        battery_percent.setText(Html.fromHtml(getResources().getText(R.string.battery_percentage) + "<br /><small> <font color=\"#777777\">" + getResources().getText(R.string.reboot_required) + "</font></small>"));

        power_notif_controls = (CardView) view.findViewById(R.id.power_notification_controls_card);

        if (Build.VERSION.SDK_INT > 23) {
            clock_seconds.setVisibility(View.VISIBLE); //only show switch if user is on Nougat or later
            power_notif_controls.setVisibility(View.VISIBLE); //this is a Nougat feature; only show it on Nougat devices
        } else {
            clock_seconds.setVisibility(View.GONE);
            power_notif_controls.setVisibility(View.GONE);
        }

        anim = (TextInputEditText) view.findViewById(R.id.anim_text);
        trans = (TextInputEditText) view.findViewById(R.id.trans_text);
        win = (TextInputEditText) view.findViewById(R.id.win_text);
        custom_global = (TextInputEditText) view.findViewById(R.id.global_settings);
        custom_secure = (TextInputEditText) view.findViewById(R.id.secure_settings);
        custom_system = (TextInputEditText) view.findViewById(R.id.system_settings);

        animScale = Settings.Global.getString(activity.getContentResolver(), "animator_duration_scale");
        if (animScale == null) animScale = "1.0";
        transScale = Settings.Global.getString(activity.getContentResolver(), "transition_animation_scale");
        if (transScale == null) transScale = "1.0";
        winScale = Settings.Global.getString(activity.getContentResolver(), "window_animation_scale");
        if (winScale == null) winScale = "1.0";

        anim.setHint(getResources().getText(R.string.animator_duration_scale) + " (" + String.valueOf(animScale) + ")");
        trans.setHint(getResources().getText(R.string.transition_animation_scale) + " (" + String.valueOf(transScale) + ")");
        win.setHint(getResources().getText(R.string.window_animation_scale) + " (" + String.valueOf(winScale) + ")");
        custom_global.setHint(getResources().getText(R.string.global));
        custom_secure.setHint(getResources().getText(R.string.secure));
        custom_system.setHint(getResources().getText(R.string.system));

        activity.setThings.switches(show_full_zen, "sysui_show_full_zen", "secure", view); //switch listener
        activity.setThings.switches(hu_notif, "heads_up_notifications_enabled", "global", view);
        activity.setThings.switches(vol_warn, "audio_safe_volume_state", "global", view);

        activity.setThings.switches(clock_seconds, "clock_seconds", "secure", view);
        activity.setThings.switches(battery_percent, "status_bar_show_battery_percent", "system", view);

        activity.setThings.switches(power_notifs, "show_importance_slider", "secure", view);

        buttons(animApply);
        buttons(transApply);
        buttons(winApply);
        buttons(globalApply);
        buttons(secureApply);
        buttons(systemApply);

        textFields(anim);
        textFields(trans);
        textFields(win);
        textFields(custom_global);
        textFields(custom_secure);
        textFields(custom_system);

        return view;
    }

    private void textFields(final TextInputEditText textInputEditText) {
        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (textInputEditText.getText().length() > 0) {
                    final String value = textInputEditText.getText().toString();

                    if (textInputEditText == anim) animScale = value;
                    else if (textInputEditText == trans) transScale = value;
                    else if (textInputEditText == win) winScale = value;
                    else if (textInputEditText == custom_global) global = value;
                    else if (textInputEditText == custom_secure) secure = value;
                    else if (textInputEditText == custom_system) system = value;
                }
            }
        });
    }

    private void buttons(final Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pref;
                String val;
                String type;
                String[] parsedString;

                if (button == animApply) {
                    pref = "animator_duration_scale";
                    if (animScale.contains(".") && !animScale.contains("0.")) animScale = "0" + animScale;
                    if (animScale.indexOf(".") == animScale.length() - 1) animScale = animScale + "0";
                    if (Float.valueOf(animScale) > 10) animScale = "10";
                    val = animScale;
                    type = "global";
                } else if (button == transApply) {
                    pref = "transition_animation_scale";
                    if (transScale.contains(".") && !transScale.contains("0.")) transScale = "0" + transScale;
                    if (transScale.indexOf(".") == transScale.length() - 1) transScale = transScale + "0";
                    if (Float.valueOf(transScale) > 10) transScale = "10";
                    val = transScale;
                    type = "global";
                } else if (button == winApply) {
                    pref = "window_animation_scale";
                    if (winScale.contains(".") && !winScale.contains("0.")) winScale = "0" + winScale;
                    if (winScale.indexOf(".") == winScale.length() - 1) winScale = winScale + "0";
                    if (Float.valueOf(winScale) > 10) winScale = "10";
                    val = winScale;
                    type = "global";
                } else if (button == globalApply) {
                    parsedString = global.split("[ ]");
                    pref = parsedString[0];
                    if (parsedString.length > 1) val = parsedString[1];
                    else val = "";
                    type = "global";
                } else if (button == secureApply) {
                    parsedString = secure.split("[ ]");
                    pref = parsedString[0];
                    if (parsedString.length > 1) val = parsedString[1];
                    else val = "";
                    type = "secure";
                } else if (button == systemApply) {
                    parsedString = system.split("[ ]");
                    pref = parsedString[0];
                    if (parsedString.length > 1) val = parsedString[1];
                    else val = "";
                    type = "system";
                } else {
                    pref = new String();
                    val = new String();
                    type = "";
                }

                activity.setThings.settings(type, pref, val);
            }
        });
    }
}
