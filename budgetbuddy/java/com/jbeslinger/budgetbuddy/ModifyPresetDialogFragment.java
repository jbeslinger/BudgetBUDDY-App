package com.jbeslinger.budgetbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ModifyPresetDialogFragment extends DialogFragment {

    private ModifyPresetDialogFragment() {}

    public static ModifyPresetDialogFragment newInstance(String presetNameToChange) {
        ModifyPresetDialogFragment fragment = new ModifyPresetDialogFragment();
        Bundle args = new Bundle();
        args.putString("presetNameToChange", presetNameToChange);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_modify_presets, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().setTitle("Modify Preset");
        final String presetNameToChange = getArguments().getString("presetNameToChange");

        final EditText editTextPresetName = (EditText)view.findViewById(R.id.editTextPresetName);
        editTextPresetName.setText(presetNameToChange);

        Button buttonRename = (Button)view.findViewById(R.id.buttonRename);
        buttonRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = editTextPresetName.getText().toString();

                if (input.isEmpty()) {
                    Toast.makeText(getContext(), "You must provide a name.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (input.equals("Default") || input.equals("– SAVE PRESET –")) {
                    Toast.makeText(getContext(), "You can't name your preset that.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (UserManager.currentUser.presets.containsKey(input)) {
                    Toast.makeText(getContext(), "A preset of that name already exists.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    UserManager.currentUser.renamePreset(presetNameToChange, input);
                    dismiss();
                }
            }
        });

        Button buttonDelete = (Button)view.findViewById(R.id.buttonDelete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserManager.currentUser.removePreset(presetNameToChange);
                dismiss();
            }
        });
    }

}
