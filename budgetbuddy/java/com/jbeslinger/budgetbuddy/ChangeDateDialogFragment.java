package com.jbeslinger.budgetbuddy;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class ChangeDateDialogFragment extends DialogFragment {

    private DatePicker datePickerDate;
    private Button buttonUpdate;

    private ChangeDateDialogFragment() {}

    public static ChangeDateDialogFragment newInstance(int buttonID) {
        ChangeDateDialogFragment fragment = new ChangeDateDialogFragment();
        Bundle args = new Bundle();
        args.putInt("button id", buttonID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_date, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        datePickerDate = (DatePicker)view.findViewById(R.id.datePickerDate);
        buttonUpdate = (Button)view.findViewById(R.id.buttonRename);
        final int buttonID = getArguments().getInt("button id");
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int day = datePickerDate.getDayOfMonth();
                int month = datePickerDate.getMonth();
                int year = datePickerDate.getYear();
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                if (buttonID == R.id.buttonStartDate) {
                    TransactionsActivity.startDate = calendar.getTime();
                    Toast.makeText(getContext(), "Changed start date", Toast.LENGTH_SHORT).show();
                } else if (buttonID == R.id.buttonEndDate) {
                    TransactionsActivity.endDate = calendar.getTime();
                    Toast.makeText(getContext(), "Changed end date", Toast.LENGTH_SHORT).show();
                }
                dismiss();
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        getActivity().recreate();
        super.onDismiss(dialog);
    }
}
