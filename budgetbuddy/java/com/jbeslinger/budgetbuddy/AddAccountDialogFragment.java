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

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.NumberFormat;

public class AddAccountDialogFragment extends DialogFragment {

    private EditText editTextAccountName, editTextBalance;
    private Button buttonAdd;
    private double parsedBalance;
    private DatabaseManager databaseManager;

    private AddAccountDialogFragment() {}

    public static AddAccountDialogFragment newInstance() {
        AddAccountDialogFragment fragment = new AddAccountDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", "Add Account");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_account, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        databaseManager = new DatabaseManager(getContext());
        super.onViewCreated(view, savedInstanceState);
        editTextAccountName = (EditText)view.findViewById(R.id.editTextAccountName);
        editTextBalance = (EditText)view.findViewById(R.id.editTextBalance);
        editTextBalance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            private String current = "";
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(current)){
                    editTextBalance.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[$,.]", "");
                    parsedBalance = Double.parseDouble(cleanString);
                    parsedBalance /= 100;
                    String formatted = NumberFormat.getCurrencyInstance().format((parsedBalance));
                    current = formatted;
                    editTextBalance.setText(formatted);
                    editTextBalance.setSelection(formatted.length());
                    editTextBalance.addTextChangedListener(this);
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
        buttonAdd = (Button)view.findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextAccountName.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), R.string.warning_empty_name, Toast.LENGTH_SHORT).show();
                    return;
                } else if (UserManager.currentUser.containsAccount(editTextAccountName.getText().toString())) {
                    Toast.makeText(getContext(), R.string.warning_duplicate_name, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    databaseManager.open();
                    UserManager.currentUser.addAccount(editTextAccountName.getText().toString(), parsedBalance);
                    databaseManager.updateTables();
                    databaseManager.close();
                    dismiss();
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
