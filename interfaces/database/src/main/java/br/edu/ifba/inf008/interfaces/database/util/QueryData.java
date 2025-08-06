package br.edu.ifba.inf008.interfaces.database.util;

import java.util.List;

public class QueryData {
    private final String sql;
    private final List<Object> parameters;

    public QueryData(final String sql, final List<Object> parameters) {
        this.sql = sql;
        this.parameters = parameters;
    }

    public String getSql() {
        return this.sql;
    }

    public List<Object> getParameters() {
        return this.parameters;
    }
}
