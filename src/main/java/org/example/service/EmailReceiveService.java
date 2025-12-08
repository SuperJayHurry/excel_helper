package org.example.service;

public interface EmailReceiveService {
    /**
     * Connect to mail server, find relevant emails for the given task,
     * download attachments, and create Submissions.
     * @param taskId The task ID to look for (optional, or sync all).
     * @return Number of new submissions processed.
     */
    int receiveSubmissions(Long taskId);
}

