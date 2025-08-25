package org.ject.support.jacoco;

public class JacocoEx {

    public String helloWorld(String input) {
        if (input.equals("hello")) {
            return "world";
        }
        return "bye";
    }

    public int onePlusOne() {
        return 1 + 1;
    }

    public boolean isOne(int input) {
        return input == 1;
    }
}
