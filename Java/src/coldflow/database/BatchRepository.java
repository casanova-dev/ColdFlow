package coldflow.database;

import coldflow.Batch;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BatchRepository {
// ==========================================
// ADD BATCH
// ==========================================

public boolean addBatch(Batch batch) {

    if (batch == null
            || batch.getBatchId() == null
            || batch.getProductName() == null
            || batch.getExpiryDate() == null
            || batch.getZoneName() == null
            || batch.getZoneName().isBlank()
            || batch.getQuantity() < 0) {

        System.out.println("Invalid batch details.");
        return false;
    }

    String sql = """
        INSERT INTO batches
        (batch_id, product_name, quantity, expiry_date, zone_id)
        SELECT ?, ?, ?, ?, zone_id
        FROM storage_zones
        WHERE LOWER(zone_name) = LOWER(?)
        """;

    try (Connection con = DatabaseManager.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, batch.getBatchId());
        ps.setString(2, batch.getProductName());
        ps.setInt(3, batch.getQuantity());
        ps.setDate(4, Date.valueOf(batch.getExpiryDate()));
        ps.setString(5, batch.getZoneName());

        int rowsInserted = ps.executeUpdate();

        if (rowsInserted > 0) {

            System.out.println(
                    "Batch successfully added to Supabase."
            );

            return true;

        } else {

            System.out.println(
                    "Invalid storage zone: " + batch.getZoneName()
            );

            System.out.println(
                    "Batch was not inserted into the database."
            );

            return false;
        }

    } catch (SQLException e) {

        System.out.println(
                "Error adding batch: " + e.getMessage()
        );

        return false;
    }
}

// ==========================================
// GET ALL BATCHES
// ==========================================

public List<Batch> getAllBatches() {

    List<Batch> batches = new ArrayList<>();

    String sql = """
        SELECT b.batch_id,
               b.product_name,
               b.quantity,
               b.expiry_date,
               COALESCE(z.zone_name, 'Unassigned') AS zone_name
        FROM batches b
        LEFT JOIN storage_zones z
        ON b.zone_id = z.zone_id
        ORDER BY b.expiry_date ASC
        """;

    try (Connection con = DatabaseManager.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

            Batch batch = new Batch(
                    rs.getString("batch_id"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getDate("expiry_date").toLocalDate(),
                    rs.getString("zone_name")
            );

            batches.add(batch);
        }

        System.out.println(
                "Retrieved " + batches.size()
                + " batches from Supabase."
        );

        return batches;

    } catch (SQLException e) {

        throw new RuntimeException(
                "Failed to retrieve inventory from Supabase: "
                + e.getMessage(), e
        );
    }
}

// ==========================================
// UPDATE QUANTITY
// ==========================================

public boolean updateQuantity(String batchId, int quantity) {

    if (batchId == null || quantity < 0) {
        return false;
    }

    String sql = """
        UPDATE batches
        SET quantity = ?
        WHERE batch_id = ?
        """;

    try (Connection con = DatabaseManager.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, quantity);
        ps.setString(2, batchId);

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {

        System.out.println(
                "Error updating quantity: " + e.getMessage()
        );

        return false;
    }
}

// ==========================================
// DELETE BATCH
// ==========================================

public boolean deleteBatch(String batchId) {

    if (batchId == null || batchId.isBlank()) {
        return false;
    }

    String sql = """
        DELETE FROM batches
        WHERE batch_id = ?
        """;

    try (Connection con = DatabaseManager.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, batchId);

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {

        System.out.println(
                "Error deleting batch: " + e.getMessage()
        );

        return false;
    }
}

// ==========================================
// GET NEXT FEFO BATCH
// ==========================================

public Batch getNextFEFOBatch() {

    String sql = """
        SELECT b.batch_id,
               b.product_name,
               b.quantity,
               b.expiry_date,
               COALESCE(z.zone_name, 'Unassigned') AS zone_name
        FROM batches b
        LEFT JOIN storage_zones z
        ON b.zone_id = z.zone_id
        WHERE b.quantity > 0
        AND b.expiry_date >= CURRENT_DATE
        ORDER BY b.expiry_date ASC
        LIMIT 1
        """;

    try (Connection con = DatabaseManager.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        if (rs.next()) {

            return new Batch(
                    rs.getString("batch_id"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getDate("expiry_date").toLocalDate(),
                    rs.getString("zone_name")
            );
        }

    } catch (SQLException e) {

        System.out.println(
                "Error retrieving FEFO batch: " + e.getMessage()
        );
    }

    return null;
}
}
