package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;


    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    // 1. dirty checking 이용하기 (앵간하면 이것을 사용하자)
    @Transactional
    public void updateItem(Long itemId, String name, int price, int stock){
        Item findItem = itemRepository.findOne(itemId);
        // 영속성 컨텍스트에 들어간 findItem
        findItem.setPrice(price);
        findItem.setName(name);
        findItem.setStockQuantity(stock);
        // transaction이 끝나며 commit -> flush(영속성 엔티티중에서 변경된것 다 찾는다) -> update query 날림

    }
    // 2. merge를 풀어쓴 코드
    @Transactional
    public Item updateItem2(Long itemId, Book bookParam){
        Item findItem = itemRepository.findOne(itemId);
        // 영속성 컨텍스트에 들어간 findItem
        findItem.setPrice(bookParam.getPrice());
        findItem.setName(bookParam.getName());
        findItem.setStockQuantity(bookParam.getStockQuantity());
        // transaction이 끝나며 commit -> flush(영속성 엔티티중에서 변경된것 다 찾는다) -> update query 날림
        return findItem;
    }

    // 병합은 모든 속성을 다 갈아져간다 - 몇개를 집어서 바꿀 수는 없다 -> 앵간하면 변경 감지를 쓰자


    public List<Item> findItems(){
        return itemRepository.findAll();
    }

    public Item findOne(Long id){
        return itemRepository.findOne(id);
    }
}
