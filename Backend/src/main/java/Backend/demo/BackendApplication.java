package Backend.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import Backend.demo.Entities.task.TaskStatus;
import Backend.demo.Repositories.task.StatusRepository;

import Backend.demo.Entities.task.TaskCategory;
import Backend.demo.Repositories.task.TaskCategoryRepository;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	

    @Bean
    public CommandLineRunner loadDefaultStatuses(StatusRepository statusRepository) {
        return args -> {
            if (statusRepository.count() == 0) {
                TaskStatus s1 = new TaskStatus();
                s1.setStatusName("Empty");
                statusRepository.save(s1);

                TaskStatus s2 = new TaskStatus();
                s2.setStatusName("To Do");
                statusRepository.save(s2);

                TaskStatus s3 = new TaskStatus();
                s3.setStatusName("In Progress");
                statusRepository.save(s3);

                TaskStatus s4 = new TaskStatus();
                s4.setStatusName("Complete");
                statusRepository.save(s4);
            }
        };
    }

    @Bean
    public CommandLineRunner loadDefaultCategory(TaskCategoryRepository categoryRepository) {
        return args -> {
            if (categoryRepository.count() == 0) {
                TaskCategory c1 = new TaskCategory();
                c1.setCategoryName("Empty");
                categoryRepository.save(c1);

                TaskCategory c2 = new TaskCategory();
                c2.setCategoryName("Programming");
                categoryRepository.save(c2);
            }
        };
    }

}
