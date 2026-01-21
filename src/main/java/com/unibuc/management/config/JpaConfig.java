package com.unibuc.management.config;

import com.unibuc.management.listeners.DWPropagationListener;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class JpaConfig {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private DWPropagationListener dwPropagationListener;

    @PostConstruct
    public void registerListeners() {
        try {
            if (entityManagerFactory.unwrap(SessionFactoryImpl.class) != null) {
                SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
                EventListenerRegistry registry = sessionFactory.getServiceRegistry()
                        .getService(EventListenerRegistry.class);
                
                // Register the listener for post-insert, post-update, and post-delete events
                registry.appendListeners(EventType.POST_INSERT, dwPropagationListener);
                registry.appendListeners(EventType.POST_UPDATE, dwPropagationListener);
                registry.appendListeners(EventType.POST_DELETE, dwPropagationListener);
                
                System.out.println("[JPA Config] DW Propagation Listener registered successfully!");
            } else {
                System.err.println("[JPA Config] Could not unwrap SessionFactoryImpl");
            }
        } catch (Exception e) {
            System.err.println("[JPA Config] Error registering listeners: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

