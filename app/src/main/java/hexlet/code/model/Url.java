package hexlet.code.model;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "urls")
public class Url extends Model {

    @Id
    @GeneratedValue
    private int id;
    private String name;
    @WhenCreated
    private Date created_at;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }
}
