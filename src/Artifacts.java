import java.util.Date;

/**
 * Created by Steven on 2017-11-13.
 */
public class Artifacts {
    private int id;
    private String name;
    private String creator;
    private String dateOfCreation;
    private String placeOfCreation;
    private String genre;

    public Artifacts(int id, String name, String creator, String dateOfCreation, String placeOfCreation, String genre) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.dateOfCreation = dateOfCreation;
        this.placeOfCreation = placeOfCreation;
        this.genre = genre;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreator() {
        return creator;
    }

    public String getDateOfCreation() {
        return dateOfCreation;
    }

    public String getPlaceOfCreation() {
        return placeOfCreation;
    }

    public String getGenre() {
        return genre;
    }
}
