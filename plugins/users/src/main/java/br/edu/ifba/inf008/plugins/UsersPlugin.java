package br.edu.ifba.inf008.plugins;

import br.edu.ifba.inf008.interfaces.controller.IPlugin;
import br.edu.ifba.inf008.interfaces.controller.ICore;
import br.edu.ifba.inf008.interfaces.controller.IUIController;

import br.edu.ifba.inf008.interfaces.grid.IGridPlugin;
import br.edu.ifba.inf008.interfaces.form.IFormPlugin;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

public class UsersPlugin implements IGridPlugin<User>, IFormPlugin<User>
{
    private ObservableList<User> users = FXCollections.observableArrayList();

    @Override
    public ObservableList<User> getTableItems() {
        return this.users;
    }

    @Override
    public Class<User> getEntityClazz() {
        return User.class;
    }


    @Override
    public boolean initForm() {
        if (this.addForm() == false) return false;

        return true;
    }

    @Override
    public boolean initGrid() {
        if (this.addTable(null) == false) return false;

        return true;
    }
}
