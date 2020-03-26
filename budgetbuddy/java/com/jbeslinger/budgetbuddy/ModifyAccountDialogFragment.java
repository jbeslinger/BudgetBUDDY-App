package com.jbeslinger.budgetbuddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
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

import java.text.NumberFormat;

public class ModifyAccountDialogFragment extends DialogFragment {

    private EditText editTextAccountName, editTextBalance;
    private Button buttonUpdate, buttonDelete;
    private double parsedBalance;
    private DatabaseManager databaseManager;
    private String startingName;

    private ModifyAccountDialogFragment() {}

    public static ModifyAccountDialogFragment newInstance(int index) {
        ModifyAccountDialogFragment fragment = new ModifyAccountDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", "Modify Account");
        args.putInt("index", index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_modify_account, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final int index = getArguments().getInt("index");
        databaseManager = new DatabaseManager(getContext());
        super.onViewCreated(view, savedInstanceState);
        editTextAccountName = (EditText)view.findViewById(R.id.editTextAccountName);
        editTextAccountName.setText(UserManager.currentUser.accounts.get(index).name);
        startingName = editTextAccountName.getText().toString();
        editTextBalance = (EditText)view.findViewById(R.id.editTextBalance);
        String formatted = NumberFormat.getCurrencyInstance().format((UserManager.currentUser.accounts.get(index).balance));
        editTextBalance.setText(formatted);
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
        buttonUpdate = (Button)view.findViewById(R.id.buttonRename);
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = editTextAccountName.getText().toString();
                if (editTextAccountName.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), R.string.warning_empty_name, Toast.LENGTH_SHORT).show();
                    return;
                } else if (UserManager.currentUser.containsAccount(newName)) {
                    if (startingName.equals(newName)) {
                        UserManager.currentUser.accounts.get(index).balance = parsedBalance;
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), R.string.warning_duplicate_name, Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    databaseManager.open();
                    databaseManager.renameTable(UserManager.currentUser.accounts.get(index), newName);
                    UserManager.currentUser.modifyAccount(index, newName, parsedBalance);
                    databaseManager.close();
                    dismiss();
                }
            }
        });
        buttonDelete = (Button)view.findViewById(R.id.buttonDelete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserManager.currentUser.accounts.size() > 1) { //Delete the account
                    databaseManager.open();
                    showDeleteWarningDialog(index);
                } else { //You can't because it's the last one
                    Toast.makeText(getContext(), R.string.warning_delete_last_account, Toast.LENGTH_SHORT).show();
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

    private void showDeleteWarningDialog(final int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.warning_delete_account);
        builder.setPositiveButton(R.string.misc_okay, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                databaseManager.dropTable(UserManager.currentUser.accounts.get(index));
                UserManager.currentUser.removeAccount(index);
                databaseManager.close();
                dismiss();
            }
        });
        builder.setNegativeButton(R.string.misc_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
