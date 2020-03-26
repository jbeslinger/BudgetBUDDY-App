package com.jbeslinger.budgetbuddy;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.NumberFormat;

public class AddCategoryDialogFragment extends DialogFragment {

    private EditText editTextCategoryName, editTextLimit;
    private Button buttonAdd;
    private double parsedLimit;

    private AddCategoryDialogFragment() {}

    public static AddCategoryDialogFragment newInstance() {
        AddCategoryDialogFragment fragment = new AddCategoryDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", "Add Category");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_category, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editTextCategoryName = (EditText)view.findViewById(R.id.editTextCategoryName);
        editTextLimit = (EditText)view.findViewById(R.id.editTextLimit);
        editTextLimit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            private String current = "";
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(current)){
                    editTextLimit.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[$,.]", "");
                    parsedLimit = Double.parseDouble(cleanString);
                    parsedLimit /= 100;
                    String formatted = NumberFormat.getCurrencyInstance().format((parsedLimit));
                    current = formatted;
                    editTextLimit.setText(formatted);
                    editTextLimit.setSelection(formatted.length());
                    editTextLimit.addTextChangedListener(this);
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
        buttonAdd = (Button)view.findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextCategoryName.getText().toString().isEmpty()) { //Warn the player if the EditText is empty
                    Toast.makeText(getContext(), R.string.warning_empty_name, Toast.LENGTH_SHORT).show();
                    return;
                } else if (editTextCategoryName.getText().toString().equals("Income")) {
                    Toast.makeText(getContext(), R.string.warning_name_income, Toast.LENGTH_SHORT).show();
                    return;
                } else if (UserManager.currentUser.addCategory(editTextCategoryName.getText().toString(), parsedLimit)){ //Try to add the category
                    dismiss();
                } else { //If it fails, then let the user know
                    Toast.makeText(getContext(), getString(R.string.warning_duplicate_category), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        String title = getArguments().getString("title", "Untitled");
        getDialog().setTitle(title);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        UserManager.saveUserData(getContext());
        getActivity().recreate();
        super.onDismiss(dialog);
    }
}
