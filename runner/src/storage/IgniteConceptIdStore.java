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

import grakn.core.concept.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

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
    private static final Logger LOG = LoggerFactory.getLogger(IgniteConceptIdStore.class);

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
            LOG.trace(e.getMessage(), e);
        }

        // Register JDBC driver.
        try {
            Class.forName("org.apache.ignite.IgniteJdbcThinDriver");
        } catch (ClassNotFoundException e) {
            LOG.trace(e.getMessage(), e);
        }

        // Open JDBC connection.
        try {
            this.conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/");
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
        }

        // Create database tables.
        for (String typeLabel : this.entityTypeLabels) {
            this.createTypeIdsTable(typeLabel, relationshipTypes);
        }

        for (String typeLabel : this.relationshipTypeLabels) {
            this.createTypeIdsTable(typeLabel, relationshipTypes);
        }

        for (Map.Entry<String, AttributeType.DataType<?>> entry : this.attributeTypeLabels.entrySet()) {
            String typeLabel = entry.getKey();
            AttributeType.DataType<?> datatype = entry.getValue();
            String dbDatatype = DATATYPE_MAPPING.get(datatype);
            this.createAttributeValueTable(typeLabel, dbDatatype, relationshipTypes);
        }

        // re-create special table
        // role players that have been assigned into a relationship at some point
        createTable("roleplayers", "VARCHAR", new LinkedList<>());
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
    private void createTypeIdsTable(String typeLabel, Set<RelationshipType> relationshipTypes) {
        String tableName = this.putTableName(typeLabel);
        List<String> relationshipColumnNames = relationshipTypes.stream()
                .map(relType -> convertTypeLabelToSqlName(relType.label().toString()))
                .collect(Collectors.toList());
        createTable(tableName, "VARCHAR", relationshipColumnNames);
    }

    /**
     * Create a table for storing attributeValues for the given type
     * this is a TWO column table of attribute ID and attribute value
     * @param typeLabel
     * @param sqlDatatypeName
     */
    private void createAttributeValueTable(String typeLabel, String sqlDatatypeName, Set<RelationshipType> relationshipTypes) {

        List<String> furtherColumns = relationshipTypes.stream()
                .map(relType -> convertTypeLabelToSqlName(relType.label().toString()))
                .collect(Collectors.toList());
        String tableName = convertTypeLabelToSqlName(typeLabel);
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE " + tableName + " (" +
                    " id VARCHAR PRIMARY KEY, " +
                    " value " + sqlDatatypeName + ", " +
                    joinColumnLabels(furtherColumns) +
                    " nothing LONG) " +
                    " WITH \"template=" + cachingMethod + "\"");
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
        }
    }

    /**
     * Create a single column table with the value stored being of the given sqlDatatype
     * @param tableName
     * @param sqlDatatypeName
     */
    private void createTable(String tableName, String sqlDatatypeName, List<String> furtherColumns) {

        try (Statement stmt = conn.createStatement()) {

            String sqlStatement = "CREATE TABLE " + tableName + " (" +
                    " id " + sqlDatatypeName + " PRIMARY KEY, " +
                    joinColumnLabels(furtherColumns) +
                    " nothing LONG) " +
                    " WITH \"template=" + cachingMethod + "\"";

            stmt.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
        }
    }

    private String joinColumnLabels(List<String> columnLabels) {
        String joinedColumns = String.join(" VARCHAR, ", columnLabels);
        joinedColumns = joinedColumns.length() > 0 ? joinedColumns + " VARCHAR, " : joinedColumns;
        return joinedColumns;
    }

    private String convertTypeLabelToSqlName(String typeLabel) {
        return typeLabel.replace('-', '_');
//                .replaceAll("[0-9]", "");
    }

    private String putTableName(String typeLabel) {
        String tableName = this.convertTypeLabelToSqlName(typeLabel);
        this.typeLabelsTotableNames.put(typeLabel, tableName);
        return tableName;
    }

    private String getTableName(String typeLabel) {

        String tableName = this.typeLabelsTotableNames.get(typeLabel);
        if (tableName != null) {
            return tableName;
        } else {
            // TODO Don't need this else clause if I can figure out how to drop all tables in clean()
            return convertTypeLabelToSqlName(typeLabel);
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

            // check if this ID is already in the table suffices
            try (Statement stmt = this.conn.createStatement()) {
                String checkExists = "SELECT id FROM " + tableName + " WHERE id = '" + conceptId + "'";
                try (ResultSet rs = stmt.executeQuery(checkExists)) {
                    // skip insertion if this query has any results
                    if (rs.next()) {
                        return;
                    }
                } catch (SQLException e) {
                    LOG.trace(e.getMessage(), e);
                }
            } catch (SQLException e) {
                LOG.trace(e.getMessage(), e);
            }

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
                    LOG.trace(e.getMessage(), e);
                }
            }

        } else {


            try (PreparedStatement stmt = this.conn.prepareStatement(
                    "INSERT INTO " + tableName + " (id, ) VALUES (?, )")) {
                stmt.setString(ID_INDEX, conceptId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                if (!e.getSQLState().equals("23000")) {
                    LOG.trace(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Add a role player without specifying the specific relationship & role it fills
     * @param conceptId
     */
    public void addRolePlayer(String conceptId) {
        // add the conceptID to the overall role players table
        try (Statement stmt = conn.createStatement()) {
            String checkExists = "SELECT id FROM roleplayers WHERE id = '" + conceptId + "'";
            try (ResultSet rs = stmt.executeQuery(checkExists)) {
                if (rs.next()) {
                    // if we have any rows matching this ID, skip
                    return;
                }
            } catch (SQLException e) {
                LOG.trace(e.getMessage(), e);
            }
            String addRolePlayer = "INSERT INTO roleplayers (id, ) VALUES ('"+conceptId+"')";
            stmt.executeUpdate(addRolePlayer);
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
        }
    }

    /**
     * Add a role player, and specify its type, the relationship, and role it fills
     * This will track
     * @param conceptId
     * @param conceptType
     * @param relationshipType
     * @param role
     */
    public void addRolePlayer(String conceptId, String conceptType, String relationshipType, String role) {
        addRolePlayer(conceptId);
        addRolePlayerForConcept(conceptId, conceptType, relationshipType, role);
    }

    private void addRolePlayerForConcept(String conceptId, String type, String relationshipType, String role) {
        // add the role to this concept ID's row/column for this relationship
        String sqlTypeTable = convertTypeLabelToSqlName(type);
        String sqlRelationship = convertTypeLabelToSqlName(relationshipType);
        String sqlRole = convertTypeLabelToSqlName(role);

        try (Statement stmt = conn.createStatement()) {
            // do a select to retrieve currently filled roles by this conceptID in this relationship
            String retrieveCurrentRolesSql = "SELECT " + sqlRelationship + " from " + sqlTypeTable +
                    " where id = '" + conceptId + "'";
            String currentRoles = "";
            try (ResultSet rs = stmt.executeQuery(retrieveCurrentRolesSql)) {
                rs.next(); // move to cursor first result
                currentRoles = rs.getString(sqlRelationship);
                currentRoles = currentRoles == null ? "" : currentRoles;
            } catch (SQLException e) {
                LOG.trace(e.getMessage(), e);
            }

            if (!currentRoles.contains(sqlRole)) {
                currentRoles = currentRoles + "," + sqlRole;
                String setCurrentRolesSql = "UPDATE " + sqlTypeTable +
                        " SET " + sqlRelationship + " = '" + currentRoles + "'" +
                        " WHERE id = '" + conceptId + "'";
                stmt.executeUpdate(setCurrentRolesSql);
            }
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
        }
    }

    /**
     * String hacks to stuff all the roles played by a concept of a given type in a specific relationship into one string
     */
    public List<String> getIdsNotPlayingRole(String typeLabel, String relationshipType, String role) {
        String tableName = convertTypeLabelToSqlName(typeLabel);
        String columnName = convertTypeLabelToSqlName(relationshipType);
        String roleName = convertTypeLabelToSqlName(role);

        String sql = "SELECT ID, " + columnName + " FROM " + tableName +
                " WHERE (" + columnName + " IS NULL OR " + columnName + "  NOT LIKE '%" + roleName + "%')";

        LinkedList<String> ids = new LinkedList<>();

        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    ids.addLast(rs.getString("id"));
                }
            } catch (SQLException e) {
                LOG.trace(e.getMessage(), e);
            }
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
        }
        return ids;
    }

    public Integer numIdsNotPlayingRole(String typeLabel, String relationshipType, String role) {
        String tableName = convertTypeLabelToSqlName(typeLabel);
        String columnName = convertTypeLabelToSqlName(relationshipType);
        String roleName = convertTypeLabelToSqlName(role);

        String sql = "SELECT COUNT(ID) FROM " + tableName +
                " WHERE (" + columnName + " IS NULL OR " + columnName + "  NOT LIKE '%" + roleName + "%')";

        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                rs.next(); // move to first result
                return rs.getInt(1); // apparently column indices start at 1
            } catch (SQLException e) {
                LOG.trace(e.getMessage(), e);
            }
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
        }
        return 0;
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
                LOG.trace(e.getMessage(), e);
            }
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
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
                LOG.trace(e.getMessage(), e);
            }
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
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
                LOG.trace(e.getMessage(), e);
            }
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
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
                LOG.trace(e.getMessage(), e);
            }
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
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
                LOG.trace(e.getMessage(), e);
            }
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
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
                LOG.trace(e.getMessage(), e);
            }
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
        }
        return null;
    }

    public int getConceptCount(String typeLabel) {
        String tableName = getTableName(typeLabel);
        return getCountInTable(tableName);
    }


    private int getCountInTable(String tableName) {
        String sql = "SELECT COUNT(1) FROM " + tableName;

        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {

                if (rs != null && rs.next()) { // Need to do this to increment one line in the ResultSet
                    return rs.getInt(1);
                } else {
                    return 0;
                }

            } catch (SQLException e) {
                LOG.trace(e.getMessage(), e);
            }
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int totalRelationships() {
        int total = 0;
        for (String relationshipType : this.relationshipTypeLabels) {
            total += getConceptCount(relationshipType);
        }
        return total;
    }

    @Override
    public int totalRolePlayers() {
        return getCountInTable("roleplayers");
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
                LOG.trace(e.getMessage(), e);
            }
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
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
            LOG.trace(e.getMessage(), e);
        }
    }
}
