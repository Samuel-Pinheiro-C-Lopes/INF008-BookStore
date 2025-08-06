package br.edu.ifba.inf008.plugins;

import br.edu.ifba.inf008.interfaces.database.annotations.*;

import br.edu.ifba.inf008.interfaces.form.IForm;
import br.edu.ifba.inf008.interfaces.form.annotations.*;

import br.edu.ifba.inf008.interfaces.grid.IGrid;
import br.edu.ifba.inf008.interfaces.grid.annotations.*;

import java.time.LocalDateTime;

@Table(name = "books")
public class Book implements IGrid, IForm {
    @Column(name = "book_id", primaryKey = true)
    private Integer id;

    @Column(name = "title")
    @Viewable(name = "Title")
    @Input(label = "Title", position = 1)
    private String title;

    @Column(name = "author")
    @Viewable(name = "Author")
    @Input(label = "Author", position = 2)
    private String author;

    @Column(name = "isbn")
    @Viewable(name = "International Standard Book Number")
    @Input(label = "International Standard Book Number", position = 3)
    private String isbn;

    @Column(name = "published_year")
    @Viewable(name = "Publish Year")
    @Input(label = "Publish Year", position = 4)
    private Integer publishYear;

    @Override
    public String getSelectDisplayText() {
        return this.title;
    }

    @Override
    public String getColumnDisplayText() {
        return this.title;
    }

    public Integer getId() { return this.id; }
    public String getTitle() { return this.title; }
    public String getAuthor() { return this.author; }
    public String getIsbn() { return this.isbn; }
    public Integer getPublishYear() { return this.publishYear; }

    public void setId(Integer id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }
}
