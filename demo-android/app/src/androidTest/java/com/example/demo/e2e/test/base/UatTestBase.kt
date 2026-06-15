package com.example.demo.e2e.test.base

import org.junit.Rule
import org.junit.rules.RuleChain
import java.lang.annotation.Inherited

@MustBeDocumented
@Inherited
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Uat

@Uat
abstract class UatTestBase : ComposeInstrumentedTestBase() {
    @get:Rule
    val ruleChain: RuleChain = allureComposeRuleChain()
}
