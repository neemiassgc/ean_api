package com.api.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Price {

    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private UUID id;

    private BigDecimal value;

    @Column(name = "instant", columnDefinition = "TIMESTAMPTZ")
    private Instant instant = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_product_prices"), nullable = false)
    @Setter(AccessLevel.PACKAGE)
    private Product product;

    public Price(final BigDecimal value) {
        this(value, null);
    }

    public Price(final BigDecimal value, final Product product) {
        this(value, Instant.now(), product);
    }

    public Price(final BigDecimal value, final Instant instant, final Product product) {
        this(null, value, instant, product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value, instant, product.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Price)) return false;

        final Price that = (Price) obj;

        return Objects.equals(this.id, that.getId()) &&
            Objects.equals(this.value, that.getValue()) &&
            Objects.equals(this.instant, that.getInstant()) &&
            Objects.equals(this.product, that.getProduct());
    }

    @Override
    public String toString() {
        return String.format("Price{id=%s, value=%s, created=%s, product=%s}", id, value, instant, product);
    }
}
