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
package com.redhat.lightblue.extensions.synch;

import com.redhat.lightblue.extensions.Extension;

/**
 * This is the locking extension. It is a factory interface that
 * prodives locking implementation based on a given domain.
 *
 * Different locking implementations can be provided by different
 * backends. A single backend can also provide multiple locking
 * implementations. The domain is used to distinguish between
 * different locking implenentatios. For instance, for MongoDB
 * backend, domain is associated with a MongoDB datasource, and
 * locking information is kept in that datasource. Dufferent domains
 * select different datasources,
 */
public interface LockingSupport extends Extension {
    /**
     * Return all recognized domains
     */
    String[] getLockingDomains();

    /**
     * Return a locking implementation for the specific domain
     */
    Locking getLockingInstance(String domain);
}
