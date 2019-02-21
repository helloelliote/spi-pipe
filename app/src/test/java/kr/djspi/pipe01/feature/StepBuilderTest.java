package kr.djspi.pipe01.feature;

import org.junit.Test;

/**
 * http://rdafbn.blogspot.com/2012/07/step-builder-pattern_28.html
 */
public class StepBuilderTest {

    public StepBuilderTest() {

    }

    public static FirstStep newBuilder() {
        return new Steps();
    }

    public interface FirstStep {
        SecondStep setFirst();
    }

    public interface SecondStep {
        BuildStep setSecond();
    }

    public interface BuildStep {
        BuildTarget build();
    }

    private static class Steps implements FirstStep, SecondStep {

        public SecondStep setFirst() {
            return this;
        }

        @Override
        public BuildStep setSecond() {
            return null;
        }

        public BuildTarget build() {
            BuildTarget buildTarget = new BuildTarget();
            return buildTarget;
        }
    }

    @Test
    public void doTest() {
        BuildTarget buildTarget = StepBuilderTest.newBuilder().setFirst().setSecond().build();
    }
}
