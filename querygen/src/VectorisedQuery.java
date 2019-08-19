package grakn.benchmark.querygen;

import graql.lang.query.GraqlGet;

public class VectorisedQuery {

    private GraqlGet graqlQuery;

    private double numVariables;
    private double meanRolesPerRelation;
    private double meanUniqueRolesPerRelation;
    private double meanAttributesOwnedPerThing;
    private double ambiguity;
    private double specificity;
    private double meanEdgesPerVariable;
    private double comparisonsPerAttribute;


    public VectorisedQuery(GraqlGet graqlQuery, Vectoriser queryVectoriser) {
        this.graqlQuery = graqlQuery;

        numVariables = queryVectoriser.numVariables();
        meanRolesPerRelation = queryVectoriser.meanRolesPerRelation();
        meanUniqueRolesPerRelation = queryVectoriser.meanUniqueRolesPerRelation();
        meanAttributesOwnedPerThing = queryVectoriser.meanAttributesOwnedPerThing();
        ambiguity = queryVectoriser.ambiguity();
        specificity = queryVectoriser.specificity();
        meanEdgesPerVariable = queryVectoriser.meanEdgesPerVariable();
        comparisonsPerAttribute = queryVectoriser.comparisonsPerAttribute();
    }
}
