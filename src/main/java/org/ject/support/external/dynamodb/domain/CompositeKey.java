package org.ject.support.external.dynamodb.domain;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Deprecated
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class CompositeKey implements Comparable<CompositeKey>, Serializable {
    public static final String DELIMITER = "#";

    private String prefix;
    private String postfix;

    @Override
    public String toString() {
        return prefix + DELIMITER + postfix;
    }

    @Override
    public int compareTo(CompositeKey compositeKey) {
        return this.toString().compareTo(compositeKey.toString());
    }
}
