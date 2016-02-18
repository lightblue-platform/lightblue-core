/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.assoc.ep;


/**
 * Given n source steps, returns n-tuples containing the documents from those steps
 *
 * Input: List of ResultDocument
 * Output: [ ResultDocument ] (tuples)
 */
public class Join implements Step<List<ResultDocument>> {

    private static final Logger LOGGER=LoggerFactory.getLogger(Join.class);
    
    private final Step<ResultDocument>[] sources;
    private Map<Step<ResultDocument>,RewindableStream<ResultDocument>> sourceResults;
    
    public Join(Step<ResultDocument>[] sources) {
        this.sources=sources;
    }

    public Step<ResultDocument>[] getSources() {
        return sources;
    }
    
    @Override
    public ResultStream<List<ResultDocument>> getResults(ExecutionContext ctx) {
        if(sourceResults==null) {
            // get all document streams from result steps
            Future<RewindableStream<ResultDocument>> [] futureResults=new Future<>[sources.length];
            int i=0;
            for(Step<ResultDocument> source:sources) {
                futureResults[i++]=ctx.getExecutor().submit(new ResultGetter(source,ctx));
            }
            sourceResults=new HashMap();
            int i=0;
            for(Future<RewindableStream<ResultDocument>> futureResult:futureResults) {
                sourceResults.put(sources[i++],futureResult.get());
            }
        }
        return new JoinStream();
    }

    
    /**
     * Asnycnronus Callable that gets result streams from source steps
     */
    private static class ResultGetter implements Callable<ResultStream<ResultDocument>> {
        private final Step<ResultDocument> source;
        private final ExecutionContext ctx;
        
        public ResultGetter(Step<ResultDocument> source,ExecutionContext ctx) {
            this.source=source;
            this.ctx=ctx;
        }

        @Override
        public RewindableStream<ResultDocument> call() {
            return new RewindableStream(source.getResults(ctx));
        }
    }

    
    private class JoinStream implements ResultStream<List<ResultDocument>> {
        private final Tuples<ResultDocument> tuples;
        private Iterator<List> itr;

        public JoinStream() {
            tuples=new Tuples();
            for(Step<ResultDocument> step:sources)
                tuples.add(new IterableAdapter(sourceResults.get(step)));
            itr=tuples.iterator();
        }

        @Override
        public List<ResultDocument> next() {
            if(itr.hasNext())
                return itr.next();
            else
                return null;
        }
    }
}

