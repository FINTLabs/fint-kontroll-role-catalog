package no.fintlabs.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;

@Getter
@Setter
@Builder
public class Scope {
    private  @Setter String id;
    private @Setter String objecttype;
    private @Setter List<String> orgunits;

    public Scope() {}

    public Scope(String id, String objecttype, List<String> orgunits)
    {
        this.id = id;
        this.objecttype = objecttype;
        this.orgunits = orgunits;
    }
}
