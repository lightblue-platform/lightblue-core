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
        rdbmsConfiguration.setDelete(parseOperation(p, p.getObjectProperty(node, "delete")));
        rdbmsConfiguration.setFetch(parseOperation(p, p.getObjectProperty(node, "fetch")));
        rdbmsConfiguration.setInsert(parseOperation(p, p.getObjectProperty(node, "insert")));
        rdbmsConfiguration.setSave(parseOperation(p, p.getObjectProperty(node, "save")));
        rdbmsConfiguration.setUpdate(parseOperation(p, p.getObjectProperty(node, "update")));

        return rdbmsConfiguration;
    }

    @Override
    public void convert(MetadataParser<T> p, T parent, Object object) {
        p.putObject(parent,"rdbms", convertRDBMS(p, (RDBMSConfiguration) object));
    }

    private Object convertRDBMS(MetadataParser<T> p, RDBMSConfiguration object) {
        T rdbms = p.newNode();
        p.putObject(rdbms, "delete", convertOperation(p, object.getDelete()));
        p.putObject(rdbms, "fetch", convertOperation(p, object.getFetch()));
        p.putObject(rdbms, "insert", convertOperation(p, object.getInsert()));
        p.putObject(rdbms, "save", convertOperation(p, object.getSave()));
        p.putObject(rdbms, "update", convertOperation(p, object.getUpdate()));
        return rdbms;
    }

    private Operation parseOperation(MetadataParser<T> p, T operation) {
        T b = p.getObjectProperty(operation, "bindings");
        Bindings bindings = transformToBindings(p, b);

        List<T> expressionsT = p.getObjectList(operation, "expressions");
        List<Expression> expressions = parseExpressions(p, expressionsT);

        final Operation s = new Operation();
        s.setBindings(bindings);
        s.setExpressionList(expressions);

        return s;
    }

    private Bindings transformToBindings(MetadataParser<T> p,T bindings) {
        final Bindings b = new Bindings();
        List<T> inRaw = p.getObjectList(bindings, "in");
        List<T> outRaw = p.getObjectList(bindings, "out");

        List<InOut> inList = transformToInOut(p, inRaw);
        List<InOut> outList = transformToInOut(p, outRaw);

        b.setInList(inList);
        b.setOutList(outList);

        return b;
    }

    private List<InOut> transformToInOut(MetadataParser<T> p, List<T> inRaw) {
        final ArrayList<InOut> result = new ArrayList<>();
        for (T t : inRaw) {
            InOut a = new InOut();
            a.setColumn(p.getStringProperty(t, "column"));
            a.setPath(p.getStringProperty(t, "path"));

            result.add(a);
        }
        return result;
    }

    private List<Expression> parseExpressions(MetadataParser<T> p, List<T> expressionsT) {
        final ArrayList<Expression> result = new ArrayList<>();
        for (T expression : expressionsT) {
            Expression e;
            String sql = p.getStringProperty(expression, "sql");
            T forS = p.getObjectProperty(expression, "$for");
            T foreachS = p.getObjectProperty(expression, "$foreach");
            T ifthen = p.getObjectProperty(expression, "$if");

            if(sql != null) {
                String datasource = p.getStringProperty(expression, "datasource");
                String type = p.getStringProperty(expression, "type");

                Statement statement = new Statement();
                statement.setSQL(sql);
                statement.setDatasource(datasource);
                statement.setType(type);

                e = statement;
            } else if(forS != null){
                String loopTimesS = p.getStringProperty(forS, "loopTimes");
                int loopTimes =  Integer.parseInt(loopTimesS);
                String loopCounterVariableName = p.getStringProperty(forS, "loopCounterVariableName");
                List<T> expressionsTforS = p.getObjectList(forS, "expressions");
                List<Expression> expressions = parseExpressions(p, expressionsTforS);

                For forLoop = new For();
                forLoop.setLoopTimes(loopTimes);
                forLoop.setLoopCounterVariableName(loopCounterVariableName);
                forLoop.setExpressions(expressions);

                e = forLoop;
            } else if(foreachS != null){
                String iterateOverPath = p.getStringProperty(foreachS, "iterateOverPath");
                List<T> expressionsTforS = p.getObjectList(foreachS, "expressions");
                List<Expression> expressions = parseExpressions(p, expressionsTforS);

                ForEach forLoop = new ForEach();
                forLoop.setIterateOverPath(iterateOverPath);
                forLoop.setExpressions(expressions);

                e = forLoop;
            } else if(ifthen != null) {
                //$if
                If If = parseIf(p, ifthen);

                //$then
                Then Then = parseThen(p,expression);

                //$elseIf
                List<ElseIf> elseIfList= parseElseIf(p,expression);

                //$else
                Else elseC = parseElse(p,expression);

                Conditional c  = new Conditional();
                c.setIf(If);
                c.setThen(Then);
                c.setElseIfList(elseIfList);
                c.setElse(elseC);

                e = c;
            } else {
                throw com.redhat.lightblue.util.Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, "No valid field was found");
            }
            result.add(e);
        }
        return result;
    }

    private List<ElseIf> parseElseIf(MetadataParser<T> p, T expression) {
        List<T> elseIfs = p.getObjectList(expression, "$elseIf");
        List<ElseIf> elseIfList = new ArrayList<>();
        for(T ei : elseIfs){
            T eiIfT = p.getObjectProperty(ei, "$if");
            If eiIf = parseIf(p, eiIfT);

            T eiThenT = p.getObjectProperty(ei, "$then");
            Then eiThen = parseThen(p,eiThenT);

            ElseIf elseIf = new ElseIf();
            elseIf.setIf(eiIf);
            elseIf.setThen(eiThen);
            elseIfList.add(elseIf);
        }
        return elseIfList;
    }

    private If parseIf(MetadataParser<T> p, T ifT) {
        If x = null;
        List<T> orArray = p.getObjectList(ifT, "$or");
        if (orArray != null) {
             x = parseIfs(p, orArray, new IfOr());
        } else {
            List<T> anyArray = p.getObjectList(ifT, "$any");
            if (anyArray != null) {
                 x = parseIfs(p, anyArray, new IfOr());
            } else {
                List<T> andArray = p.getObjectList(ifT, "$and");
                if (andArray != null) {
                     x = parseIfs(p, andArray, new IfAnd());
                } else {
                    List<T> allArray = p.getObjectList(ifT, "$all");
                    if (allArray != null) {
                         x = parseIfs(p, allArray, new IfAnd());
                    } else {
                        T notIfT = p.getObjectProperty(ifT, "$not");
                        if (notIfT != null) {
                            If y = parseIf(p, notIfT);
                            x = new IfNot();
                            x.getConditions().add(y);
                        } else {
                            T pathEmpty = p.getObjectProperty(ifT, "$path-empty");
                            if (pathEmpty != null) {
                                x = new IfPathEmpty();
                            } else {
                                T pathpath = p.getObjectProperty(ifT, "$path-check-path");
                                if (pathpath != null) {
                                    x = new IfPathPath();
                                } else {
                                    T pathvalue = p.getObjectProperty(ifT, "$path-check-value");
                                    if (pathvalue != null) {
                                        x = new IfPathValue();
                                    } else {
                                        T pathvalues = p.getObjectProperty(ifT, "$path-check-values");
                                        if (pathvalues != null) {
                                            x = new IfPathValues();
                                        } else {
                                            T pathregex = p.getObjectProperty(ifT, "$path-regex");
                                            if (pathregex != null) {
                                                x = new IfPathRegex();
                                            } else {
                                                throw com.redhat.lightblue.util.Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, "No valid field was found on if");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    return x;
    }

    private <Z extends If> Z parseIfs(MetadataParser<T> p, List<T> orArray, Z ifC) {
        List<If> l = new ArrayList<>();
        for (T t : orArray){
            If eiIf = parseIf(p, t);
            l.add(eiIf);
        }
        ifC.setConditions(l);
        return ifC;
    }

    private Then parseThenOrElse(MetadataParser<T> p, T t, String name, Then then) {
        String loopOperator = p.getStringProperty(t, name);
        if(loopOperator != null) {
            then.setLoopOperator(loopOperator);
        } else {
            List<T> expressionsT= p.getObjectList(t, name);
            List<Expression> expressions = parseExpressions(p, expressionsT);
            then.setExpressions(expressions);
        }

        return then;
    }

    private Then parseThen(MetadataParser<T> p, T parentThenT) {
        return parseThenOrElse(p, parentThenT, "$then", new Else());
    }

    private Else parseElse(MetadataParser<T> p, T parentElseT) {
        return (Else) parseThenOrElse(p, parentElseT, "$else", new Else());
    }



    private Object convertOperation(MetadataParser<T> p, Operation o) {
        T oT = p.newNode();
        p.putObject(oT, "bindings", convertBindings(p, o.getBindings()));
        Object expressions = p.newArrayField(oT, "expressions");
        convertExpressions(p, o.getExpressionList(), expressions);
        return oT;
    }

    private Object convertBindings(MetadataParser<T> p, Bindings bindings) {
        T bT = p.newNode();
        Object arri = p.newArrayField(bT, "in");
        for (InOut x : bindings.getInList()) {
            p.addObjectToArray(arri, convertInOut(p,x));
        }
        Object arro = p.newArrayField(bT, "out");
        for (InOut x : bindings.getOutList()) {
            p.addObjectToArray(arro, convertInOut(p,x));
        }
        return bT;
    }

    private Object convertInOut(MetadataParser<T> p, InOut x) {
        T ioT = p.newNode();

        p.putString(ioT,"column",x.getColumn());
        p.putString(ioT,"path",x.getPath());

        return ioT;
    }

    private void convertExpressions(MetadataParser<T> p, List<Expression> expressionList, Object expressions) {
        for (Expression expression : expressionList) {

            //p.putObject();
        }
    }
}
