package br.edu.ifba.inf008.plugins;

import br.edu.ifba.inf008.interfaces.controller.ICore;
import br.edu.ifba.inf008.interfaces.grid.IGridPlugin;
import br.edu.ifba.inf008.plugins.Loan;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class BooksInLoanPlugin implements IGridPlugin<Loan> {

    private ObservableList<Loan> loans = FXCollections.observableArrayList();

    @Override
    public ObservableList<Loan> getTableItems() {
        return this.loans;
    }

    @Override
    public Class<Loan> getEntityClazz() {
        return Loan.class;
    }

    @Override
    public boolean initGrid() {
        if (this.addTable("Books in Loan") == false) {
            return false;
        }

        return true;
    }

    @Override
    public boolean tryBindTableData(final TableView<Loan> table) {
        final List<Loan> selectedBd = new Vector<>();

        if (this.select(selectedBd) == false) {
            return false;
        }

        final List<Loan> filteredLoans = selectedBd.stream()
                .filter(s -> s.getReturnDate() == null)
                .collect(Collectors.toList());

        final ObservableList<Loan> tableItems = this.getTableItems();

        tableItems.setAll(filteredLoans);

        table.setItems(tableItems);

        return true;
    }
}
