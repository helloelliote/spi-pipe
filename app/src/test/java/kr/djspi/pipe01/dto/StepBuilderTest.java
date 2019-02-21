package kr.djspi.pipe01.dto;

import org.junit.Test;

public class StepBuilderTest {

    public StepBuilderTest() {

    }

    @Test
    public void doTest() {
        BuildTarget buildTarget = StepBuilderTest.newBuilder().setFirst().setSecond().build();
    }

    public static FirstStep newBuilder() {
        return new Steps();
    }

    public static interface FirstStep {
        SecondStep setFirst();
    }

    public static interface SecondStep {
        ThirdStep setSecond();
    }

    public static interface ThirdStep {
        BuildTarget build();
    }

    private static class Steps implements FirstStep, SecondStep, ThirdStep {

        @Override
        public SecondStep setFirst() {
            return this;
        }

        @Override
        public ThirdStep setSecond() {
            return this;
        }

        @Override
        public BuildTarget build() {
            BuildTarget buildTarget = new BuildTarget();
            return buildTarget;
        }
    }
}
