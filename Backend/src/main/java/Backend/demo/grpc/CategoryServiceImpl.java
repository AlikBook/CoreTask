package Backend.demo.grpc;

import Backend.demo.Entities.task.TaskCategory;
import Backend.demo.Repositories.task.TaskCategoryRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GrpcService
public class CategoryServiceImpl extends CategoryServiceGrpc.CategoryServiceImplBase {
    
    @Autowired
    private TaskCategoryRepository categoryRepository;
    
    @Override
    public void getCategoryById(CategoryIdRequest request, StreamObserver<CategoryResponse> responseObserver) {
        TaskCategory category = categoryRepository.findById(request.getId())
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getId()));
        
        CategoryResponse response = CategoryResponse.newBuilder()
            .setId(category.getCategoryId())
            .setCategoryName(category.getCategoryName())
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    @Override
    public void getAllCategories(EmptyRequest request, StreamObserver<CategoryListResponse> responseObserver) {
        List<TaskCategory> categories = categoryRepository.findAll();
        
        CategoryListResponse.Builder responseBuilder = CategoryListResponse.newBuilder();
        for (TaskCategory category : categories) {
            CategoryResponse categoryResponse = CategoryResponse.newBuilder()
                .setId(category.getCategoryId())
                .setCategoryName(category.getCategoryName())
                .build();
            responseBuilder.addCategories(categoryResponse);
        }
        
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
