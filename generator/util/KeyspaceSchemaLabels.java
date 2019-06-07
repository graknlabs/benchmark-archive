package grakn.benchmark.generator.util;

import grakn.client.GraknClient;
import grakn.core.concept.type.AttributeType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Container class to store all Types labels of a given keyspace
 */
public class KeyspaceSchemaLabels {

    private final Set<String> entityTypeLabels;
    private final Set<String> relationTypeLabels;
    private final Map<String, AttributeType.DataType<?>> attributeTypeLabelsDataTypes;

    public KeyspaceSchemaLabels(GraknClient client, String keyspace) {
        GraknClient.Session session = client.session(keyspace);
        GraknClient.Transaction tx = session.transaction().read();
        this.entityTypeLabels = getEntityTypes(tx);
        this.relationTypeLabels = getRelationTypes(tx);
        this.attributeTypeLabelsDataTypes = getAttributeTypes(tx);
        session.close();
        tx.close();
    }

    public Set<String> entityLabels() {
        return entityTypeLabels;
    }

    public Set<String> relationLabels() {
        return relationTypeLabels;
    }

    public Map<String, AttributeType.DataType<?>> attributeLabelsDataTypes() {
        return attributeTypeLabelsDataTypes;
    }

    private Set<String> getEntityTypes(GraknClient.Transaction tx) {
        return tx.getEntityType("entity")
                .subs()
                .map(type -> type.label().getValue())
                .filter(label -> !label.equals("entity"))
                .collect(Collectors.toSet());
    }

    private Set<String> getRelationTypes(GraknClient.Transaction tx) {
        return tx.getRelationType("relation")
                .subs()
                .filter(type -> !type.isImplicit())
                .map(type -> type.label().getValue())
                .filter(label -> !label.equals("relation"))
                .collect(Collectors.toSet());
    }

    private Map<String, AttributeType.DataType<?>> getAttributeTypes(GraknClient.Transaction tx) {
        HashMap<String, AttributeType.DataType<?>> typeLabels = new HashMap<>();
        HashSet<AttributeType<Object>> types = tx.getAttributeType("attribute")
                .subs()
                .filter(type -> !type.label().getValue().equals("attribute"))
                .collect(Collectors.toCollection(HashSet::new));
        for (AttributeType conceptType : types) {
            String label = conceptType.label().toString();
            AttributeType.DataType<?> datatype = conceptType.dataType();
            typeLabels.put(label, datatype);
        }
        return typeLabels;
    }
}
