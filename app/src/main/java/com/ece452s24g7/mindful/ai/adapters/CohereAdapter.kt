package com.ece452s24g7.mindful.ai.adapters

import com.cohere.api.Cohere
import com.cohere.api.requests.ChatRequest
import com.ece452s24g7.mindful.ai.AiAdapter

const val COHERE_TOKEN = BuildConfig.COHERE_API_KEY
const val COHERE_CLIENT = "mindful"

class CohereAdapter: AiAdapter {

    private val cohere = Cohere.builder().token(COHERE_TOKEN).clientName(COHERE_CLIENT).build()

    override fun prompt(message: String): String {
        return cohere.chat(ChatRequest.builder().message(message).build()).text
    }

}
