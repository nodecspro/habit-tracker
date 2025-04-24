module org.example.habittracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.desktop;

    // UI библиотеки
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;

    // Spring и JPA
    requires spring.core;
    requires spring.beans;
    requires spring.context;
    requires spring.data.jpa;
    requires spring.data.commons;
    requires jakarta.persistence;
    requires jakarta.annotation;

    // Другие зависимости
    requires java.sql;
    requires com.zaxxer.hikari;
    requires spring.security.crypto;
    requires spring.orm;
    requires spring.tx;
    requires spring.boot;
    requires spring.boot.autoconfigure;

    // Открываем пакеты для JavaFX и Spring
    opens org.example.habittracker to javafx.fxml, spring.core;
    opens org.example.habittracker.controller to javafx.fxml, spring.core;
    opens org.example.habittracker.model to spring.core, org.hibernate.orm.core;
    opens org.example.habittracker.config to spring.core;
    opens org.example.habittracker.util to spring.core, spring.beans;

    // Экспортируем пакеты
    exports org.example.habittracker;
    exports org.example.habittracker.controller;
    exports org.example.habittracker.model;
    exports org.example.habittracker.service;
    exports org.example.habittracker.repository;
    exports org.example.habittracker.config;
    exports org.example.habittracker.util;

    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires atlantafx.base;
}