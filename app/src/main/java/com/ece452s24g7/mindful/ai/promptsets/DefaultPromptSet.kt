package com.ece452s24g7.mindful.ai.promptsets

import com.ece452s24g7.mindful.ai.AiPromptSet

const val DEFAULT_SUMMARY_PROMPT = "Please provide me with a summary of my journal entry. " +
        "Keep the summary to only a few sentences in length. " +
        "If you are unable to create a summary, simply respond with \"Sorry, I can't seem to summarize this journal entry.\"" +
        "Here is my journal entry: "

const val DEFAULT_INSIGHT_PROMPT = "Please provide me with a few insights for my journal entry. " +
        "Keep insights strictly positive and helpful. " +
        "Insights should be in point form and only 1 or 2 sentences in length." +
        "Include a newline in between each point." +
        "Keep your response to only a few sentences in length. " +
        "Do not include any preamble or conclusion in your response." +
        "If you are unable to generate any insights, simply respond with \"Sorry, I can't seem to generate any useful insights this journal entry.\"" +
        "Here is my journal entry: "

class DefaultPromptSet: AiPromptSet {
    override fun getSummaryPrompt(text: String) = DEFAULT_SUMMARY_PROMPT + text
    override fun getInsightsPrompt(text: String) = DEFAULT_INSIGHT_PROMPT + text
}