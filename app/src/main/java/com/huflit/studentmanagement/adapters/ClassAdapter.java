package com.huflit.studentmanagement.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.studentmanagement.databinding.ItemContainerClassBinding;
import com.huflit.studentmanagement.listeners.ClassListener;
import com.huflit.studentmanagement.models.Class;
import com.huflit.studentmanagement.utilities.Constants;

import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {
    private final List<Class> Classes;
    private final ClassListener ClassListener;

    public ClassAdapter(List<Class> Classes, ClassListener ClassListener) {
        this.Classes = Classes;
        this.ClassListener = ClassListener;
    }

    @NonNull
    @Override
    public ClassAdapter.ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerClassBinding itemContainerClassBinding = ItemContainerClassBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ClassAdapter.ClassViewHolder(itemContainerClassBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassAdapter.ClassViewHolder holder, int position) {

        holder.setData(Classes.get(position));
    }

    @Override
    public int getItemCount() {
        return Classes.size();
    }
    class ClassViewHolder extends RecyclerView.ViewHolder {
        ItemContainerClassBinding binding;

        ClassViewHolder(ItemContainerClassBinding itemContainerMenuBinding) {
            super(itemContainerMenuBinding.getRoot());
            binding = itemContainerMenuBinding;
        }

        void setTermName(String termId) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(Constants.KEY_COLLECTION_TERMS)
                    .document(termId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String termName = documentSnapshot.getString(Constants.KEY_TERM_NAME);
                            binding.tvTerm.setText(termName);
                        }
                    });
        }

        void setData(Class Class) {
            binding.tvClass.setText(Class.getName());
            setTermName(Class.getTermId());

            binding.getRoot().setOnClickListener(v -> ClassListener.onClassClick(Class));
        }
    }
}
