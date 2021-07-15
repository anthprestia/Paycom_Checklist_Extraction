package org.extract.Data;

import java.util.List;

public class DataTable {

    private String name;
    private List<TableRows> rowsList;

    public DataTable(String name, List<TableRows> rowsList) {
        this.name = name;
        this.rowsList = rowsList;
    }


    @Override
    public String toString() {
        return rowsList.toString();
    }
}
