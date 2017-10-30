package com.apitest.dataProvider

import kotlin.reflect.KClass

annotation class TestData(
        val source: DataSource = DataSource.Spring,
        val single:Boolean=true,
        val dataProvider: KClass<out IDataProvider<*>> = IDataProvider::class,
        val parallel:Boolean = false,
        val file:String="",
        val pattern:String="")





