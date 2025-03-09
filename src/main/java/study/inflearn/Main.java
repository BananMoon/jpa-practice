package study.inflearn;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.List;

public class Main {
    private final EntityManagerFactory entityManagerFactory;    // 애플리케이션 실행 시 최초 1번 생성하면 됨. DB 연결을 맺거나, 기본적인 검증을 수행할 수 있음.
    private EntityManager entityManager;   // entitymanager는 트랜잭션 단위마다 만들어주어야 함.

    public Main() {
        this.entityManagerFactory = Persistence.createEntityManagerFactory("hello");
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.detached();
    }

    private void dbConnectionTest() {
        System.out.println("==== EntityManagerFactory created ====");
        this.entityManager = entityManagerFactory.createEntityManager();
        System.out.println("==== EntityManager created ====");
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        System.out.println("==== Transaction started ====");
        tx.commit();
        System.out.println("==== Transaction closed(commit) ====");

        entityManager.close();
        System.out.println("==== EntityManager closed ====");
        entityManagerFactory.close();
        System.out.println("==== EntityManagerFactory closed ====");
    }

    private void crudUsingEntityManager() {
        this.entityManager = entityManagerFactory.createEntityManager();
        // 데이터를 변경하는 모든 작업은 트랜잭션 안에서 이루어져야 함.
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();

        try {
            List<Member> results = selectJPQL(entityManager);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            entityManager.close();
        }
        entityManagerFactory.close();
    }

    private List<Member> selectJPQL(EntityManager entityManager) {
        List<Member> findMembers = entityManager.createQuery("select m from Member as m", Member.class)
                .setFirstResult(0)
                .setMaxResults(10)
                .getResultList();
        for (Member member : findMembers) {
            System.out.println("member = " + member.getName());
        }
        return findMembers;
    }

    private void delete(EntityManager entityManager) {
        Member member = entityManager.find(Member.class, 1L);
        entityManager.remove(member);
    }

    private void update(EntityManager entityManager) {
        Member member = entityManager.find(Member.class, 1L);
        member.setName("HelloB");
    }

    private void save(EntityManager entityManager) {
        Member member = new Member(1L, "HelloA");
        entityManager.persist(member);
    }

    private void detached() {
        this.entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        try {
            // 영속
            Member member = entityManager.find(Member.class, 1L);
            member.setName("updatedHelloA");

            entityManager.detach(member);
            System.out.println("=== detached ===");

            // 비영속 처리했으니, 엔티티 조회하면 조회 쿼리문이 실행된다.
            entityManager.find(Member.class, 1L);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            entityManager.close();
        }
    }
}
