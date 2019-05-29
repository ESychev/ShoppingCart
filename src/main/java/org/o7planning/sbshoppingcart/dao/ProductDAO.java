package org.o7planning.sbshoppingcart.dao;

import org.o7planning.sbshoppingcart.entity.Product;
import org.o7planning.sbshoppingcart.form.ProductForm;
import org.o7planning.sbshoppingcart.model.ProductInfo;
import org.o7planning.sbshoppingcart.pagination.PaginationResult;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.Date;

@Transactional
@Repository
public class ProductDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public Product findProduct(String code) {
        try {
            String sql = "Select e from " + Product.class.getName() + " e Where e.code =:code ";
            return entityManager.createQuery(sql, Product.class)
                    .setParameter("code", code)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public ProductInfo findProductInfo(String code) {
        Product product = this.findProduct(code);
        if (product == null) {
            return null;
        }
        return new ProductInfo(product.getCode(), product.getName(), product.getPrice());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void save(ProductForm productForm) {

        String code = productForm.getCode();

        Product product = null;

        boolean isNew = false;
        if (code != null) {
            product = this.findProduct(code);
        }
        if (product == null) {
            isNew = true;
            product = new Product();
            product.setCreateDate(new Date());
        }
        product.setCode(code);
        product.setName(productForm.getName());
        product.setPrice(productForm.getPrice());

        if (productForm.getFileData() != null) {
            byte[] image = null;
            try {
                image = productForm.getFileData().getBytes();
            } catch (IOException e) {
            }
            if (image != null && image.length > 0) {
                product.setImage(image);
            }
        }
        if (isNew) {
            entityManager.persist(product);
        }
        // If error in DB, Exceptions will be thrown out immediately
        entityManager.flush();
    }

//    public PaginationResult<ProductInfo> queryProducts(int page, int maxResult, int maxNavigationPage,
//                                                       String likeName) {
//        String sql = "Select new " + ProductInfo.class.getName() //
//                + "(p.code, p.name, p.price) " + " from "//
//                + Product.class.getName() + " p ";
//        if (likeName != null && likeName.length() > 0) {
//            sql += " Where lower(p.name) like :likeName ";
//        }
//        sql += " order by p.createDate desc ";
//        //
//        Query<ProductInfo> query = entityManager.createQuery(sql, ProductInfo.class);
//
//        if (likeName != null && likeName.length() > 0) {
//            query.setParameter("likeName", "%" + likeName.toLowerCase() + "%");
//        }
//        return new PaginationResult<ProductInfo>(query, page, maxResult, maxNavigationPage);
//    }

//    public PaginationResult<ProductInfo> queryProducts(int page, int maxResult, int maxNavigationPage) {
//        return queryProducts(page, maxResult, maxNavigationPage, null);
//    }

}
