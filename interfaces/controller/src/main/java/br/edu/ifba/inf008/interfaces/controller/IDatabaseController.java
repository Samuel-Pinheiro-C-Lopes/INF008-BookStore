package br.edu.ifba.inf008.interfaces.controller;

import java.util.List;
import java.util.Map;

public interface IDatabaseController {
    Object executeScalarQuery(String query, List<Object> parameters);
    List<Map<String, Object>> executeEntityQuery(String query, List<Object> parameters);
    int executeNonQuery(String query, List<Object> parameters);
}
