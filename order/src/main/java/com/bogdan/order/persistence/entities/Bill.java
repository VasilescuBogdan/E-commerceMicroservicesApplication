package com.bogdan.order.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String user;

    private LocalDateTime dateTime;

    private Long orderNumber;

    @OneToMany
    private List<Item> items;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Bill bill = (Bill) o;
        return Objects.equals(id, bill.id) && Objects.equals(user, bill.user) && Objects.equals(orderNumber,
                bill.orderNumber) && Objects.equals(items, bill.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, orderNumber, items);
    }
}