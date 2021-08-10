package hibernate.traps.merge_vs_update;

import hibernate.traps.JpaTransactionWrapper;
import hibernate.traps.SQLStatementLoggingAppender;
import hibernate.traps.merge_vs_update.model.Person;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MergeVsUpdateTest {

    private EntityManagerFactory emf;

    private JpaTransactionWrapper jpaTransactionWrapper;

    @BeforeEach
    public void init() {
        emf = Persistence.createEntityManagerFactory("merge_vs_update");
        jpaTransactionWrapper = new JpaTransactionWrapper(emf);
        jpaTransactionWrapper.doInTransaction(em -> {
            for (long i = 0; i < 1000; i++) {
                Person person = new Person("John Doe " + i);
                person.setId(i);
                em.persist(person);
            }
        });
        SQLStatementLoggingAppender.clear();
    }

    @Test
    public void testMerge() {
        //WHEN: User is being updated using EntityManager::merge
        jpaTransactionWrapper.doInTransaction(em -> {
            Person person = new Person("John Doe 0");
            person.setName("John Merge 0");
            person.setId(0L);
            em.merge(person);
        });

        //THEN: 1 Select + 1 Update are executed against Database
        SQLStatementLoggingAppender.printAll();
        assertEquals(2, SQLStatementLoggingAppender.countQueriesContaining("execute"));
    }

    @Test
    public void testMergeBatch() {
        //WHEN: 1000 Users are updated using EntityManager::merge
        jpaTransactionWrapper.doInTransaction(em -> {
            for (long i = 0; i < 1000; i++) {
                Person person = new Person("John Merge " + i);
                person.setId(i);
                em.merge(person);
            }
        });

        //THEN: 1000 Selects + 1 Update are executed against Database
        SQLStatementLoggingAppender.printAll();
        assertEquals(1001, SQLStatementLoggingAppender.countQueriesContaining("execute"));
    }


    @Test
    public void testUpdate() {
        //WHEN: User is being updated using Session::update
        jpaTransactionWrapper.doInTransaction(em -> {
            Person person = new Person("John Doe 0");
            person.setName("John Update 0");
            person.setId(0L);
            em.unwrap(Session.class).update(person);
        });

        //THEN: only one Update Statement is executed against Database
        SQLStatementLoggingAppender.printAll();
        assertEquals(1, SQLStatementLoggingAppender.countQueriesContaining("execute"));
    }

    @Test
    public void testUpdateBatch() {
        //WHEN: 1000 Users are updated using Session:update
        jpaTransactionWrapper.doInTransaction(em -> {
            for (long i = 0; i < 1000; i++) {
                Person person = new Person("John Update " + i);
                person.setId(i);
                em.unwrap(Session.class).update(person);
            }
        });

        //THEN: only one Update Statement is executed against Database
        SQLStatementLoggingAppender.printAll();
        assertEquals(1, SQLStatementLoggingAppender.countQueriesContaining("execute"));
    }


}
