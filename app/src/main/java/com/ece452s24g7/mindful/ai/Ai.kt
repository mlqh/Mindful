package com.ece452s24g7.mindful.ai

class Ai (private val aiAdapter: AiAdapter, private val aiPromptSet: AiPromptSet) {

    fun getSummary(text: String): String {
        return aiAdapter.prompt(aiPromptSet.getSummaryPrompt(text))
    }

    fun getInsights(text: String): String {
        return aiAdapter.prompt(aiPromptSet.getInsightsPrompt(text))
    }

}