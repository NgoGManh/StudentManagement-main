package com.huflit.studentmanagement.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.studentmanagement.databinding.ItemContainerClassBinding;
import com.huflit.studentmanagement.databinding.ItemContainerStudentBinding;
import com.huflit.studentmanagement.listeners.ClassListener;
import com.huflit.studentmanagement.listeners.StudentListener;
import com.huflit.studentmanagement.models.Class;
import com.huflit.studentmanagement.models.Student;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
    private final List<Student> students;
    private final StudentListener studentListener;
    private final String layout;
    private final PreferenceManager preferenceManager;

    public StudentAdapter(List<Student> students, StudentListener studentListener, String layout, PreferenceManager preferenceManager) {
        this.students = students;
        this.studentListener = studentListener;
        this.layout = layout;
        this.preferenceManager = preferenceManager;
    }

    @NonNull
    @Override
    public StudentAdapter.StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerStudentBinding itemContainerStudentBinding = ItemContainerStudentBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new StudentAdapter.StudentViewHolder(itemContainerStudentBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentAdapter.StudentViewHolder holder, int position) {

        holder.setData(students.get(position));
    }

    @Override
    public int getItemCount() {
        return students.size();
    }
    class StudentViewHolder extends RecyclerView.ViewHolder {
        ItemContainerStudentBinding binding;

        StudentViewHolder(ItemContainerStudentBinding itemContainerMenuBinding) {
            super(itemContainerMenuBinding.getRoot());
            binding = itemContainerMenuBinding;
        }
        void setData(Student student) {
            binding.index.setText(String.valueOf(getAdapterPosition() + 1 + "."));
            binding.tvName.setText(student.getName());
            binding.tvDate.setText(student.getDob());

            binding.getRoot().setOnClickListener(v -> studentListener.onStudentClick(student));
            binding.getRoot().setOnLongClickListener(v -> {
                studentListener.onStudentLongClick(student);
                return true;
            });

            if(layout.equals("main")) {
                if(!preferenceManager.getString(Constants.KEY_USER_ROLE).equals("Học sinh")){
                    binding.btnChange.setVisibility(View.VISIBLE);
                    binding.btnChange.setOnClickListener(v -> studentListener.onStudentChange(student));
                }
            }
        }
    }
}
