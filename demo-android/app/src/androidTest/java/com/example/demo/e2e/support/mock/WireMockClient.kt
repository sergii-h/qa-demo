package com.example.demo.e2e.support.mock

import com.example.demo.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class WireMockClient(
    private val wiremockHost: String = resolveWiremockHost(),
) {
    private val client = OkHttpClient()

    fun clearMocks() {
        deleteAdminResource("/__admin/mappings")
    }

    fun resetScenarios() {
        postAdminResource("/__admin/scenarios/reset", "{}")
    }

    fun reset() {
        clearMocks()
        resetScenarios()
    }

    fun addScenarioMapping(
        scenarioName: String,
        request: JSONObject,
        response: JSONObject,
    ) {
        val requiredState = fetchScenarioState(scenarioName)
        val newState = nextScenarioState(requiredState)

        val mapping = JSONObject()
            .put("scenarioName", scenarioName)
            .put("requiredScenarioState", requiredState)
            .put("newScenarioState", newState)
            .put("request", request)
            .put("response", response)

        postAdminResource("/__admin/mappings", mapping.toString())
    }

    private fun fetchScenarioState(scenarioName: String): String {
        val response = client.newCall(
            Request.Builder()
                .url("$wiremockHost/__admin/scenarios")
                .build(),
        ).execute()
        check(response.isSuccessful) {
            "WireMock GET /__admin/scenarios failed: HTTP ${response.code} ${response.body?.string()}"
        }

        val scenarios = JSONObject(response.body!!.string()).getJSONArray("scenarios")
        for (index in 0 until scenarios.length()) {
            val scenario = scenarios.getJSONObject(index)
            if (scenario.getString("name") == scenarioName) {
                return scenario.getString("state")
            }
        }
        return "Started"
    }

    private fun nextScenarioState(currentState: String): String = when (currentState) {
        "Started" -> "step-1"
        else -> "step-${currentState.removePrefix("step-").toInt() + 1}"
    }

    private fun deleteAdminResource(path: String) {
        val response = client.newCall(
            Request.Builder()
                .delete()
                .url("$wiremockHost$path")
                .build(),
        ).execute()
        check(response.isSuccessful) {
            "WireMock DELETE $path failed: HTTP ${response.code} ${response.body?.string()}"
        }
    }

    private fun postAdminResource(path: String, body: String) {
        val response = client.newCall(
            Request.Builder()
                .url("$wiremockHost$path")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build(),
        ).execute()
        check(response.isSuccessful) {
            "WireMock POST $path failed: HTTP ${response.code} ${response.body?.string()}"
        }
    }

    companion object {
        fun resolveWiremockHost(): String = BuildConfig.WIREMOCK_URL.trimEnd('/')
    }
}
