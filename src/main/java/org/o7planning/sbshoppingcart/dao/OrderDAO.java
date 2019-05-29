package org.o7planning.sbshoppingcart.dao;

import org.o7planning.sbshoppingcart.entity.Order;
import org.o7planning.sbshoppingcart.entity.OrderDetail;
import org.o7planning.sbshoppingcart.entity.Product;
import org.o7planning.sbshoppingcart.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Transactional
@Repository
public class OrderDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProductDAO productDAO;

    private int getMaxOrderNum() {
        String sql = "Select max(o.orderNum) from " + Order.class.getName() + " o ";
        Integer value = entityManager.createQuery(sql, Integer.class)
                .getSingleResult();
        if (value == null) {
            return 0;
        }
        return value;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrder(CartInfo cartInfo) {

        int orderNum = this.getMaxOrderNum() + 1;
        Order order = new Order();

        order.setId(UUID.randomUUID().toString());
        order.setOrderNum(orderNum);
        order.setOrderDate(new Date());
        order.setAmount(cartInfo.getAmountTotal());

        CustomerInfo customerInfo = cartInfo.getCustomerInfo();
        order.setCustomerName(customerInfo.getName());
        order.setCustomerEmail(customerInfo.getEmail());
        order.setCustomerPhone(customerInfo.getPhone());
        order.setCustomerAddress(customerInfo.getAddress());

        entityManager.persist(order);

        List<CartLineInfo> lines = cartInfo.getCartLines();

        for (CartLineInfo line : lines) {
            OrderDetail detail = new OrderDetail();
            detail.setId(UUID.randomUUID().toString());
            detail.setOrder(order);
            detail.setAmount(line.getAmount());
            detail.setPrice(line.getProductInfo().getPrice());
            detail.setQuanity(line.getQuantity());

            String code = line.getProductInfo().getCode();
            Product product = this.productDAO.findProduct(code);
            detail.setProduct(product);

            entityManager.persist(detail);
        }

        // Order Number!
        cartInfo.setOrderNum(orderNum);
        // Flush
        entityManager.flush();
    }

    // @page = 1, 2, ...
//    public PaginationResult<OrderInfo> listOrderInfo(int page, int maxResult, int maxNavigationPage) {
//        String sql = "Select new " + OrderInfo.class.getName()//
//                + "(ord.id, ord.orderDate, ord.orderNum, ord.amount, "
//                + " ord.customerName, ord.customerAddress, ord.customerEmail, ord.customerPhone) " + " from "
//                + Order.class.getName() + " ord "//
//                + " order by ord.orderNum desc";
//
//        Query<OrderInfo> query = session.createQuery(sql, OrderInfo.class);
//        return new PaginationResult<OrderInfo>(query, page, maxResult, maxNavigationPage);
//    }

    public Order findOrder(String orderId) {
        return entityManager.find(Order.class, orderId);
    }

    public OrderInfo getOrderInfo(String orderId) {
        Order order = this.findOrder(orderId);
        if (order == null) {
            return null;
        }
        return new OrderInfo(order.getId(), order.getOrderDate(), //
                order.getOrderNum(), order.getAmount(), order.getCustomerName(), //
                order.getCustomerAddress(), order.getCustomerEmail(), order.getCustomerPhone());
    }

    public List<OrderDetailInfo> listOrderDetailInfos(String orderId) {
        String sql = "Select new " + OrderDetailInfo.class.getName() //
                + "(d.id, d.product.code, d.product.name , d.quanity,d.price,d.amount) "//
                + " from " + OrderDetail.class.getName() + " d "//
                + " where d.order.id = :orderId ";

        return entityManager.createQuery(sql, OrderDetailInfo.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

}
