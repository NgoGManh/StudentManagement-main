package com.huflit.studentmanagement.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huflit.studentmanagement.databinding.ItemContainerStudentBinding;
import com.huflit.studentmanagement.databinding.ItemContainerGradeBinding;
import com.huflit.studentmanagement.listeners.StudentListener;
import com.huflit.studentmanagement.listeners.GradeListener;
import com.huflit.studentmanagement.models.Student;
import com.huflit.studentmanagement.models.Grade;

import java.util.List;

public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.GradeViewHolder> {
    private final List<Grade> grades;
    private final GradeListener gradeListener;

    public GradeAdapter(List<Grade> grades, GradeListener gradeListener) {
        this.grades = grades;
        this.gradeListener = gradeListener;
    }

    @NonNull
    @Override
    public GradeAdapter.GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerGradeBinding itemContainerGradeBinding = ItemContainerGradeBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new GradeAdapter.GradeViewHolder(itemContainerGradeBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull GradeAdapter.GradeViewHolder holder, int position) {

        holder.setData(grades.get(position));
    }

    @Override
    public int getItemCount() {
        return grades.size();
    }

    class GradeViewHolder extends RecyclerView.ViewHolder {
        ItemContainerGradeBinding binding;

        GradeViewHolder(ItemContainerGradeBinding itemContainerMenuBinding) {
            super(itemContainerMenuBinding.getRoot());
            binding = itemContainerMenuBinding;
        }

        void setData(Grade grade) {
            binding.tvGrade.setText(grade.getBand());

            binding.getRoot().setOnClickListener(v -> gradeListener.onGradeClick(grade));
        }
    }
}