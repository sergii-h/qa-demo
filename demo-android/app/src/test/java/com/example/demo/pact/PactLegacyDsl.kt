package com.example.demo.pact

import au.com.dius.pact.consumer.dsl.PactBuilder
import au.com.dius.pact.core.model.V4Pact

internal fun PactBuilder.legacyHttpPact(
    configure: au.com.dius.pact.consumer.dsl.PactDslWithProvider.() -> au.com.dius.pact.consumer.dsl.PactDslResponse
): V4Pact = usingLegacyDsl().configure().toPact(V4Pact::class.java)
