package com.jbeslinger.budgetbuddy;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class PresetsDialogFragment extends DialogFragment {

    private ListView listViewPresets;

    private PresetsDialogFragment() {}

    public static PresetsDialogFragment newInstance() {
        PresetsDialogFragment fragment = new PresetsDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", "Presets");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_presets, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String title = getArguments().getString("title", "Untitled");
        getDialog().setTitle(title);

        listViewPresets = (ListView)view.findViewById(R.id.listViewPresets);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.big_list_item, UserManager.currentUser.getPresetNames());
        listViewPresets.setAdapter(arrayAdapter);
        listViewPresets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selection = listViewPresets.getItemAtPosition(position).toString();
                if (selection == "– SAVE PRESET –") {
                    UserManager.currentUser.addPreset();
                    dismiss();
                } else {
                    UserManager.currentUser.changeBudget(selection);
                    dismiss();
                }
            }
        });
        listViewPresets.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String selection = listViewPresets.getItemAtPosition(position).toString();
                if (selection.equals("Default") || selection.equals("– SAVE PRESET –")) {
                    return false;
                } else {
                    showModifyPresetDialog(listViewPresets.getItemAtPosition(position).toString());
                    return true;
                }
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        UserManager.saveUserData(getContext());
        getActivity().recreate();
        super.onDismiss(dialog);
    }

    private void showModifyPresetDialog(String presetName) {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        ModifyPresetDialogFragment fragment = ModifyPresetDialogFragment.newInstance(presetName);
        fragment.show(manager, "fragment_modify_preset");
    }

}
