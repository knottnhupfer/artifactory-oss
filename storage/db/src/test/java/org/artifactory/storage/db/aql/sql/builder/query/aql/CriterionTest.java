package org.artifactory.storage.db.aql.sql.builder.query.aql;

import org.artifactory.aql.model.*;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.assertEquals;

/**
 * @author Rotem Kfir
 */
public class CriterionTest {

    @Test(description = "Not equals query should include 'or x is null' only for nullable fields")
    public void generateNotEqualsQuery() {
        AqlField nonNullVariable = new AqlField(AqlPhysicalFieldEnum.itemRepo);
        AqlValue value = new AqlValue(AqlVariableTypeEnum.string, "my-value");
        String query = Criterion.generateNotEqualsQuery(nonNullVariable, value, new ArrayList<>(), "n.");
        assertEquals(query, " (n.repo != ?)");

        AqlField nullableVariable = new AqlField(AqlPhysicalFieldEnum.itemModifiedBy);
        query = Criterion.generateNotEqualsQuery(nullableVariable, value, new ArrayList<>(), "n.");
        assertEquals(query, " (n.modified_by != ? or n.modified_by is null)");
    }

    @Test
    public void generateLessEqualsQuery() {
        AqlField variable = new AqlField(AqlPhysicalFieldEnum.buildCreated);
        AqlValue value = new AqlValue(AqlVariableTypeEnum.string, "5");
        String query = Criterion.generateLessEqualsQuery(variable, value, new ArrayList<>(), "b.");
        assertEquals(query, " b.created <= ?");
    }
}
