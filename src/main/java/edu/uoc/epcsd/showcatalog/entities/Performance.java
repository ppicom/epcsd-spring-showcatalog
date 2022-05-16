package edu.uoc.epcsd.showcatalog.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Performance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "image", nullable = false)
    private String image;

    @Column(name = "price", nullable = false)
    private double price;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "duration", nullable = false)
    private int duration;

    @Column(name = "date")
    private ZonedDateTime date;

    @Column(name = "streaming_url", nullable = false)
    private String streamingURL;

    @Column(name = "remaining_seats", nullable = false)
    private int remainingSeats;

    @Column(name = "status", nullable = false)
    private Status status;

    @JsonIgnore
    @ManyToOne()
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;
}
