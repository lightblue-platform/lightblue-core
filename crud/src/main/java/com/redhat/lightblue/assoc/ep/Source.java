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
 * This class is used to refer to a source step. The source step of
 * another step is sometimes given as a definite step, and sometimes
 * as the result step of an execution block. However, during execution
 * plan construction, not all execution blocks have a result step
 * yet. So, this class defers the initialization of source until it is
 * needed
 */
public final class Source<T> {
    private ExecutionBlock sourceBlock;
    private Step<T> sourceStep;

    public Source(ExecutionBlock sourceBlock) {
        this.sourceBlock=sourceBlock;
    }

    public Source(Step<T> step) {
        this.sourceStep=step;
    }

    public Step<T> getStep() {
        if(sourceStep==null)
            sourceStep=(Step<T>)sourceBlock.getResultStep();
        return sourceStep;
    }

    public ExecutionBlock getBlock() {
        if(sourceBlock==null)
            sourceBlock=sourceStep.getBlock();
        return sourceBlock;
    }

    @Override
    public String toString() {
        return "Step:"+getStep()+" block:"+getBlock();
    }    
}
