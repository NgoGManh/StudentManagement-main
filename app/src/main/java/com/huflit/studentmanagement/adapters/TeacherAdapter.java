package com.huflit.studentmanagement.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huflit.studentmanagement.databinding.ItemContainerTeacherBinding;
import com.huflit.studentmanagement.listeners.TeacherListener;
import com.huflit.studentmanagement.models.Teacher;

import java.util.List;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder> {
    private final List<Teacher> teachers;
    private final TeacherListener teacherListener;

    public TeacherAdapter(List<Teacher> teachers, TeacherListener teacherListener) {
        this.teachers = teachers;
        this.teacherListener = teacherListener;
    }

    @NonNull
    @Override
    public TeacherAdapter.TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerTeacherBinding itemContainerTeacherBinding = ItemContainerTeacherBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TeacherAdapter.TeacherViewHolder(itemContainerTeacherBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherAdapter.TeacherViewHolder holder, int position) {

        holder.setData(teachers.get(position));
    }

    @Override
    public int getItemCount() {
        return teachers.size();
    }
    class TeacherViewHolder extends RecyclerView.ViewHolder {
        ItemContainerTeacherBinding binding;

        TeacherViewHolder(ItemContainerTeacherBinding itemContainerMenuBinding) {
            super(itemContainerMenuBinding.getRoot());
            binding = itemContainerMenuBinding;
        }
        void setData(Teacher teacher) {
            binding.tvName.setText(teacher.getName());
            binding.tvEmail.setText(teacher.getEmail());

            binding.getRoot().setOnClickListener(v -> teacherListener.onTeacherClick(teacher));
            binding.getRoot().setOnLongClickListener(v -> {
                teacherListener.onTeacherLongClick(teacher);
                return true;
            });
        }
    }
}
