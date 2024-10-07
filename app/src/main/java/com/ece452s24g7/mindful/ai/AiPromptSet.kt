package com.ece452s24g7.mindful.ai

import com.ece452s24g7.mindful.ai.promptsets.DefaultPromptSet

interface AiPromptSet {
    companion object {
        fun getDefault(): AiPromptSet = DefaultPromptSet()
    }
    fun getSummaryPrompt(text: String): String
    fun getInsightsPrompt(text: String): String
}
