package com.unibuc.management.listeners;

import com.unibuc.management.entities.Appointment;
import com.unibuc.management.entities.Doctor;
import com.unibuc.management.entities.Patient;
import com.unibuc.management.services.PropagationService;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class DWPropagationListener implements ApplicationContextAware,
        PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        DWPropagationListener.applicationContext = applicationContext;
    }

    private static PropagationService getPropagationService() {
        if (applicationContext == null) {
            return null;
        }
        return applicationContext.getBean(PropagationService.class);
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        System.out.println("[DW] PostInsert: " + event.getEntity().getClass().getSimpleName());
        propagateToDW(event.getEntity());
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        System.out.println("[DW] PostUpdate: " + event.getEntity().getClass().getSimpleName());
        propagateToDW(event.getEntity());
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        System.out.println("[DW] PostDelete: " + event.getEntity().getClass().getSimpleName());
        removeFromDW(event.getEntity());
    }

    private void propagateToDW(Object entity) {
        PropagationService propagationService = getPropagationService();
        if (propagationService == null) {
            System.err.println("[DW] PropagationService not available!");
            return;
        }
        
        try {
            if (entity instanceof Patient) {
                System.out.println("[DW] Propagating Patient ID: " + ((Patient) entity).getId());
                propagationService.propagatePatientToDW((Patient) entity);
            } else if (entity instanceof Doctor) {
                System.out.println("[DW] Propagating Doctor ID: " + ((Doctor) entity).getId());
                propagationService.propagateDoctorToDW((Doctor) entity);
            } else if (entity instanceof Appointment) {
                System.out.println("[DW] Propagating Appointment ID: " + ((Appointment) entity).getId());
                propagationService.propagateAppointmentToDW((Appointment) entity);
            }
        } catch (Exception e) {
            System.err.println("[DW] Error propagating to DW: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void removeFromDW(Object entity) {
        PropagationService propagationService = getPropagationService();
        if (propagationService == null) {
            return;
        }
        
        try {
            if (entity instanceof Patient) {
                propagationService.removePatientFromDW(((Patient) entity).getId());
            } else if (entity instanceof Doctor) {
                propagationService.removeDoctorFromDW(((Doctor) entity).getId());
            } else if (entity instanceof Appointment) {
                propagationService.removeAppointmentFromDW(((Appointment) entity).getId());
            }
        } catch (Exception e) {
            System.err.println("Error removing from DW: " + e.getMessage());
        }
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }
}

