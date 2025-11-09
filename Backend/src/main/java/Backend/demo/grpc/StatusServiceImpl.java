package Backend.demo.grpc;

import Backend.demo.Entities.task.TaskStatus;
import Backend.demo.Repositories.task.StatusRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GrpcService
public class StatusServiceImpl extends StatusServiceGrpc.StatusServiceImplBase {
    
    @Autowired
    private StatusRepository statusRepository;
    
    @Override
    public void getStatusById(StatusIdRequest request, StreamObserver<StatusResponse> responseObserver) {
        TaskStatus status = statusRepository.findById(request.getId())
            .orElseThrow(() -> new RuntimeException("Status not found with id: " + request.getId()));
        
        StatusResponse response = StatusResponse.newBuilder()
            .setId(status.getStatusId())
            .setStatusName(status.getStatusName())
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    @Override
    public void getAllStatuses(EmptyRequest request, StreamObserver<StatusListResponse> responseObserver) {
        List<TaskStatus> statuses = statusRepository.findAll();
        
        StatusListResponse.Builder responseBuilder = StatusListResponse.newBuilder();
        for (TaskStatus status : statuses) {
            StatusResponse statusResponse = StatusResponse.newBuilder()
                .setId(status.getStatusId())
                .setStatusName(status.getStatusName())
                .build();
            responseBuilder.addStatuses(statusResponse);
        }
        
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
