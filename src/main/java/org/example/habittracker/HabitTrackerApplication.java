package org.example.habittracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.Map;

@SpringBootApplication
@ComponentScan(basePackages = {"org.example.habittracker", "org.example.habittracker.controller", "org.example.habittracker.model"})
@EnableJpaRepositories(basePackages = "org.example.habittracker.repository")
public class HabitTrackerApplication extends Application {

    private ConfigurableApplicationContext springContext;
    private Parent rootNode;

    public static void main(String[] args) {
        System.setProperty("spring.classformat.ignore", "true");
        launch(args);
    }

    @Override
    public void init() throws Exception {
        springContext = SpringApplication.run(HabitTrackerApplication.class);

        // Выводим список бинов Spring
        System.out.println("Spring beans:");
        String[] beanNames = springContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            System.out.println(" - " + beanName);
        }

        // Проверяем, зарегистрирован ли контроллер
        boolean hasAuthController = springContext.containsBean("authController");
        System.out.println("Has AuthController bean: " + hasAuthController);

        // Пробуем получить все бины типа Controller
        Map<String, Object> controllers = springContext.getBeansWithAnnotation(Controller.class);
        System.out.println("Controllers: " + controllers);

        URL fxmlUrl = getClass().getResource("/fxml/login.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        fxmlLoader.setControllerFactory(springContext::getBean);
        rootNode = fxmlLoader.load();
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Habit Tracker");
        stage.setScene(new Scene(rootNode, 800, 600));
        stage.show();
    }

    @Override
    public void stop() {
        springContext.close();
    }

    @SpringBootApplication
    public static class HabitTrackerSpringApplication {
        // Пустой класс для запуска Spring Boot
    }
}