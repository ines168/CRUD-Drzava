import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DataSource dataSource = createDataSource();

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Uspješno ste spojeni na bazu podataka!");

            Scanner sc = new Scanner(System.in);
            while (true) {
                ispisMenija();
                System.out.print("Vaš izbor: ");
                int izbor;
                try {
                    izbor = sc.nextInt();
                    sc.nextLine(); //očisti nextInt enter
                } catch (InputMismatchException e) {
                    System.err.println("Greška! Molim unesi broj!");
                    sc.nextLine(); //clear endless loop
                    continue;
                }

                switch (izbor) {
                    case 1 -> createDrzava(connection, sc);
                    case 2 -> updateDrzava(connection, sc);
                    case 3 -> deleteDrzava(connection, sc);
                    case 4 -> indexDrzava(connection);
                    case 5 -> {
                        System.out.println("Kraj programa.");
                        return;
                    }
                    default -> System.err.println("Nije validan unos. Pokušaj ponovno!");
                }

            }

        }
        catch (SQLException e) {
            System.err.println("Greška prilikom spajanja na bazu podataka.");
            e.printStackTrace();
        }

    }
    public static void ispisMenija() {
        System.out.println("\nIzaberi slijedeće:");
        System.out.println("1 - Unos nove države");
        System.out.println("2 - izmjena postojeće države");
        System.out.println("3 - brisanje postojeće države");
        System.out.println("4 - prikaz svih država sortiranih po nazivu");
        System.out.println("5 - end");
    }

    public static void createDrzava(Connection connection, Scanner sc) throws SQLException {
        Statement stmt = connection.createStatement();
        System.out.println("Upiši naziv nove države:");
        String nazivDrzave = sc.nextLine().trim();
        if(nazivDrzave.isEmpty()) {
            System.err.println("Naziv ne može biti prazan.");
            return;
        }
        //check if exists
        ResultSet foundDrzava = stmt.executeQuery(String.format("SELECT COUNT(*) FROM Drzava WHERE Naziv = '%s'", nazivDrzave));
        if(foundDrzava.next() && foundDrzava.getInt(1) > 0) {
            System.err.println("Država već postoji!");
            return;
        }
        String sql = String.format("INSERT INTO Drzava(Naziv) VALUES ('%s')", nazivDrzave);
        int rowAffected = stmt.executeUpdate(sql);
        System.out.println(rowAffected > 0 ? "Država je uspješno unesena!" : "Nije unešena država!");
        stmt.close();
    }
    public static void updateDrzava(Connection connection, Scanner sc) throws SQLException {
        Statement stmt = connection.createStatement();
        System.out.println("Upiši ID države koju želiš izmijeniti:");
        int idDrzave = sc.nextInt();
        //check if exists
        ResultSet foundID = stmt.executeQuery(String.format("SELECT COUNT(*) FROM Drzava WHERE IDDrzava = '%s'", idDrzave));
        if(foundID.next() && foundID.getInt(1) == 0) {
            System.err.println("Država s tim ID-em ne postoji!");
            return;
        }
        System.out.println("Upiši novi naziv države:");
        String nazivDrzave = sc.next();
        //check if exists
        ResultSet foundDrzava = stmt.executeQuery(String.format("SELECT COUNT(*) FROM Drzava WHERE Naziv = '%s'", nazivDrzave));
        if(foundDrzava.next() && foundDrzava.getInt(1) > 0) {
            System.err.println("Država s tim nazivom već postoji!");
            return;
        }
        String sql = String.format("UPDATE Drzava SET Naziv = '%s' WHERE IDDrzava = %s", nazivDrzave, idDrzave);
        int rowAffected = stmt.executeUpdate(sql);
        System.out.println(rowAffected > 0 ? "Država je uspješno preimenovana!" : "Nije pronađena država!");
        stmt.close();
    }

    public static void deleteDrzava(Connection connection, Scanner sc) throws SQLException {
        Statement stmt = connection.createStatement();
        System.out.println("Upiši ID države koju želiš obrisati:");
        int idDrzave = sc.nextInt();
        String sql = String.format("DELETE FROM Drzava WHERE IDDrzava=%s", idDrzave);
        int rowAffected = stmt.executeUpdate(sql);
        System.out.println(rowAffected > 0 ? "Država je uspješno obrisana!" : "Nije pronađena država!");
        stmt.close();
    }

    public static void indexDrzava(Connection connection) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT IDDrzava, Naziv FROM Drzava ORDER BY Naziv");
        System.out.println("\nPopis država po nazivu:");

        while (rs.next()) {
            int id = rs.getInt("IDDrzava");
            String naziv = rs.getString("Naziv");
            System.out.println(naziv + ", ID: " + id );
        }
        rs.close();
        stmt.close();
    }

    private static DataSource createDataSource() {
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setServerName("localhost");
        ds.setPortNumber(1433);
        ds.setDatabaseName("AdventureWorksOBP");
        ds.setUser("sa");
        ds.setPassword("SQL");
        ds.setEncrypt(false);
        return ds;
    }
}