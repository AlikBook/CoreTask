package Backend.demo.grpc;

import Backend.demo.Entities.worker.Worker;
import Backend.demo.Repositories.worker.WorkerRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GrpcService
public class WorkerServiceImpl extends WorkerServiceGrpc.WorkerServiceImplBase {

    @Autowired
    private WorkerRepository workerRepository;

    @Override
    public void getAllWorkers(EmptyRequest request, StreamObserver<WorkerListResponse> responseObserver) {
        List<Worker> workers = workerRepository.findAll();
        
        WorkerListResponse.Builder responseBuilder = WorkerListResponse.newBuilder();
        
        for (Worker worker : workers) {
            WorkerResponse workerResponse = WorkerResponse.newBuilder()
                .setId(worker.getWorkerId())
                .setWorkerName(worker.getWorkerName())
                .setWorkerLastName(worker.getWorkerLastName())
                .build();
            responseBuilder.addWorkers(workerResponse);
        }
        
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getWorkerById(WorkerIdRequest request, StreamObserver<WorkerResponse> responseObserver) {
        Worker worker = workerRepository.findById(request.getId()).orElse(null);
        
        WorkerResponse.Builder responseBuilder = WorkerResponse.newBuilder();
        
        if (worker != null) {
            responseBuilder
                .setId(worker.getWorkerId())
                .setWorkerName(worker.getWorkerName())
                .setWorkerLastName(worker.getWorkerLastName());
        }
        
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void createWorker(CreateWorkerRequest request, StreamObserver<WorkerResponse> responseObserver) {
        Worker worker = new Worker();
        worker.setWorkerName(request.getWorkerName());
        worker.setWorkerLastName(request.getWorkerLastName());
        
        Worker savedWorker = workerRepository.save(worker);
        
        WorkerResponse response = WorkerResponse.newBuilder()
            .setId(savedWorker.getWorkerId())
            .setWorkerName(savedWorker.getWorkerName())
            .setWorkerLastName(savedWorker.getWorkerLastName())
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateWorker(UpdateWorkerRequest request, StreamObserver<WorkerResponse> responseObserver) {
        Worker worker = workerRepository.findById(request.getId()).orElse(null);
        
        if (worker != null) {
            worker.setWorkerName(request.getWorkerName());
            worker.setWorkerLastName(request.getWorkerLastName());
            Worker updatedWorker = workerRepository.save(worker);
            
            WorkerResponse response = WorkerResponse.newBuilder()
                .setId(updatedWorker.getWorkerId())
                .setWorkerName(updatedWorker.getWorkerName())
                .setWorkerLastName(updatedWorker.getWorkerLastName())
                .build();
            
            responseObserver.onNext(response);
        } else {
            responseObserver.onNext(WorkerResponse.newBuilder().build());
        }
        
        responseObserver.onCompleted();
    }

    @Override
    public void deleteWorker(WorkerIdRequest request, StreamObserver<EmptyResponse> responseObserver) {
        workerRepository.deleteById(request.getId());
        
        responseObserver.onNext(EmptyResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
