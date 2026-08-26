package com.example.projectcollab.bootstrap;

import com.example.projectcollab.project.persistence.ProjectRepository;
import com.example.projectcollab.task.persistence.SpringDataTaskRepository;
import com.example.projectcollab.user.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "app.demo-data.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:demo-data-disabled;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class DemoDataDisabledIntegrationTests {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SpringDataTaskRepository taskRepository;

    @Test
    void startsWithoutCreatingDemoDataWhenDisabled() {
        assertThat(applicationContext.getBeansOfType(DemoDataInitializer.class)).isEmpty();
        assertThat(userRepository.count()).isZero();
        assertThat(projectRepository.count()).isZero();
        assertThat(taskRepository.count()).isZero();
    }
}
