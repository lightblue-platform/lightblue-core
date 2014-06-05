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
package com.redhat.lightblue.rest.metadata.hystrix;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class GetEntityRolesCommandTest extends AbstractRestCommandTest {
    @Test
    public void execute() {
        GetEntityRolesCommand command = new GetEntityRolesCommand(null, metadata, "test", "1.0.0");

        String output = command.execute();

        Assert.assertNotNull(output);
    }
}
