import java.io.Serializable;
import java.util.Date;

/**
 * Created by Steven on 2017-11-13.
 */
public class Artifact implements Serializable{
    private String id;
    private String name;
    private String creator;
    private int centuryOfCreation;
    private String placeOfCreation;
    private String genre;

    public Artifact(String id, String name, String creator, int centuryOfCreation, String placeOfCreation, String genre) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.centuryOfCreation = centuryOfCreation;
        this.placeOfCreation = placeOfCreation;
        this.genre = genre;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreator() {
        return creator;
    }

    public int getCenturyOfCreation() {
        return centuryOfCreation;
    }

    public String getPlaceOfCreation() {
        return placeOfCreation;
    }

    public String getGenre() {
        return genre;
    }

    @Override
    public String toString() {
        return "Artifact{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", creator='" + creator + '\'' +
                ", dateOfCreation='" + centuryOfCreation + '\'' +
                ", placeOfCreation='" + placeOfCreation + '\'' +
                ", genre='" + genre + '\'' +
                '}';
    }
}
