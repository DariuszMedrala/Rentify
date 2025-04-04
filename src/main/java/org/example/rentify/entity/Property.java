package org.example.rentify.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "properties")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

/*
 * Property entity representing a property in the system.
 * This class is mapped to the "properties" table in the database.
 */
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "property_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PropertyType propertyType;

    private Double area;

    @Column(name = "number_of_rooms")
    private Integer numberOfRooms;

    @Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    private Boolean availability = true;

    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDate = LocalDateTime.now();

    @Column(name = "image_url")
    private String imageUrl;

    public enum PropertyType {
        APARTMENT,
        HOUSE,
        STUDIO,
        VILLA,
        LOFT,
        PENTHOUSE,
    }
}
