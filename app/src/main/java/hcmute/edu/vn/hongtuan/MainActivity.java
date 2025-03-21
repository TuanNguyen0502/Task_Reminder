package hcmute.edu.vn.hongtuan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hcmute.edu.vn.hongtuan.adapter.TaskAdapter;
import hcmute.edu.vn.hongtuan.model.TaskModel;

public class MainActivity extends AppCompatActivity implements OnDialogCloseListener{
    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;
    private List<TaskModel> taskModelList;
    private TaskAdapter taskAdapter;
    private Uri uri = Uri.parse("content://hcmute.edu.vn.hongtuan.taskcontentprovider/tasks");


    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        floatingActionButton = findViewById(R.id.floatingActionButton_add);
        recyclerView = findViewById(R.id.recycleView);
        taskModelList = new ArrayList<>();
        taskAdapter = new TaskAdapter(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

//        insertTask("Meeting", "Discuss project deadline", (System.currentTimeMillis() / 1000));

        getAllTasks();
        Collections.reverse(taskModelList);
        taskAdapter.setTasks(taskModelList);

        floatingActionButton.setOnClickListener(v -> {
            AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG);
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerViewTouchHelper(taskAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void insertTask(TaskModel taskModel) {
        ContentValues values = new ContentValues();
        values.put("id", taskModel.getId());
        values.put("title", taskModel.getTitle());
        values.put("description", taskModel.getDescription());
        values.put("due_time", taskModel.getDue_time());

        Uri newTaskUri = getContentResolver().insert(uri, values);
        if (newTaskUri != null) {
            Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to add task", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateTask(TaskModel taskModel) {
        ContentValues values = new ContentValues();
        values.put("title", taskModel.getTitle());
        values.put("description", taskModel.getDescription());
        values.put("due_time", taskModel.getDue_time());

        int rowsUpdated = getContentResolver().update(
                Uri.parse(uri + "/" + taskModel.getId()),
                values, null, null
        );

        if (rowsUpdated > 0) {
            Toast.makeText(this, "Task updated!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteTask(long taskId) {
        int rowsDeleted = getContentResolver().delete(
                Uri.parse(Uri.parse("content://hcmute.edu.vn.hongtuan.taskcontentprovider/tasks") + "/" + taskId),
                null, null
        );

        if (rowsDeleted > 0) {
            Toast.makeText(this, "Task deleted!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("Range")
    private void getAllTasks() {
        taskModelList.clear();
        Cursor cursor = getContentResolver().query(uri, null, null, null, "due_time ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                TaskModel taskModel = new TaskModel();
                taskModel.setId(cursor.getLong(cursor.getColumnIndex("id")));
                taskModel.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                taskModel.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                taskModel.setDue_time(cursor.getString(cursor.getColumnIndex("due_time")));
                taskModelList.add(taskModel);
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
//        taskModelList = dataBaseHelper.getAllTasks();
        getAllTasks();
        Collections.reverse(taskModelList);
        taskAdapter.setTasks(taskModelList);
        taskAdapter.notifyDataSetChanged();
    }
}