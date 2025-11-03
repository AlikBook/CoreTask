package Backend.demo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "Backend.demo.Repositories.task",
    entityManagerFactoryRef = "taskEntityManagerFactory",
    transactionManagerRef = "taskTransactionManager"
)
public class TaskDatabaseConfig {

    @Primary
    @Bean(name = "taskDataSource")
    @ConfigurationProperties(prefix = "task.datasource")
    public DataSource taskDataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:h2:mem:taskdb")
            .driverClassName("org.h2.Driver")
            .username("sa")
            .password("")
            .build();
    }

    @Primary
    @Bean(name = "taskEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean taskEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("taskDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        
        return builder
                .dataSource(dataSource)
                .packages("Backend.demo.Entities.task")
                .persistenceUnit("task")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "taskTransactionManager")
    public PlatformTransactionManager taskTransactionManager(
            @Qualifier("taskEntityManagerFactory") LocalContainerEntityManagerFactoryBean taskEntityManagerFactory) {
        return new JpaTransactionManager(taskEntityManagerFactory.getObject());
    }
}
