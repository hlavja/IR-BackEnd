package cz.zcu.kiv.nlp.ir.trec.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * DTO for communication with frontend
 * @author hlavj on 1.5.2021
 */
@Data
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class QueryModel {
    /**
     * Search query
     */
    private String query;

    /**
     * Determines if use vector or boolean model
     * false > boolean model
     * true > vector model
     */
    private boolean vectorModel;

    /**
     * Number of results to return back ti the frontend
     */
    private Integer numberOfResults;
}
