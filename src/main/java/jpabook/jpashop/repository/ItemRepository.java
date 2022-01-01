package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class ItemRepository {

    private final EntityManager em;

    @Autowired
    public ItemRepository(EntityManager em) {
        this.em = em;
    }

    public void save(Item item){
        if(item.getId() == null){
            em.persist(item);
        }else{
            em.merge(item); // update 와 비슷함 - 이미 db에 등록된 것을 한번 가져온 케이스
        }
    }

    public Item findOne(Long id){
        return em.find(Item.class, id);
    }

    public List<Item> findAll(){
        return em.createQuery("select i from Item i", Item.class).getResultList();
    }


}
