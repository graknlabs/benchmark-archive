/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2018 Grakn Labs Ltd
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.runner.storage;

import grakn.core.concept.Attribute;
import grakn.core.concept.AttributeType;
import grakn.core.concept.Concept;
import grakn.core.concept.ConceptId;
import grakn.core.concept.EntityType;
import grakn.core.concept.Label;
import grakn.core.concept.RelationshipType;
import grakn.core.concept.SchemaConcept;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static grakn.core.concept.AttributeType.DataType.BOOLEAN;
import static grakn.core.concept.AttributeType.DataType.DATE;
import static grakn.core.concept.AttributeType.DataType.DOUBLE;
import static grakn.core.concept.AttributeType.DataType.FLOAT;
import static grakn.core.concept.AttributeType.DataType.LONG;
import static grakn.core.concept.AttributeType.DataType.STRING;

/**
 * Stores identifiers for all concepts in a Grakn
 */
public class IgniteConceptIdStore implements IdStoreInterface {

    private final HashSet<String> entityTypeLabels;
    private final HashSet<String> relationshipTypeLabels;
    private final HashMap<java.lang.String, AttributeType.DataType<?>> attributeTypeLabels; // typeLabel, datatype
    private HashMap<String, String> typeLabelsTotableNames = new HashMap<>();

    private Connection conn;
    private HashSet<String> allTypeLabels;
    private final String cachingMethod = "REPLICATED";
    private final int ID_INDEX = 1;

    public static final Map<AttributeType.DataType<?>, String> DATATYPE_MAPPING;
    static {
        Map<AttributeType.DataType<?>, String> mapBuilder = new HashMap<>();
        mapBuilder.put(STRING, "VARCHAR");
        mapBuilder.put(BOOLEAN, "BOOLEAN");
        mapBuilder.put(LONG, "LONG");
        mapBuilder.put(DOUBLE, "DOUBLE");
        mapBuilder.put(FLOAT, "FLOAT");
        mapBuilder.put(DATE, "DATE");
        DATATYPE_MAPPING = Collections.unmodifiableMap(mapBuilder);
    }

    public IgniteConceptIdStore(HashSet<EntityType> entityTypes,
                                HashSet<RelationshipType> relationshipTypes,
                                HashSet<AttributeType> attributeTypes) {

        this.entityTypeLabels = this.getTypeLabels(entityTypes);
        this.relationshipTypeLabels = this.getTypeLabels(relationshipTypes);
        this.attributeTypeLabels = this.getAttributeTypeLabels(attributeTypes);

        this.allTypeLabels = this.getAllTypeLabels();

        try {
            clean(this.allTypeLabels);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Register JDBC driver.
        try {
            Class.forName("org.apache.ignite.IgniteJdbcThinDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Open JDBC connection.
        try {
            this.conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create database tables.
        for (String typeLabel : this.entityTypeLabels) {
            this.createConceptIdTable(typeLabel);
        }

        for (String typeLabel : this.relationshipTypeLabels) {
            this.createConceptIdTable(typeLabel);
        }

//        for (String typeLabel : this.relationshipTypeLabels) {
        for (Map.Entry<String, AttributeType.DataType<?>> entry : this.attributeTypeLabels.entrySet()) {
            String typeLabel = entry.getKey();
            AttributeType.DataType<?> datatype = entry.getValue();

            String dbDatatype = DATATYPE_MAPPING.get(datatype);
            this.createTable(typeLabel, dbDatatype);
        }
    }

    private <T extends SchemaConcept> HashSet<String> getTypeLabels(Set<T> conceptTypes) {
        HashSet<String> typeLabels = new HashSet<>();
        for (T conceptType : conceptTypes) {
            typeLabels.add(conceptType.label().toString());
        }
        return typeLabels;
    }

    private HashMap<String, AttributeType.DataType<?>> getAttributeTypeLabels(Set<AttributeType> conceptTypes) {
        HashMap<String, AttributeType.DataType<?>> typeLabels = new HashMap<>();
        for (AttributeType conceptType : conceptTypes) {
            String label = conceptType.label().toString();

            AttributeType.DataType<?> datatype = conceptType.dataType();
            typeLabels.put(label, datatype);
        }
        return typeLabels;
    }

    private HashSet<String> getAllTypeLabels() {
        HashSet<String> allLabels = new HashSet<>();
        allLabels.addAll(this.entityTypeLabels);
        allLabels.addAll(this.relationshipTypeLabels);
        allLabels.addAll(this.attributeTypeLabels.keySet());
        return allLabels;
    }

    private void createConceptIdTable(String typeLabel) {
        createTable(typeLabel, "VARCHAR");
    }

    private void createTable(String typeLabel, String sqlDatatypeName) {

        String tableName = this.putTableName(typeLabel);

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE " + tableName + " (" +
                    " id " + sqlDatatypeName + " PRIMARY KEY, " +
                    "nothing LONG) " +
                    " WITH \"template=" + cachingMethod + "\"");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String convertTypeLabelToTableName(String typeLabel) {
        return typeLabel.replace('-', '_')
                .replaceAll("[0-9]", "");
    }

    private String putTableName(String typeLabel) {
        String tableName = this.convertTypeLabelToTableName(typeLabel);
        this.typeLabelsTotableNames.put(typeLabel, tableName);
        return tableName;
    }

    private String getTableName(String typeLabel) {

        String tableName = this.typeLabelsTotableNames.get(typeLabel);
        if (tableName != null) {
            return tableName;
        } else {
            // TODO Don't need this else clause if I can figure out how to drop all tables in clean()
            return convertTypeLabelToTableName(typeLabel);
        }
    }

    @Override
    public void add(Concept concept) {

        Label conceptTypeLabel = concept.asThing().type().label();
        String tableName = this.getTableName(conceptTypeLabel.toString());

        if (concept.isAttribute()) {
            Attribute<?> attribute = concept.asAttribute();
            AttributeType.DataType<?> datatype = attribute.dataType();

            Object value = attribute.value();
            try (PreparedStatement stmt = this.conn.prepareStatement(
                    "INSERT INTO " + tableName + " (id, ) VALUES (?, )")) {

                if (value.getClass() == String.class) {
                    stmt.setString(ID_INDEX, (String) value);

                } else if (value.getClass() == Double.class) {
                    stmt.setDouble(ID_INDEX, (Double) value);

                } else if (value.getClass() == Long.class || value.getClass() == Integer.class) {
                    stmt.setLong(ID_INDEX, (Long) value);

                } else if (value.getClass() == Boolean.class) {
                    stmt.setBoolean(ID_INDEX, (Boolean) value);

                } else if (value.getClass() == Date.class) {
                    stmt.setDate(ID_INDEX, (Date) value);
                } else {
                    throw new UnsupportedOperationException(String.format("Datatype %s isn't supported by Grakn", datatype));
                }

                stmt.executeUpdate();

            } catch (SQLException e) {
                if (!e.getSQLState().equals("23000")) {
                    // TODO Doesn't seem like the right way to go
                    // In the case of duplicate primary key, which we want to ignore since I want to keep a unique set of
                    // attribute values in each table
                    e.printStackTrace();
                }
            }

        } else {

            String conceptId = concept.asThing().id().toString(); // TODO use the value instead for attributes

            try (PreparedStatement stmt = this.conn.prepareStatement(
                    "INSERT INTO " + tableName + " (id, ) VALUES (?, )")) {

                stmt.setString(ID_INDEX, conceptId);
                stmt.executeUpdate();

            } catch (SQLException e) {
                if (!e.getSQLState().equals("23000")) {
                    // TODO Doesn't seem like the right way to go
                    // In the case of duplicate primary key, which we want to ignore since I want to keep a unique set of
                    // attribute values in each table
                    e.printStackTrace();
                }
            }
        }
    }

    /*
    [{ LIMIT expression [OFFSET expression]
    [SAMPLE_SIZE rowCountInt]} | {[OFFSET expression {ROW | ROWS}]
    [{FETCH {FIRST | NEXT} expression {ROW | ROWS} ONLY}]}]
     */

    private String sqlGet(String typeLabel, int offset) {
        String sql = "SELECT id FROM " + getTableName(typeLabel) +
                " OFFSET " + offset +
                " FETCH FIRST ROW ONLY";
        return sql;
    }

    public ConceptId getConceptId(String typeLabel, int offset) {
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sqlGet(typeLabel, offset))) {
                if (rs != null && rs.next()) { // Need to do this to increment one line in the ResultSet
                    return ConceptId.of(rs.getString(ID_INDEX));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getString(String typeLabel, int offset) {
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sqlGet(typeLabel, offset))) {
                if (rs != null && rs.next()) { // Need to do this to increment one line in the ResultSet
                    return rs.getString(ID_INDEX);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    public Double getDouble(String typeLabel, int offset) {
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sqlGet(typeLabel, offset))) {
                if (rs != null && rs.next()) { // Need to do this to increment one line in the ResultSet
                    return rs.getDouble(ID_INDEX);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Long getLong(String typeLabel, int offset) {
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sqlGet(typeLabel, offset))) {
                if (rs != null && rs.next()) { // Need to do this to increment one line in the ResultSet
                    return rs.getLong(ID_INDEX);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Boolean getBoolean(String typeLabel, int offset) {
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sqlGet(typeLabel, offset))) {
                if (rs != null && rs.next()) { // Need to do this to increment one line in the ResultSet
                    return rs.getBoolean(ID_INDEX);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Date getDate(String typeLabel, int offset) {
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sqlGet(typeLabel, offset))) {
                if (rs != null && rs.next()) { // Need to do this to increment one line in the ResultSet
                    return rs.getDate(ID_INDEX);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getConceptCount(String typeLabel) {

        String sql = "SELECT COUNT(1) FROM " + getTableName(typeLabel);
//        ResultSet rs = this.runQuery(sql);

        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {

                if (rs != null && rs.next()) { // Need to do this to increment one line in the ResultSet
                    return rs.getInt(1);
                } else {
                    return 0;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int total() {
        int total = 0;
        for (String typeLabel : this.allTypeLabels) {
            total += this.getConceptCount(typeLabel);
        }
        return total;
    }

//    private ResultSet runQuery(String sql) {
//        try (Statement stmt = conn.createStatement()) {
//            try (ResultSet rs = stmt.executeQuery(sql)) {
//                return rs;
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * Clean all elements in the storage
     */
    public void clean(Set<String> typeLabels) throws SQLException {
//        Connection conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/");
//        try (Statement stmt = conn.createStatement()) {
//            try (ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
//                rs.next();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

//        try (PreparedStatement stmt = conn.prepareStatement("DROP DATABASE ")) {
//            stmt.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

        // TODO figure out how to drop all tables
        for (String typeLabel : typeLabels) {
            Connection conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/");
            try (PreparedStatement stmt = conn.prepareStatement("DROP TABLE IF EXISTS " + this.getTableName(typeLabel))) {
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
