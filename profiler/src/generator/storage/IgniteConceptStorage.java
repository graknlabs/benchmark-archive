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

package grakn.benchmark.profiler.generator.storage;

import grakn.benchmark.profiler.generator.DataGeneratorException;
import grakn.core.concept.Concept;
import grakn.core.concept.ConceptId;
import grakn.core.concept.Label;
import grakn.core.concept.thing.Attribute;
import grakn.core.concept.type.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static grakn.core.concept.type.AttributeType.DataType.BOOLEAN;
import static grakn.core.concept.type.AttributeType.DataType.DATE;
import static grakn.core.concept.type.AttributeType.DataType.DOUBLE;
import static grakn.core.concept.type.AttributeType.DataType.FLOAT;
import static grakn.core.concept.type.AttributeType.DataType.LONG;
import static grakn.core.concept.type.AttributeType.DataType.STRING;


/**
 * Stores identifiers for all concepts in a Grakn
 */
public class IgniteConceptStorage implements ConceptStorage {
    private static final Logger LOG = LoggerFactory.getLogger(IgniteConceptStorage.class);

    private HashSet<String> entityTypeLabels;
    private HashSet<String> relationshipTypeLabels;
    private HashSet<String> explicitRelationshipTypeLabels;
    private Map<String, AttributeType.DataType<?>> attributeTypeLabels; // typeLabel, datatype
    private HashMap<String, String> labelToSqlNameMap;

    private Connection conn;
    private final String cachingMethod = "REPLICATED";
    private final int ID_INDEX = 1;
    private final int VALUE_INDEX = 2;

    // store a counter for number of role players because the ignite tables de-duplicate IDs that play multiple roles
    // total is implicit + explicit role players
    private int totalRolePlayers = 0;
    // separately count only roles that are in explicit relationships
    private int totalExplicitRolePlayers = 0;

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

    public IgniteConceptStorage(HashSet<String> entityTypeLabels, HashSet<String> relationshipTypeLabels, Map<String, AttributeType.DataType<?>> attributeTypeLabels) {
        LOG.info("Initialising ignite...");
        // Read schema concepts and create ignite tables
        this.entityTypeLabels = entityTypeLabels;
        this.explicitRelationshipTypeLabels = new HashSet<>(relationshipTypeLabels);
        this.attributeTypeLabels = attributeTypeLabels;

        this.relationshipTypeLabels = relationshipTypeLabels;
        // add @has-[attribute] relationships as possible relationships
        // sanitize the @has-[attribute] to valid SQL strings
        for (String s : this.attributeTypeLabels.keySet()) {
            this.relationshipTypeLabels.add("@has-" + s);
        }


        labelToSqlNameMap = mapLabelToSqlName(entityTypeLabels, relationshipTypeLabels, attributeTypeLabels.keySet());
        cleanTables();
        initializeSqlDriver();
        createTables();
    }


    /**
     * Convert raw type labels to more specific SQL names by appending _entity/_relationship/_attr
     * This avoids clashes between SQL reserved keywords and schema labels
     * Also converts "@" to "__"
     */
    private HashMap<String, String> mapLabelToSqlName(Set<String> entityLabels, Set<String> relationshipLabels, Set<String> attrLabels) {
        HashMap<String, String> labelToSqlName = new HashMap<>();
        for (String entityLabel : entityLabels) {
            labelToSqlName.put(entityLabel, sanitizeString(entityLabel + "_entity"));
        }
        for (String attrLabel : attrLabels) {
            labelToSqlName.put(attrLabel, sanitizeString(attrLabel + "_attr"));
        }
        for (String relationshipLabel : relationshipLabels) {
            labelToSqlName.put(relationshipLabel, sanitizeString(relationshipLabel + "_relationship"));
        }
        return labelToSqlName;
    }

    private void cleanTables() {
        try {
            clean(this.getAllTypeLabels());
            dropTable("roleplayers"); // one special table for tracking role players
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
        }
    }

    private void initializeSqlDriver() {
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
    }

    private void createTables() {
        // Create database tables.
        for (String typeLabel : this.entityTypeLabels) {
            this.createTypeIdsTable(typeLabel, this.relationshipTypeLabels);
        }

        for (String typeLabel : this.relationshipTypeLabels) {
            this.createTypeIdsTable(typeLabel, this.relationshipTypeLabels);
        }

        for (Map.Entry<String, AttributeType.DataType<?>> entry : this.attributeTypeLabels.entrySet()) {
            String typeLabel = entry.getKey();
            AttributeType.DataType<?> datatype = entry.getValue();
            String dbDatatype = DATATYPE_MAPPING.get(datatype);
            this.createAttributeValueTable(typeLabel, dbDatatype, this.relationshipTypeLabels);
        }

        // re-create special table
        // role players that have been assigned into a relationship at some point
        createTable("roleplayers", "VARCHAR", new LinkedList<>());

    }

    private String labelToSqlName(String label) {
        if (!labelToSqlNameMap.containsKey(label)){
            LOG.error("No SQL-safe conversion for label: " + label);
        }
        return labelToSqlNameMap.get(label);
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
     *
     * @param typeLabel
     */
    private void createTypeIdsTable(String typeLabel, Set<String> relationshipLabels) {
        String sqlTypeLabel = labelToSqlName(typeLabel);
        List<String> relationshipColumnNames = relationshipLabels.stream()
                .map(label -> labelToSqlName(label))
                .collect(Collectors.toList());
        createTable(sqlTypeLabel, "VARCHAR", relationshipColumnNames);
    }

    /**
     * Create a table for storing attributeValues for the given type
     * this is a TWO column table of attribute ID and attribute value
     *
     * @param typeLabel
     * @param sqlDatatypeName
     */
    private void createAttributeValueTable(String typeLabel,
                                           String sqlDatatypeName,
                                           Set<String> relationshipLabels) {

        List<String> relationshipColumnNames = relationshipLabels.stream()
                .map(label -> labelToSqlName(label))
                .collect(Collectors.toList());

        String sqlTypeLabel = labelToSqlName(typeLabel);
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE " + sqlTypeLabel + " (" +
                    " id VARCHAR PRIMARY KEY, " +
                    " value " + sqlDatatypeName + ", " +
                    joinColumnLabels(relationshipColumnNames) +
                    " nothing LONG) " +
                    " WITH \"template=" + cachingMethod + "\"");
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
        }
    }

    /**
     * Create a single column table with the value stored being of the given sqlDatatype
     *
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

    /*

    -------- helpers -------

     */

    private String joinColumnLabels(List<String> columnLabels) {
        String joinedColumns = String.join(" VARCHAR, ", columnLabels);
        joinedColumns = joinedColumns.length() > 0 ? joinedColumns + " VARCHAR, " : joinedColumns;
        return joinedColumns;
    }

    private String sanitizeString(String string) {
        return string.replace('-', '_').replace("@", "__");
    }

    @Override
    public void addConcept(Concept concept) {

        Label conceptTypeLabel = concept.asThing().type().label();
        String tableName = labelToSqlName(conceptTypeLabel.toString());
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
     * Add a role player, and specify its type, the relationship, and role it fills
     * This will track
     *
     * @param conceptId
     * @param conceptType
     * @param relationshipType
     * @param role
     */
    public void addRolePlayer(String conceptId, String conceptType, String relationshipType, String role) {

        // sanity check for the user in case they entered something wrong in the data generator
        if (!this.relationshipTypeLabels.contains(relationshipType)) {
            throw new DataGeneratorException(relationshipType + " is not a valid relationship type. This is likely an error in the data generator definition");
        }

        // update in-memory accounting
        totalRolePlayers += 1;
        if (!relationshipType.startsWith("@")) {
            totalExplicitRolePlayers += 1;
        }

        // add the role to this concept ID's row/column for this relationship
        String sqlTypeTable = labelToSqlName(conceptType);
        String sqlRelationship = labelToSqlName(relationshipType);
        String sqlRole = sanitizeString(role);

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

        // add the conceptID to the overall role players table
        try (Statement stmt = conn.createStatement()) {
            String checkExists = "SELECT id FROM roleplayers WHERE id = '" + conceptId + "'";
            try (ResultSet rs = stmt.executeQuery(checkExists)) {
                if (!rs.next()) {
                    String addRolePlayer = "INSERT INTO roleplayers (id, ) VALUES ('" + conceptId + "')";
                    stmt.executeUpdate(addRolePlayer);
                }
            } catch (SQLException e) {
                LOG.trace(e.getMessage(), e);
            }
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
        }
    }

    /**
     * String stuffing all the roles played by a concept of a given type in a specific relationship into one
     */
    public List<ConceptId> getIdsNotPlayingRole(String typeLabel, String relationshipType, String role) {
        String tableName = labelToSqlName(typeLabel);
        String columnName = labelToSqlName(relationshipType);
        String roleName = sanitizeString(role);

        String sql = "SELECT ID, " + columnName + " FROM " + tableName +
                " WHERE (" + columnName + " IS NULL OR " + columnName + "  NOT LIKE '%" + roleName + "%')";

        LinkedList<ConceptId> ids = new LinkedList<>();

        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    ids.addLast(ConceptId.of(rs.getString("id")));
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
        String tableName = labelToSqlName(typeLabel);
        String columnName = labelToSqlName(relationshipType);
        String roleName = sanitizeString(role);

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
        String sql = "SELECT id FROM " + labelToSqlName(typeLabel) +
                " OFFSET " + offset +
                " FETCH FIRST ROW ONLY";
        return sql;
    }

    private String sqlGetAttrValue(String typeLabel, int offset) {
        String sql = "SELECT value FROM " + labelToSqlName(typeLabel) +
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
        String tableName = labelToSqlName(typeLabel);
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
    public int totalExplicitRelationships() {
        int total = 0;
        for (String relationshipType : this.explicitRelationshipTypeLabels) {
            total += getConceptCount(relationshipType);
        }
        return total;
    }

    @Override
    public int totalImplicitRelationships() {
        int totalRelationships = 0;
        for (String relationshipType : this.relationshipTypeLabels) {
            totalRelationships += getConceptCount(relationshipType);
        }
        return totalRelationships - totalExplicitRelationships();
    }

    @Override
    public int totalEntities() {
        int total = 0;
        for (String entityType : this.entityTypeLabels) {
            total += getConceptCount(entityType);
        }
        return total;
    }

    @Override
    public int totalAttributes() {
        int total = 0;
        for (String attributeType : this.attributeTypeLabels.keySet()) {
            total += getConceptCount(attributeType);
        }
        return total;
    }


    /**
     * Return total count of number of role players (repeat counts of the same concept playing multiples roles is
     * counted repeatedly, not once)
     *
     * @return
     */
    @Override
    public int totalRolePlayers() {
        return totalRolePlayers;
    }

    /**
     * Return total count of number of role players (repeat counts of the same concept playing multiples roles is
     * counted repeatedly, not once)
     *
     * @return
     */
    @Override
    public int totalExplicitRolePlayers() {
        return totalExplicitRolePlayers;
    }

    /**
     * Orphan entities = Set(all entities) - Set(entities playing roles)
     *
     * @return
     */
    @Override
    public int totalOrphanEntities() {
        Set<String> rolePlayerIds = getIds("roleplayers");
        Set<String> entityIds = new HashSet<>();
        for (String typeLabel : this.entityTypeLabels) {
            Set<String> ids = getIds(labelToSqlName(typeLabel));
            entityIds.addAll(ids);
        }
        entityIds.removeAll(rolePlayerIds);
        return entityIds.size();
    }

    /**
     * Orphan attributes = Set(all attribute ids) - Set(attributes playing roles)
     *
     * @return
     */
    @Override
    public int totalOrphanAttributes() {
        Set<String> rolePlayerIds = getIds("roleplayers");
        Set<String> attributeIds = new HashSet<>();
        for (String typeLabel : this.attributeTypeLabels.keySet()) {
            Set<String> ids = getIds(labelToSqlName(typeLabel));
            attributeIds.addAll(ids);
        }
        attributeIds.removeAll(rolePlayerIds);
        return attributeIds.size();
    }

    /**
     * Double counting between relationships and relationships also playing roles (including implicit and explicit rels)
     * = Set(All relationship ids) intersect Set(role players)
     *
     * @return
     */
    @Override
    public int totalRelationshipsRolePlayersOverlap() {
        Set<String> rolePlayerIds = getIds("roleplayers");
        Set<String> relationshipIds = new HashSet<>();
        for (String typeLabel : this.relationshipTypeLabels) {
            Set<String> ids = getIds(labelToSqlName(typeLabel));
            relationshipIds.addAll(ids);
        }
        relationshipIds.retainAll(rolePlayerIds);
        return relationshipIds.size();
    }

    @Override
    public int getGraphScale() {
        int entities = totalEntities();
        int attributes = totalAttributes();
        int relationships = totalExplicitRelationships();
        return entities + attributes + relationships;
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
            dropTable(labelToSqlName(typeLabel));
        }
    }

    private void dropTable(String tableName) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/");
        try (PreparedStatement stmt = conn.prepareStatement("DROP TABLE IF EXISTS " + tableName)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOG.trace(e.getMessage(), e);
        }
    }
}
