package com.ece452s24g7.mindful.ai

import com.ece452s24g7.mindful.ai.adapters.CohereAdapter

interface AiAdapter {
    companion object {
        fun getCohere(): AiAdapter = CohereAdapter()
    }
    fun prompt(message: String): String
}
