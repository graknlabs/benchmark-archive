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
    private final int VALUE_INDEX = 2;

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
            dropTable("roleplayers"); // one special table for tracking role players
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
            this.createTypeIdsTable(typeLabel);
        }

        for (String typeLabel : this.relationshipTypeLabels) {
            this.createTypeIdsTable(typeLabel);
        }

        for (Map.Entry<String, AttributeType.DataType<?>> entry : this.attributeTypeLabels.entrySet()) {
            String typeLabel = entry.getKey();
            AttributeType.DataType<?> datatype = entry.getValue();
            String dbDatatype = DATATYPE_MAPPING.get(datatype);
            this.createAttributeValueTable(typeLabel, dbDatatype);
        }

        // re-create special table
        // role players that have been assigned into a relationship at some point
        createTable("roleplayers", "VARCHAR");
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

    /**
     * Create a table for storing concept IDs of the given type
     * @param typeLabel
     */
    private void createTypeIdsTable(String typeLabel) {
        String tableName = this.putTableName(typeLabel);
        createTable(tableName, "VARCHAR");
    }

    /**
     * Create a table for storing attributeValues for the given type
     * this is a TWO column table of attribute ID and attribute value
     * @param typeLabel
     * @param sqlDatatypeName
     */
    private void createAttributeValueTable(String typeLabel, String sqlDatatypeName) {

        String tableName = this.putTableName(typeLabel);
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE " + tableName + " (" +
                    " id VARCHAR PRIMARY KEY, " +
                    " value " + sqlDatatypeName + ", " +
                    "nothing LONG) " +
                    " WITH \"template=" + cachingMethod + "\"");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a single column table with the value stored being of the given sqlDatatype
     * @param tableName
     * @param sqlDatatypeName
     */
    private void createTable(String tableName, String sqlDatatypeName) {

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
    public void addConcept(Concept concept) {

        Label conceptTypeLabel = concept.asThing().type().label();
        String tableName = this.getTableName(conceptTypeLabel.toString());
        String conceptId = concept.asThing().id().toString(); // TODO use the value instead for attributes

        if (concept.isAttribute()) {
            Attribute<?> attribute = concept.asAttribute();
            AttributeType.DataType<?> datatype = attribute.dataType();

            Object value = attribute.value();
            try (PreparedStatement stmt = this.conn.prepareStatement(
                    "INSERT INTO " + tableName + " (id, value, ) VALUES (?, ?, )")) {

                if (value.getClass() == String.class) {
                    stmt.setString(VALUE_INDEX, (String) value);

                } else if (value.getClass() == Double.class) {
                    stmt.setDouble(VALUE_INDEX, (Double) value);

                } else if (value.getClass() == Long.class || value.getClass() == Integer.class) {
                    stmt.setLong(VALUE_INDEX, (Long) value);

                } else if (value.getClass() == Boolean.class) {
                    stmt.setBoolean(VALUE_INDEX, (Boolean) value);

                } else if (value.getClass() == Date.class) {
                    stmt.setDate(VALUE_INDEX, (Date) value);
                } else {
                    throw new UnsupportedOperationException(String.format("Datatype %s isn't supported by Grakn", datatype));
                }

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

        } else {


            try (PreparedStatement stmt = this.conn.prepareStatement(
                    "INSERT INTO " + tableName + " (id, ) VALUES (?, )")) {

                stmt.setString(ID_INDEX, conceptId);
                stmt.executeUpdate();

            } catch (SQLException e) {
                if (!e.getSQLState().equals("23000")) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addRolePlayer(Concept concept) {
        String conceptId = concept.asThing().id().toString();
        try (PreparedStatement stmt = this.conn.prepareStatement(
                "INSERT INTO roleplayers (id, ) VALUES (?, )")) {

            stmt.setString(ID_INDEX, conceptId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            // TODO Doesn't seem like the right way to go
            // In the case of duplicate primary key, which we want to ignore since I want to keep a unique set of
            // attribute values in each table
            e.printStackTrace();
        }
    }

    /*
    [{ LIMIT expression [OFFSET expression]
    [SAMPLE_SIZE rowCountInt]} | {[OFFSET expression {ROW | ROWS}]
    [{FETCH {FIRST | NEXT} expression {ROW | ROWS} ONLY}]}]
     */

    private String sqlGetId(String typeLabel, int offset) {
        String sql = "SELECT id FROM " + getTableName(typeLabel) +
                " OFFSET " + offset +
                " FETCH FIRST ROW ONLY";
        return sql;
    }

    private String sqlGetAttrValue(String typeLabel, int offset) {
        String sql = "SELECT value FROM " + getTableName(typeLabel) +
                " OFFSET " + offset +
                " FETCH FIRST ROW ONLY";
        return sql;
    }

    public ConceptId getConceptId(String typeLabel, int offset) {
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sqlGetId(typeLabel, offset))) {
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
            try (ResultSet rs = stmt.executeQuery(sqlGetAttrValue(typeLabel, offset))) {
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
            try (ResultSet rs = stmt.executeQuery(sqlGetAttrValue(typeLabel, offset))) {
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
            try (ResultSet rs = stmt.executeQuery(sqlGetAttrValue(typeLabel, offset))) {
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
            try (ResultSet rs = stmt.executeQuery(sqlGetAttrValue(typeLabel, offset))) {
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
            try (ResultSet rs = stmt.executeQuery(sqlGetAttrValue(typeLabel, offset))) {
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
        String tableName = getTableName(typeLabel);
        return getCount(tableName);
    }


    private int getCount(String tableName) {
        String sql = "SELECT COUNT(1) FROM " + tableName;

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
    public int totalRolePlayers() {
        return getCount("roleplayers");
    }

    /**
     * Orphan entities = Set(all entities) - Set(entities playing roles)
     * @return
     */
    @Override
    public int totalOrphanEntities() {
        Set<String> rolePlayerIds = getIds("roleplayers");
        Set<String> entityIds = new HashSet<>();
        for (String typeLabel: this.entityTypeLabels) {
            Set<String> ids = getIds(getTableName(typeLabel));
            entityIds.addAll(ids);
        }
        entityIds.removeAll(rolePlayerIds);
        return entityIds.size();
    }

    /**
     * Orphan attributes = Set(all attribute ids) - Set(attributes playing roles)
     * @return
     */
    @Override
    public int totalOrphanAttributes() {
        Set<String> rolePlayerIds = getIds("roleplayers");
        Set<String> attributeIds = new HashSet<>();
        for (String typeLabel: this.attributeTypeLabels.keySet()) {
            Set<String> ids = getIds(getTableName(typeLabel));
            attributeIds.addAll(ids);
        }
        attributeIds.removeAll(rolePlayerIds);
        return attributeIds.size();
    }

    /**
     * Double counting between relationships and relationships also playing roles
     * = Set(All relationship ids) intersect Set(role players)
     * @return
     */
    @Override
    public int totalRelationshipsRolePlayersOverlap() {
        Set<String> rolePlayerIds = getIds("roleplayers");
        Set<String> relationshipIds = new HashSet<>();
        for (String typeLabel: this.relationshipTypeLabels) {
            Set<String> ids = getIds(getTableName(typeLabel));
            relationshipIds.addAll(ids);
        }
        relationshipIds.retainAll(rolePlayerIds);
        return relationshipIds.size();
    }

    private Set<String> getIds(String tableName) {
        String sql = "SELECT id FROM " + tableName;
        Set<String> ids = new HashSet<>();
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet resultSet = stmt.executeQuery(sql)) {
                while (resultSet.next()) {
                    ids.add(resultSet.getString(ID_INDEX));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    /**
     * clean up a table for a specific type
     */
    public void clean(Set<String> typeLabels) throws SQLException {
        for (String typeLabel : typeLabels) {
            dropTable(getTableName(typeLabel));
        }
    }

    private void dropTable(String tableName) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/");
        try (PreparedStatement stmt = conn.prepareStatement("DROP TABLE IF EXISTS " + this.getTableName(tableName))) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
