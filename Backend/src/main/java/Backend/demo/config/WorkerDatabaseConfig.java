package Backend.demo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    basePackages = "Backend.demo.Repositories.worker",
    entityManagerFactoryRef = "workerEntityManagerFactory",
    transactionManagerRef = "workerTransactionManager"
)
public class WorkerDatabaseConfig {

    @Bean(name = "workerDataSource")
    @ConfigurationProperties(prefix = "worker.datasource")
    public DataSource workerDataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:h2:mem:workerdb")
            .driverClassName("org.h2.Driver")
            .username("sa")
            .password("")
            .build();
    }

    @Bean(name = "workerEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean workerEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("workerDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        
        return builder
                .dataSource(dataSource)
                .packages("Backend.demo.Entities.worker")
                .persistenceUnit("worker")
                .properties(properties)
                .build();
    }

    @Bean(name = "workerTransactionManager")
    public PlatformTransactionManager workerTransactionManager(
            @Qualifier("workerEntityManagerFactory") LocalContainerEntityManagerFactoryBean workerEntityManagerFactory) {
        return new JpaTransactionManager(workerEntityManagerFactory.getObject());
    }
}
