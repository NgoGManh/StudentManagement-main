package com.huflit.studentmanagement.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huflit.studentmanagement.databinding.ItemContainerAnnouncementBinding;
import com.huflit.studentmanagement.listeners.AnnouncementListener;
import com.huflit.studentmanagement.models.Announcement;
import com.huflit.studentmanagement.utilities.Constants;
import com.huflit.studentmanagement.utilities.PreferenceManager;
import com.huflit.studentmanagement.utilities.Utils;
import com.huflit.studentmanagement.models.Announcement;

import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {
    private final List<Announcement> announcements;
    private final AnnouncementListener announcementListener;

    public AnnouncementAdapter(List<Announcement> announcements, AnnouncementListener announcementListener) {
        this.announcements = announcements;
        this.announcementListener = announcementListener;
    }

    @NonNull
    @Override
    public AnnouncementAdapter.AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerAnnouncementBinding itemContainerAnnouncementBinding = ItemContainerAnnouncementBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AnnouncementAdapter.AnnouncementViewHolder(itemContainerAnnouncementBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementAdapter.AnnouncementViewHolder holder, int position) {

        holder.setData(announcements.get(position));
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }
    class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        ItemContainerAnnouncementBinding binding;

        AnnouncementViewHolder(ItemContainerAnnouncementBinding itemContainerMenuBinding) {
            super(itemContainerMenuBinding.getRoot());
            binding = itemContainerMenuBinding;
        }
        void setData(Announcement announcement) {
            binding.tvTitle.setText(announcement.title);
            binding.tvContent.setText(announcement.content);
            binding.tvDate.setText(Utils.formatDateTime(announcement.time));
            binding.getRoot().setOnClickListener(v -> announcementListener.onAnnouncementClick(announcement));
            binding.getRoot().setOnLongClickListener(v -> {
                announcementListener.onAnnouncementLongClick(announcement);
                return true;
            });
        }
    }
}
