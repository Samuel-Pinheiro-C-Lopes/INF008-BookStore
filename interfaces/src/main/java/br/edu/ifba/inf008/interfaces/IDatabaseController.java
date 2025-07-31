package br.edu.ifba.inf008.interfaces;

public interface IDatabaseController {
    Object executeScalarQuery(String query);
    List<Map<String, Object>> executeEntityQuery(String query);
    int executeNonQuery(String query);
}
