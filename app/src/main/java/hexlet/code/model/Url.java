package hexlet.code.model;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "urls")
public final class Url extends Model {

    @Id
    @GeneratedValue
    private int id;
    private String name;
    @WhenCreated
    private Instant createdAt;
    @OneToMany(mappedBy = "url")
    private List<UrlCheck> urlChecks;

    public Url(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<UrlCheck> getUrlChecks() {
        return urlChecks;
    }
}
