package cz.hlavja.ir.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Query {
    private String query;
    private boolean vectorModel;
    private Integer numberOfResults;
}
