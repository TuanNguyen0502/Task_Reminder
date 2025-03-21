package hcmute.edu.vn.hongtuan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import hcmute.edu.vn.hongtuan.broadcastReceiver.TaskReminderReceiver;
import hcmute.edu.vn.hongtuan.model.TaskModel;

public class AddNewTask extends BottomSheetDialogFragment {
    public static final String TAG = "AddNewTask";
    private EditText editText_title, editText_description;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button button_save;
    private MainActivity mainActivity;

    public static AddNewTask newInstance() {
        return new AddNewTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_new_task, container, false);
        return v;
    }

    @SuppressLint("ScheduleExactAlarm")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) getActivity();

        editText_title = view.findViewById(R.id.editText_title);
        editText_description = view.findViewById(R.id.editText_description);
        datePicker = view.findViewById(R.id.datePicker);
        timePicker = view.findViewById(R.id.timePicker);
        button_save = view.findViewById(R.id.button_save);

        boolean isUpdate = false;

        Bundle bundle = getArguments();
        if (bundle != null) {
            isUpdate = true;
            String title = bundle.getString("title");
            String description = bundle.getString("description");
            String date = bundle.getString("date");
            // Split the string to extract year, month, and day
            String[] parts = date.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1; // Month is 0-based
            int day = Integer.parseInt(parts[2]);
            editText_title.setText(title);
            editText_description.setText(description);
            // Set DatePicker to this date
            datePicker.updateDate(year, month, day);
        }

        boolean finalIsUpdate = isUpdate;
        button_save.setOnClickListener(v -> {
            TaskModel taskModel = new TaskModel();
            taskModel.setTitle(editText_title.getText().toString());
            taskModel.setDescription(editText_description.getText().toString());
            String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d %02d:%02d:00",
                    datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth(), timePicker.getHour(), timePicker.getMinute());
            taskModel.setDue_time(formattedDate);
            if (finalIsUpdate) {
//                dataBaseHelper.updateTask(bundle.getInt("id"), title1, description1, date1);
                taskModel.setId((int) bundle.getLong("id"));
                mainActivity.updateTask(taskModel);
            } else {
//                dataBaseHelper.addTask(todoModel);
                mainActivity.insertTask(taskModel.getTitle(), taskModel.getDescription(), taskModel.getDue_time());
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, datePicker.getYear());
                calendar.set(Calendar.MONTH, datePicker.getMonth());
                calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());
                calendar.set(Calendar.SECOND, 0); // Set seconds to 0
                // Convert to milliseconds
                long dueTimeMillis = calendar.getTimeInMillis();
                setTaskReminder(mainActivity, dueTimeMillis, taskModel.getTitle());
            }
            dismiss();
        });
    }

    @SuppressLint("ScheduleExactAlarm")
    public void setTaskReminder(Context context, long dueTimeMillis, String taskTitle) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TaskReminderReceiver.class);
        intent.putExtra("task_title", taskTitle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueTimeMillis, pendingIntent);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener) activity).onDialogClose(dialog);
        }
    }
}
