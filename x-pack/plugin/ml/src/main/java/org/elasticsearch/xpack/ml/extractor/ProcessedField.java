/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.extractor;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.xpack.core.ml.inference.preprocessing.PreProcessor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class ProcessedField {
    private final PreProcessor preProcessor;

    public ProcessedField(PreProcessor processor) {
        this.preProcessor = Objects.requireNonNull(processor);
    }

    public List<String> getInputFieldNames() {
        return preProcessor.inputFields();
    }

    public List<String> getOutputFieldNames() {
        return preProcessor.outputFields();
    }

    public Set<String> getOutputFieldType(String outputField) {
        return Collections.singleton(preProcessor.getOutputFieldType(outputField));
    }

    public Object[] value(SearchHit hit, Function<String, ExtractedField> fieldExtractor) {
        Map<String, Object> inputs = new HashMap<>(preProcessor.inputFields().size(), 1.0f);
        for (String field : preProcessor.inputFields()) {
            ExtractedField extractedField = fieldExtractor.apply(field);
            if (extractedField == null) {
                return new Object[0];
            }
            Object[] values = extractedField.value(hit);
            if (values == null || values.length == 0) {
                continue;
            }
            final Object value = values[0];
            if (values.length == 1 && (value instanceof String || value instanceof Number)) {
                inputs.put(field, value);
            }
        }
        preProcessor.process(inputs);
        return preProcessor.outputFields().stream().map(inputs::get).toArray();
    }

    public String getProcessorName() {
        return preProcessor.getName();
    }

}
