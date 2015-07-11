package mvv.app.entity;

import mvv.app.sqlite.Entity;
import mvv.app.sqlite.Id;

/**
 * @author Manh Vu
 */
@Entity("CALD")
public class DictionaryEntity {
    @Id(true)
    public Integer id;
    public String word;
    public byte[] definition;
}
