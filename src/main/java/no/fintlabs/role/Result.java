package no.fintlabs.role;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@Builder
public class Result {
    List<Scope> result;

    public Result() {};

    public Result(List<Scope> scopes) {
        this.result =   scopes;
    };
}
