package hexlet.code.model;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "urls")
public class Url extends Model {

    @Id
    @GeneratedValue
    private int id;
    private String name;
    @WhenCreated
    private Instant created_at;

    public Url(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreated_at(Instant created_at) {
        this.created_at = created_at;
    }
}
