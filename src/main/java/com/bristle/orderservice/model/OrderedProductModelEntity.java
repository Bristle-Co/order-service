package com.bristle.orderservice.model;

import org.springframework.lang.NonNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "ordered_product_models")
public class OrderedProductModelEntity {

    // Table name
    public static final String TABLE_NAME = "ordered_product_models";

    // Column names, reusable from outside of class
    // COLM for column
    public static final String COLM_MODEL_ID = "model_id";
    public static final String COLM_NUM_OF_ITEMS = "num_of_items";
    public static final String COLM_PRICE= "price";
    public static final String COLM_MODEL = "model";
    public static final String COLM_ORDER_ID = "order_id";

    @Id
    @NonNull
    @Column(name = COLM_MODEL_ID)
    int modelID;

    @Column(name = COLM_NUM_OF_ITEMS)
    int numOfItems;

    @Column(name = COLM_PRICE)
    int price;

    @Column(name = COLM_MODEL)
    @NonNull
    String model;

    @Column(name = COLM_ORDER_ID)
    @NonNull
    String orderId;

}
