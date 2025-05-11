package org.example.rentify.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "images")
@Data
@NoArgsConstructor
@AllArgsConstructor

/*
 * Image entity representing an image in the system.
 * This class is mapped to the "images" table in the database.
 */
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "description")
    private String description;

    @Column(name = "upload_date", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime uploadDate;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return Objects.equals(id, image.id) && Objects.equals(imageUrl, image.imageUrl) && Objects.equals(description, image.description) && Objects.equals(uploadDate, image.uploadDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, imageUrl, description, uploadDate);
    }
}
