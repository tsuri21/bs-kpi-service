package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaProvider {
    private static EntityManagerFactory emf;

    private JpaProvider() {
    }

    static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("kpi-persistence-unit");
        }
        return emf;
    }

    static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
