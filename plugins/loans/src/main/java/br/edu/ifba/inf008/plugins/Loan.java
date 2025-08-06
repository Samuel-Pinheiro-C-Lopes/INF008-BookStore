package br.edu.ifba.inf008.plugins;

import br.edu.ifba.inf008.interfaces.database.annotations.*;

import br.edu.ifba.inf008.interfaces.form.IForm;
import br.edu.ifba.inf008.interfaces.form.annotations.*;
import br.edu.ifba.inf008.interfaces.form.enums.*;

import br.edu.ifba.inf008.interfaces.grid.IGrid;
import br.edu.ifba.inf008.interfaces.grid.annotations.*;

import br.edu.ifba.inf008.plugins.Book;
import br.edu.ifba.inf008.plugins.User;

import java.time.LocalDateTime;

import java.util.List;
import java.util.ArrayList;

@Table(name = "loans")
public class Loan implements IGrid, IForm {
    @Column(name = "loan_id", primaryKey = true)
    private Integer id;

    @Column(name = "book_id")
    @Viewable(name = "Book")
    @Input(label = "Book", position = 1, type = InputType.SELECT)
    private Book book;

    @Input(label = "Books", position = 1, type = InputType.OPTIONS)
    private List<Book> books = new ArrayList<>();

    @Column(name = "user_id")
    @Viewable(name = "User")
    @Input(label = "User", position = 2, type = InputType.SELECT)
    private User user;

    @Input(label = "Users", position = 2,type = InputType.OPTIONS)
    private List<User> users = new ArrayList<>();

    @Column(name = "loan_date")
    @Viewable(name = "Loan Date")
    @Input(label = "Loan Date", position = 3, type = InputType.DATE)
    private LocalDateTime loanDate;

    @Column(name = "return_date")
    @Viewable(name = "Return Date")
    @Input(label = "Return Date", position = 4, type = InputType.DATE)
    private LocalDateTime returnDate;


    @Override
    public String getSelectDisplayText() {
        return this.user.getSelectDisplayText() + "-" + this.book.getSelectDisplayText() + this.loanDate.toString();
    }

    @Override
    public String getColumnDisplayText() {
        return this.user.getSelectDisplayText() + "-" + this.book.getSelectDisplayText() + this.loanDate.toString();
    }

    public Integer getId() { return this.id; }
    public User getUser() { return this.user; }
    public Book getBook() { return this.book; }
    public LocalDateTime getLoanDate() { return this.loanDate; }
    public LocalDateTime getReturnDate() { return this.returnDate; }

    public void setId(Integer id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setBook(Book book) { this.book = book; }
    public void setLoanDate(LocalDateTime loanDate) { this.loanDate = loanDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }
}
