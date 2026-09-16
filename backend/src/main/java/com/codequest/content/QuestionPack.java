package com.codequest.content;

import java.util.List;

public record QuestionPack(String topic, int version, List<Trail> trails, List<Question> questions) {
    public record Trail(int number, String name) {}
    public record Example(String input, String output) {}
    public record Option(String id, String text) {}
    public record Question(String id, String slug, String title, String topic, String subtopic,
        String difficulty, String questionType, String description, List<Example> examples,
        List<String> constraints, String starterCode, String expectedAnswer, List<Option> options,
        List<String> hints, String explanation, int xpReward, boolean isClassic, List<String> tags,
        int trail, int order, String validator) {}
}
