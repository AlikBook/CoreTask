package Backend.demo.Entities.worker;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity
public class Worker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer workerId;

    private String workerName;
    private String workerLastName;

    @Column(length = 120)
    private String viewerKey;

    public Integer getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Integer workerId) {
        this.workerId = workerId;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getWorkerLastName() {
        return workerLastName;
    }

    public void setWorkerLastName(String workerLastName) {
        this.workerLastName = workerLastName;
    }

    public String getViewerKey() {
        return viewerKey;
    }

    public void setViewerKey(String viewerKey) {
        this.viewerKey = viewerKey;
    }
}
