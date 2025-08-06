package br.edu.ifba.inf008.plugins;

import br.edu.ifba.inf008.interfaces.controller.IPlugin;
import br.edu.ifba.inf008.interfaces.controller.ICore;
import br.edu.ifba.inf008.interfaces.controller.IUIController;

import br.edu.ifba.inf008.interfaces.grid.IGridPlugin;
import br.edu.ifba.inf008.interfaces.form.IFormPlugin;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

public class BooksPlugin implements IGridPlugin<Book>, IFormPlugin<Book>
{
    private ObservableList<Book> books = FXCollections.observableArrayList();

    @Override
    public ObservableList<Book> getTableItems() {
        return this.books;
    }

    @Override
    public Class<Book> getEntityClazz() {
        return Book.class;
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
