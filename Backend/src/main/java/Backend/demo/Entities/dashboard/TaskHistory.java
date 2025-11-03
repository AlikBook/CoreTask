package Backend.demo.Entities.dashboard;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class TaskHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer historyId;
    
    private String action; // CREATE, UPDATE, DELETE
    private String taskName;
    private LocalDateTime timestamp;
    private String details;
    
    public TaskHistory() {
        this.timestamp = LocalDateTime.now();
    }
    
    public TaskHistory(String action, String taskName, String details) {
        this.action = action;
        this.taskName = taskName;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and setters
    public Integer getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Integer historyId) {
        this.historyId = historyId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
