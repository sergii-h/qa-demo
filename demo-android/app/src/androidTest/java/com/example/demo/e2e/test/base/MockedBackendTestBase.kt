package com.example.demo.e2e.test.base

import com.example.demo.e2e.support.mock.ApiRouteMockClient
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

abstract class MockedBackendTestBase : ComposeInstrumentedTestBase() {
    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(WireMockRule(support.mock))
        .around(allureComposeRuleChain())

    private class WireMockRule(
        private val mockClient: ApiRouteMockClient,
    ) : TestRule {
        override fun apply(base: Statement, description: Description): Statement = object : Statement() {
            override fun evaluate() {
                mockClient.start()
                try {
                    base.evaluate()
                } finally {
                    mockClient.close()
                }
            }
        }
    }
}
