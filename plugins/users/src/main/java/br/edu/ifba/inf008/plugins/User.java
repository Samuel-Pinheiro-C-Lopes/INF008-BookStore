package br.edu.ifba.inf008.plugins;

import br.edu.ifba.inf008.interfaces.database.annotations.*;

import br.edu.ifba.inf008.interfaces.form.IForm;
import br.edu.ifba.inf008.interfaces.form.annotations.*;

import br.edu.ifba.inf008.interfaces.grid.IGrid;
import br.edu.ifba.inf008.interfaces.grid.annotations.*;

import java.time.LocalDateTime;

@Table(name = "users")
public class User implements IGrid, IForm {
    @Column(name = "user_id", primaryKey = true)
    private Integer id;

    @Column(name = "name")
    @Viewable(name = "Name")
    @Input(label = "Name", position = 1)
    private String name;

    @Column(name = "email")
    @Viewable(name = "E-mail")
    @Input(label = "E-mail", position = 2)
    private String email;

    @Column(name = "registered_at")
    @Viewable(name = "Data de Cadastro")
    private LocalDateTime registeredDate;

    @Override
    public String getSelectDisplayText() {
        return this.name;
    }

    @Override
    public String getColumnDisplayText() {
        return this.name;
    }

    public Integer getId() { return this.id; }
    public String getName() { return this.name; }
    public String getEmail() { return this.email; }
    public LocalDateTime getRegisteredDate() { return this.registeredDate; }

    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRegisteredDate(LocalDateTime registeredDate) { this.registeredDate = registeredDate; }
}
