package com.ecommerce.project.repositories;

import com.ecommerce.project.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {

    @Query("Select ci from CartItem ci WHERE ci.product.id= ?1 and ci.cart.id= ?2")
    CartItem findCartItemByProductIdAndCartId(Long productId,Long cartId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id=?1 AND ci.product.id=?2")
    void deleteCartItemByProductIdAndCartId(Long cartId, Long productId);
}
