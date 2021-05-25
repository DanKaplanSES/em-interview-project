package com.sleepeasysoftware.eminterviewproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.sql.*;

/**
 * Created by Daniel Kaplan on behalf of Sleep Easy Software.
 */
@Component
public class ApplicationUsage implements ApplicationRunner {


    private final Connection connection;

    @Autowired
    public ApplicationUsage(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        try (connection) {
            connection.prepareStatement("CREATE TABLE CATALOG (NDC VARCHAR(255), Catalog_Number VARCHAR(255), Price NUMBER(10, 2)) AS SELECT * FROM CSVREAD('catalog-1.csv');").execute();
            connection.prepareStatement("INSERT INTO CATALOG SELECT * FROM CSVREAD('catalog-2.csv');").execute();
            connection.prepareStatement("CREATE TABLE PURCHASES (NDC VARCHAR(255), Quantity INT, Cost NUMBER(10, 2), Extended_Cost NUMBER(10, 2)) AS SELECT * FROM CSVREAD('purchases.csv');").execute();
            connection.prepareStatement("CREATE TABLE CATALOG_PRODUCTS AS SELECT NDC, MIN(Price) AS price FROM CATALOG GROUP BY NDC").execute();
            connection.prepareStatement("CREATE TABLE SUMMED_PURCHASES AS SELECT NDC, SUM(Quantity) as total_quantity, SUM(Extended_Cost) as total_extended_cost FROM PURCHASES GROUP BY NDC").execute();
            connection.prepareStatement("CREATE TABLE LAST_MONTH_TOTALS AS SELECT NDC, SUM(Extended_Cost) as total_cost_last_month FROM PURCHASES GROUP BY NDC").execute();
            connection.prepareStatement("CREATE TABLE ESTIMATED_TOTALS AS SELECT SUMMED_PURCHASES.NDC, SUMMED_PURCHASES.total_quantity * CATALOG_PRODUCTS.price AS price FROM SUMMED_PURCHASES INNER JOIN CATALOG_PRODUCTS ON SUMMED_PURCHASES.NDC = CATALOG_PRODUCTS.NDC").execute();
            connection.prepareStatement("CREATE TABLE IMPACTS AS SELECT et.NDC as NDC, et.price - lmt.total_cost_last_month AS impact FROM LAST_MONTH_TOTALS lmt INNER JOIN ESTIMATED_TOTALS et ON lmt.NDC = et.NDC").execute();

            System.out.println("NDC, IMPACT");
            printSelect(connection, "SELECT * FROM IMPACTS ORDER BY impact DESC");
            System.out.print("Grand Total: ");
            printSelect(connection, "SELECT SUM(IMPACT) FROM IMPACTS");
        }
    }

    public void printSelect(Connection conn, String sql) throws SQLException {
        ResultSet resultSet = conn.createStatement().executeQuery(sql);
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        while (resultSet.next()) {
            for (int i = 1; i <= columnsNumber; i++) {
                if (i > 1) System.out.print(",  ");
                String columnValue = resultSet.getString(i);
                System.out.print(columnValue);
            }
            System.out.println("");
        }
    }
}
