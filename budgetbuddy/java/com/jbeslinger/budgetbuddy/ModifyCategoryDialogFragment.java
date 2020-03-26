package com.jbeslinger.budgetbuddy;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.NumberFormat;

public class ModifyCategoryDialogFragment extends DialogFragment {

    private EditText editTextCategoryName, editTextLimit;
    private Button buttonUpdate, buttonDelete;
    private double parsedLimit;

    private ModifyCategoryDialogFragment() {}

    public static ModifyCategoryDialogFragment newInstance(String key) {
        ModifyCategoryDialogFragment fragment = new ModifyCategoryDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", "Modify Category");
        args.putString("key", key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_modify_category, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final String key = getArguments().getString("key");
        super.onViewCreated(view, savedInstanceState);
        editTextCategoryName = (EditText)view.findViewById(R.id.editTextCategoryName);
        editTextCategoryName.setText(key);
        editTextLimit = (EditText)view.findViewById(R.id.editTextLimit);
        parsedLimit = UserManager.currentUser.budget.get(key);
        String formatted = NumberFormat.getCurrencyInstance().format(parsedLimit);
        editTextLimit.setText(formatted);
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
        buttonUpdate = (Button)view.findViewById(R.id.buttonRename);
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextCategoryName.getText().toString().equals(key)) { //If the user just wants to change the limit
                    UserManager.currentUser.updateBudgetAmount(key, parsedLimit);
                    dismiss();
                } else if (editTextCategoryName.getText().toString().equals("Income")) {
                    Toast.makeText(getContext(), R.string.warning_name_income, Toast.LENGTH_SHORT).show();
                    return;
                } else if (UserManager.currentUser.updateBudgetAmount(key, editTextCategoryName.getText().toString(), parsedLimit)) { //If the user wants to change the name and the limit
                    dismiss();
                } else { //If either one of those fails
                    Toast.makeText(getContext(), getString(R.string.warning_duplicate_category), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        buttonDelete = (Button)view.findViewById(R.id.buttonDelete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserManager.currentUser.removeCategory(editTextCategoryName.getText().toString())) {
                    dismiss();
                } else {
                    Toast.makeText(getContext(), getString(R.string.warning_delete_last_category), Toast.LENGTH_SHORT).show();
                    return;
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

}
