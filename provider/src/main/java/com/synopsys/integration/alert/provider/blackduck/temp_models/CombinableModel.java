/**
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.provider.blackduck.temp_models;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.ListUtils;

public interface CombinableModel<T> {
    List<T> combine(T otherModel);

    static <T extends CombinableModel<T>> List<T> combine(List<T> lhs, List<T> rhs) {
        List<T> unified = ListUtils.union(lhs, rhs);
        return combine(unified);
    }

    static <T extends CombinableModel<T>> List<T> combine(List<T> models) {
        LinkedList<T> copyOfModels = new LinkedList<>(models);
        int i = 0;
        int j;
        while (i < copyOfModels.size()) {
            T e1 = copyOfModels.get(i);
            j = i + 1;
            while (j < copyOfModels.size()) {
                T e2 = copyOfModels.get(j);
                List<T> combinedElements = e1.combine(e2);
                int combinedSize = combinedElements.size();
                if (combinedSize == 0) {
                    copyOfModels.remove(e1);
                    copyOfModels.remove(e2);
                    break;
                } else if (combinedSize == 1) {
                    copyOfModels.remove(e1);
                    copyOfModels.remove(e2);
                    copyOfModels.add(i, combinedElements.get(0));
                    break;
                } else {
                    j++;
                }
            }

            if (j >= copyOfModels.size()) {
                i++;
            }
        }
        return copyOfModels;
    }

}
