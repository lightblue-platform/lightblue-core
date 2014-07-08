package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.metadata.parser.PropertyParser;

import java.util.ArrayList;
import java.util.List;

public class RDBMSPropertyParserImpl<T> extends PropertyParser<T> {
    @Override
    public Object parse(String name, MetadataParser<T> p, T node) {
        if (!"rdbms".equals(name)) {
            throw com.redhat.lightblue.util.Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
        }
        RDBMSConfiguration rdbmsConfiguration = new RDBMSConfiguration();
        rdbmsConfiguration.setSelect(parseStatement(p, p.getObjectProperty(node, "select")));
        rdbmsConfiguration.setSelect(parseStatement(p, p.getObjectProperty(node, "insert")));
        rdbmsConfiguration.setSelect(parseStatement(p, p.getObjectProperty(node, "update")));
        rdbmsConfiguration.setSelect(parseStatement(p, p.getObjectProperty(node, "delete")));

        return rdbmsConfiguration;
    }

    @Override
    public void convert(MetadataParser<T> p, T parent, Object object) {
        p.putObject(parent,"rdbms", convertRDBMS(p, (RDBMSConfiguration) object));
    }

    private Object convertRDBMS(MetadataParser<T> p, RDBMSConfiguration object) {
        T rdbms = p.newNode();
        p.putObject(rdbms, "select", convertStatement(p, object.getSelect()));
        p.putObject(rdbms, "insert", convertStatement(p, object.getInsert()));
        p.putObject(rdbms, "update", convertStatement(p, object.getUpdate()));
        p.putObject(rdbms, "delete", convertStatement(p, object.getDelete()));
        return rdbms;
    }

    private Statement parseStatement(MetadataParser<T> p, T statement) {
        List<T> statements = p.getObjectList(statement, "statements");
        List<SQLOrConditional> sQLOrConditional = transformToSQLOrConditional(p,statements);
        T b = p.getObjectProperty(statement, "bindings");
        Bindings bindings = transformToBindings(p, b);

        final Statement s = new Statement();
        s.setsQLOrConditionalList(sQLOrConditional);
        s.setBindings(bindings);

        return s;
    }

    private Bindings transformToBindings(MetadataParser<T> p,T bindings) {
        final Bindings b = new Bindings();
        List<T> inRaw = p.getObjectList(bindings, "in");
        List<T> outRaw = p.getObjectList(bindings, "out");

        List<InOut> inList = transformToInOut(p, inRaw);
        List<InOut> outList = transformToInOut(p, inRaw);

        b.setInList(inList);
        b.setOutList(outList);

        return b;
    }

    private List<InOut> transformToInOut(MetadataParser<T> p, List<T> inRaw) {
        final ArrayList<InOut> result = new ArrayList<InOut>();
        for (int i = 0; i < inRaw.size(); i++) {
            T t = inRaw.get(i);

            InOut a = new InOut();
            a.setAccumulative(Boolean.TRUE.toString().equalsIgnoreCase(p.getStringProperty(t, "isaccumulative"))? Boolean.TRUE : Boolean.FALSE);
            a.setArray(Boolean.TRUE.toString().equalsIgnoreCase(p.getStringProperty(t,"isarray"))? Boolean.TRUE : Boolean.FALSE);
            a.setColumnName(p.getStringProperty(t,"columnName"));
            a.setDocumentPath(p.getStringProperty(t,"documentPath"));
            a.setVariableName(p.getStringProperty(t,"variableName"));

            result.add(a);
        }
        return result;
    }

    private List<SQLOrConditional> transformToSQLOrConditional(MetadataParser<T> p, List<T> statements) {
        final ArrayList<SQLOrConditional> result = new ArrayList<SQLOrConditional>();
        for (T statement : statements) {
            SQLOrConditional e = new SQLOrConditional();
            final String sql = p.getStringProperty(statement, "sql");
            if(sql != null) {
                DynamicConditional c = new DynamicConditional();
                /*
                maybe change the
                 /home/lcestari/repositories/lb/lightblue/lightblue-rdbms/metadata/src/main/resources/json-schema/metadata/rdbms/metadata/if.json
                  to
                 /home/lcestari/repositories/lb/lightblue/lightblue-core/query-api/src/main/resources/json-schema/query/conditional.json
                 so the rdbms would look like the query api
                 */
                final List<T> ifArr = p.getObjectList(statement, "if");
                final List<T> thenArr = p.getObjectList(statement, "then");
                final String thenStr = p.getStringProperty(statement, "then");
                final List<T> elseifStr = p.getObjectList(statement, "elseif");
                final List<T> elseArr = p.getObjectList(statement, "then");
                final String elseStr = p.getStringProperty(statement, "else");

                if(ifArr != null && ! ifArr.isEmpty()){
                    for (T t : ifArr) {
                        final String logicalOperator = p.getStringProperty(t, "logicalOperators");
                        final String conditional = p.getStringProperty(t, "conditional");
                        final String variable1 = p.getStringProperty(t, "variable1");
                        final String variable2 = p.getStringProperty(t, "variable2");

                        If i = new If();

                        if( LogicalOperators.getValues().contains(logicalOperator)){
                            i.setLogicalOperatorNext(logicalOperator);
                        } else {
                            throw com.redhat.lightblue.util.Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, logicalOperator);
                        }

                        if( Conditional.getValues().contains(conditional)){
                            i.setConditional(conditional);
                        } else {
                            throw com.redhat.lightblue.util.Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, conditional);
                        }

                        if(!(variable1 == null || variable1.isEmpty())){
                            i.setVariable1(variable1);
                        } else {
                            throw com.redhat.lightblue.util.Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, variable1);
                        }

                        if(!(variable2 == null || variable2.isEmpty())){
                            i.setVariable2(variable2);
                        } else {
                            if(!"isempty".equalsIgnoreCase(logicalOperator)){
                                throw com.redhat.lightblue.util.Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, variable2);
                            }
                        }
                        c.getIfs().add(i);
                    }

                    if(thenStr == null){

                    }

                    if(elseifStr != null){

                    }
                    if(elseStr != null){

                    }

                } else {
                    throw com.redhat.lightblue.util.Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, ifArr.toString());
                }

                //TODO 'if then elseif else' fields
                if(1 == 2 && 1== 3 || 1==3 || 1==1 ){

                }
                e.setDynamicConditional(c);
            } else {
                SQL s = new SQL();
                s.setSQL(sql);
                s.setIterateOverRows(Boolean.TRUE.toString().equalsIgnoreCase(p.getStringProperty(statement,"iterateOverRows"))? Boolean.TRUE : Boolean.FALSE);
                final String type = p.getStringProperty(statement, "type");
                if(! Types.getValues().contains(type)) {
                    throw com.redhat.lightblue.util.Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, type);
                }
                s.setType(type);
                s.setDatasource(p.getStringProperty(statement, "datasource"));
                e.setSQL(s);
            }

            result.add(e);
        }
        return result;
    }





    private Object convertStatement(MetadataParser<T> p, Statement update) {
        return null;
    }
}
