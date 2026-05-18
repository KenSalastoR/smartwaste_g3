package org.divini.smartwaste_g3.database;

import org.divini.smartwaste_g3.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;
import java.time.LocalTime;

public class CassonettoDAO {

    private ConnessioneDatabase conn;

    public CassonettoDAO(ConnessioneDatabase conn) {
        this.conn = conn;
    }

    // ===========================
    // INSERT
    // ===========================
    public boolean inserisci(Cassonetto c) {
        String sql = "INSERT INTO cassonetti_SmartWaste_G3 " + "(latitudine, longitudine, data_installazione, ora_installazione, tipologia, valore, capacita) " + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection co = conn.getConnection();
             PreparedStatement stmt = co.prepareStatement(sql)) {

            stmt.setDouble(1, c.getLatitudine());
            stmt.setDouble(2, c.getLongitudine());
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.setTime(4, Time.valueOf(LocalTime.now()));
            stmt.setString(5, c.getTipologia().name());

            double valoreAttuale = c.getPercentualeRiempimento() * c.getCapacita() / 100;
            stmt.setDouble(6, valoreAttuale);

            stmt.setDouble(7, c.getCapacita());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ===========================
    // UPDATE (per SVUOTARE)
    // ===========================
    public boolean aggiorna(Cassonetto c) {
        String sql = "UPDATE cassonetti_SmartWaste_G3 SET " +
                "valore = ?, capacita = ?, data_svuotamento = ?, ora_svuotamento = ? " +
                "WHERE codice = ?";

        try (Connection co = conn.getConnection();
             PreparedStatement stmt = co.prepareStatement(sql)) {

            double valoreAttuale = c.getPercentualeRiempimento() * c.getCapacita() / 100;

            stmt.setDouble(1, valoreAttuale);
            stmt.setDouble(2, c.getCapacita());

            // Se è stato svuotato → registra data/ora
            if (valoreAttuale == 0) {
                stmt.setDate(3, Date.valueOf(LocalDate.now()));
                stmt.setTime(4, Time.valueOf(LocalTime.now()));
            } else {
                stmt.setNull(3, Types.DATE);
                stmt.setNull(4, Types.TIME);
            }

            stmt.setInt(5, c.getCodice());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ===========================
    // SELECT ALL
    // ===========================
    public List<Cassonetto> getTutti() {
        List<Cassonetto> lista = new ArrayList<>();
        String sql = "SELECT * FROM cassonetti_SmartWaste_G3";

        try (Connection co = conn.getConnection();
             PreparedStatement stmt = co.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(creaCassonettoDaResultSet(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return lista;
    }

    // ===========================
    // DELETE
    // ===========================
    public boolean elimina(int codice) {
        String sql = "DELETE FROM cassonetti_SmartWaste_G3 WHERE codice = ?";

        try (Connection co = conn.getConnection();
             PreparedStatement stmt = co.prepareStatement(sql)) {

            stmt.setInt(1, codice);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ===========================
    // SELECT BY CODICE
    // ===========================
    public Cassonetto cerca(int codice) {
        String sql = "SELECT * FROM cassonetti_SmartWaste_G3 WHERE codice = ?";

        try (Connection co = conn.getConnection();
             PreparedStatement stmt = co.prepareStatement(sql)) {

            stmt.setInt(1, codice);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return creaCassonettoDaResultSet(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    // ===========================
    // SELECT BY TIPOLOGIA
    // ===========================
    public List<Cassonetto> getPerTipologia(TipologiaRifiuto tipo) {
        List<Cassonetto> lista = new ArrayList<>();
        String sql = "SELECT * FROM cassonetti_SmartWaste_G3 WHERE tipologia = ?";

        try (Connection co = conn.getConnection();
             PreparedStatement stmt = co.prepareStatement(sql)) {

            stmt.setString(1, tipo.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(creaCassonettoDaResultSet(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return lista;
    }

    // ===========================
    // SELECT CASSOMETTI DA SVUOTARE (>90%)
    // ===========================
    public List<Cassonetto> getCassonettiDaSvuotare() {
        List<Cassonetto> lista = new ArrayList<>();
        String sql = "SELECT * FROM cassonetti_SmartWaste_G3 " +
                "WHERE (valore / capacita) * 100 > 90";

        try (Connection co = conn.getConnection();
             PreparedStatement stmt = co.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(creaCassonettoDaResultSet(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return lista;
    }

    // ===========================
    // RECONSTRUCTOR
    // ===========================
    private Cassonetto creaCassonettoDaResultSet(ResultSet rs) throws SQLException {

        int codice = rs.getInt("codice");
        double lat = rs.getDouble("latitudine");
        double lon = rs.getDouble("longitudine");
        String tipo = rs.getString("tipologia");
        double capacita = rs.getDouble("capacita");
        double valore = rs.getDouble("valore");

        // ====== LEGGI DATE E ORARI ======
        LocalDate dataInst = null;
        LocalTime oraInst = null;

        if (rs.getDate("data_installazione") != null)
            dataInst = rs.getDate("data_installazione").toLocalDate();

        if (rs.getTime("ora_installazione") != null)
            oraInst = rs.getTime("ora_installazione").toLocalTime();

        LocalDate dataSvuot = null;
        LocalTime oraSvuot = null;

        if (rs.getDate("data_svuotamento") != null)
            dataSvuot = rs.getDate("data_svuotamento").toLocalDate();

        if (rs.getTime("ora_svuotamento") != null)
            oraSvuot = rs.getTime("ora_svuotamento").toLocalTime();

        // ====== CREA IL CASSONETTO ======
        Cassonetto c = switch (TipologiaRifiuto.valueOf(tipo)) {
            case Organico -> new Organico(codice, lat, lon, dataInst, oraInst, capacita);
            case Vetro -> new Vetro(codice, lat, lon, dataInst, oraInst, (int) capacita);
            case Carta -> new Carta(codice, lat, lon, dataInst, oraInst, capacita);
            case Plastica -> new Plastica(codice, lat, lon, dataInst, oraInst, capacita);
            case Indifferenziata -> new Indifferenziata(codice, lat, lon, dataInst, oraInst, capacita);
        };

        // ====== IMPOSTA DATA SVUOTAMENTO SE ESISTE ======
        c.setDataSvuotamento(dataSvuot);
        c.setOraSvuotamento(oraSvuot);

        // ====== IMPOSTA IL VALORE ======
        c.aggiorna(valore);

        return c;
    }
}
