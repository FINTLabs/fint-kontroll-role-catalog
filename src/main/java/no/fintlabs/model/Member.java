package no.fintlabs.model;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Slf4j
@Entity
@Table(name="Members", indexes = @Index(name = "resource_id_index",columnList = "resourceId"))
@AllArgsConstructor
@NoArgsConstructor(access=AccessLevel.PUBLIC, force=true)

public class Member {
    @Id
    //@GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;
    private String resourceId;
    private String firstName;
    private String lastName;
    private String userType;
    private String userName;
    private String userId;

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.MERGE,
                    CascadeType.PERSIST
            },
            mappedBy = "members")
    @JsonIgnore
    private Set<Role> roles = new HashSet<>();

    public long getId() {
        return id;
    }
}
