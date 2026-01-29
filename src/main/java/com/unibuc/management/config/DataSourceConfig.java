package com.unibuc.management.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    private final Environment env;

    public DataSourceConfig(Environment env) {
        this.env = env;
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "dwDataSource")
    public DataSource dwDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        String jdbcUrl = env.getProperty("spring.dw-datasource.jdbc-url");
        if (jdbcUrl == null) {
            jdbcUrl = env.getProperty("spring.dw-datasource.url");
        }
        String driverClassName = env.getProperty("spring.dw-datasource.driver-class-name");
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(env.getProperty("spring.dw-datasource.username"));
        dataSource.setPassword(env.getProperty("spring.dw-datasource.password"));
        
        System.out.println("DW DataSource created: " + dataSource.getClass().getName());
        System.out.println("DW DataSource URL: " + dataSource.getUrl());
        System.out.println("DW DataSource Driver: " + driverClassName);
        return dataSource;
    }

    @Bean
    @Primary
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "dwJdbcTemplate")
    public JdbcTemplate dwJdbcTemplate(@Qualifier("dwDataSource") DataSource dwDataSource) {
        return new JdbcTemplate(dwDataSource);
    }
}

