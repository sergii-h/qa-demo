package com.example.demo;

import com.example.demo.context.ItemTestContext;
import com.example.demo.data.DemoData;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemoControllerTest {
    private DemoController demoController;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private DemoEventProducer demoEventProducer;

    @BeforeEach
    void beforeEach() {
        demoController = new DemoController(itemRepository, demoEventProducer);
    }

    @Test
    void shouldCallProduceEventOnceWhenCreateItem() {
        // when
        demoController.createItem(ItemTestContext.builder().build().createDemoRequest());

        // then
        verify(demoEventProducer, times(1)).produce(any());
    }

    @Test
    void shouldNotCallProduceEventWhenCreateItemRepositoryError() {
        // given
        doThrow(new IllegalStateException("Error occurred"))
                .when(itemRepository)
                .insert((DemoData) any());

        // when
        assertThrows(
                IllegalStateException.class,
                () -> demoController.createItem(ItemTestContext.builder().build().createDemoRequest())
        );

        // then
        verify(demoEventProducer, never()).produce(any());
    }

    @Test
    void shouldNotCallProduceEventWhenUpdateItem() {
        // given
        when(itemRepository.findById(any())).thenReturn(Optional.ofNullable(DemoData.builder().build()));

        // when
        demoController.updateItem("1", ItemTestContext.builder().build().createDemoRequest());

        // then
        verify(demoEventProducer, never()).produce(any());
    }

    @Test
    void shouldNotCallProduceEventWhenGetItem() {
        // given
        when(itemRepository.findById(any())).thenReturn(Optional.ofNullable(DemoData.builder().build()));

        // when
        demoController.getItem("1");

        // then
        verify(demoEventProducer, never()).produce(any());
    }

    @Test
    void shouldNotCallProduceEventWhenGetItems() {
        // when
        demoController.getItems();

        // then
        verify(demoEventProducer, never()).produce(any());
    }

    @Test
    void shouldNotCallProduceEventWhenDeleteItem() {
        // given
        when(itemRepository.findById(any())).thenReturn(Optional.ofNullable(DemoData.builder().build()));

        // when
        demoController.deleteItem("1");

        // then
        verify(demoEventProducer, never()).produce(any());
    }
}
