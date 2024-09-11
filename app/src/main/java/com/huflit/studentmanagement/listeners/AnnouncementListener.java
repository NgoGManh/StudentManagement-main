package com.huflit.studentmanagement.listeners;

import com.huflit.studentmanagement.models.Announcement;

public interface AnnouncementListener {
    void onAnnouncementClick(Announcement announcement);
    void onAnnouncementLongClick(Announcement announcement);
}
