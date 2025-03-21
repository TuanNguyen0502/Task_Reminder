package hcmute.edu.vn.hongtuan.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.hongtuan.AddNewTask;
import hcmute.edu.vn.hongtuan.MainActivity;
import hcmute.edu.vn.hongtuan.R;
import hcmute.edu.vn.hongtuan.model.TaskModel;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder> {
    private List<TaskModel> taskModelList;
    private MainActivity mainActivity;

    public TaskAdapter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final TaskModel taskModel = taskModelList.get(position);
        holder.title.setText(taskModel.getTitle());
        holder.description.setText(taskModel.getDescription());
        holder.date.setText(taskModel.getDue_time());
    }

    public Context getContext() {
        return mainActivity;
    }

    public void setTasks(List<TaskModel> taskModelList) {
        this.taskModelList = taskModelList;
        notifyDataSetChanged();
    }

    public void deleteTask(int position) {
        TaskModel todoModel = taskModelList.get(position);
//        dataBaseHelper.deleteTask(todoModel.getId());
        mainActivity.deleteTask(todoModel.getId());
        taskModelList.remove(position);
        notifyItemRemoved(position);
    }

    public void editTask(int position) {
        TaskModel todoModel = taskModelList.get(position);
        Bundle bundle = new Bundle();
        bundle.putLong("id", todoModel.getId());
        bundle.putString("title", todoModel.getTitle());
        bundle.putString("description", todoModel.getDescription());
        bundle.putString("date", todoModel.getDue_time());

        AddNewTask addNewTask = new AddNewTask();
        addNewTask.setArguments(bundle);
        addNewTask.show(mainActivity.getSupportFragmentManager(), addNewTask.getTag());
    }

    @Override
    public int getItemCount() {
        return taskModelList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date;
        CheckBox checkBox;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textView_title);
            description = itemView.findViewById(R.id.textView_description);
            date = itemView.findViewById(R.id.textView_date);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
