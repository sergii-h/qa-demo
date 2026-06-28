package com.example.demo.e2e.support.mock

import com.example.demo.e2e.support.mock.ApiRouteMock

class ApiRouteMockClient(
    private val wiremockHost: String = WireMockClient.resolveWiremockHost(),
) : AutoCloseable {
    private val wireMock = WireMockClient(wiremockHost)
    private var apiRouteMock: ApiRouteMock? = null

    fun api(): ApiRouteMock {
        if (apiRouteMock == null) {
            apiRouteMock = ApiRouteMock(wireMock)
        }
        return apiRouteMock!!
    }

    fun start() {
        reset()
        api().getTasks()
    }

    fun reset() {
        close()
    }

    override fun close() {
        wireMock.reset()
        apiRouteMock = null
    }
}
