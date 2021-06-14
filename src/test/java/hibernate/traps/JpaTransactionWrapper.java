package hibernate.traps;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

public class JpaTransactionWrapper {

    private final EntityManagerFactory emf;


    public JpaTransactionWrapper(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void doInTransaction(Delegate delegate) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            delegate.doInTransaction(em);
            tx.commit();
        } catch (RuntimeException ex) {
            tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public <R> R doInTransactionAndReturn(ReturningDelegate<R> delegate) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            R result = delegate.doInTransaction(em);
            tx.commit();
            return result;
        } catch (RuntimeException ex) {
            tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public interface Delegate {

        void doInTransaction(EntityManager em);
    }

    public interface ReturningDelegate<R> {

        R doInTransaction(EntityManager em);
    }
}
