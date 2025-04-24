package org.example.habittracker.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DatabaseManager {

    private final Environment environment;
    private final ApplicationContext context;

    @Autowired
    public DatabaseManager(Environment environment, ApplicationContext context) {
        this.environment = environment;
        this.context = context;
    }

    public void switchToGuestMode() {
        System.setProperty("spring.profiles.active", "guest");
        refreshContext();
    }

    public void switchToRegisteredMode() {
        System.setProperty("spring.profiles.active", "registered");
        refreshContext();
    }

    private void refreshContext() {
        // Здесь логика для перезагрузки контекста Spring
        // Это упрощенный пример, в реальном приложении может потребоваться
        // более сложная логика для переключения между базами данных
    }
}