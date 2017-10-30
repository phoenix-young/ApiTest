package com.apitest.spring.extension

import com.apitest.config.Environment
import com.apitest.spring.factoryBeans.SystemTimeFactoryBean
import com.apitest.spring.factoryBeans.EnvValueFactoryBean
import com.apitest.spring.factoryBeans.RepeatFactoryBean
import com.apitest.spring.factoryBeans.StringValidatorFactoryBean
import com.apitest.utils.DateUtils
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser
import org.springframework.beans.factory.xml.NamespaceHandlerSupport
import org.springframework.beans.factory.xml.ParserContext
import org.w3c.dom.Element
import java.lang.reflect.Constructor
import java.util.*
import kotlin.collections.HashMap

class ApiTestNamespaceHandler:NamespaceHandlerSupport(){

    override fun init() {
        registerBeanDefinitionParser("systemTime", SystemTimeBeanDefinitionParser())
        registerBeanDefinitionParser("date",DateBeanDefinitionParser())
        registerBeanDefinitionParser("env", EnvValueBeanDefinitionParser())
        registerBeanDefinitionParser("repeat",StringRepeatDefinitionParser())
        registerBeanDefinitionParser("strVlt",StrVltBeanDefinitionParser())
    }

    companion object {

        private val scopeName:String = "scope"
        private val parentName:String="parent"




        fun setGenericBean(builder:BeanDefinitionBuilder?,element:Element?,propNames:List<String>?){
            if(element!=null && builder !=null){
                propNames?.forEach{
                    if(element.hasAttribute(it)){
                        var property = element.getAttribute(it)
                        builder.addPropertyValue(it,property)
                    }
                }

                var scope = element.getAttribute(scopeName)
                if(scope.isNotBlank()){
                    builder.setScope(scope)
                }

                var parent = element.getAttribute(parentName)
                if(parent.isNotBlank()){
                    builder.setParentName(parent)
                }

            }
        }
    }



    inner class SystemTimeBeanDefinitionParser: AbstractSingleBeanDefinitionParser() {

        private val propList = arrayListOf("prefix","suffix","singleton")

        override fun getBeanClass(element: Element?): Class<*> {
            return SystemTimeFactoryBean::class.java
        }

        override fun doParse(element: Element?, builder: BeanDefinitionBuilder?) {
            ApiTestNamespaceHandler.setGenericBean(builder,element,propList)
        }

    }

    inner class DateBeanDefinitionParser:AbstractSingleBeanDefinitionParser(){
        override fun getBeanClass(element: Element?): Class<*> {
            return Date::class.java
        }

        override fun doParse(element: Element, builder: BeanDefinitionBuilder) {
            var buffer = element!!.getAttribute("buffer")
            if(buffer.isNullOrBlank()){
                return
            }else{
                var time = DateUtils.getTimeByBuffer(buffer)
                builder.addConstructorArgValue(time)
            }
        }
    }

    inner class StringRepeatDefinitionParser:AbstractSingleBeanDefinitionParser(){

        private val propList = arrayListOf("template","length","singleton")

        override fun getBeanClass(element: Element?): Class<*> {
            return RepeatFactoryBean::class.java
        }

        override fun doParse(element: Element?,parserContext: ParserContext, builder: BeanDefinitionBuilder) {
            setGenericBean(builder,element,propList)
            var list = parserContext.delegate.parseListElement(element,builder.rawBeanDefinition)
            when(list.size){
                0-> setGenericBean(builder,element, arrayListOf("template"))
                else->builder.addPropertyValue("template",list[0])
            }






        }
    }

    inner class EnvValueBeanDefinitionParser:AbstractSingleBeanDefinitionParser(){

        override fun getBeanClass(element: Element?): Class<*> {
            return EnvValueFactoryBean::class.java
        }

        override fun doParse(element: Element, builder: BeanDefinitionBuilder) {

            val map:MutableMap<Environment,Any> = HashMap()

            for(e in Environment.values()){
                map.put(e,element.getAttribute(e.name))
            }

            builder.addPropertyValue("envs",map)
        }
    }

    inner class StrVltBeanDefinitionParser:AbstractSingleBeanDefinitionParser(){

        private val propList = arrayListOf("property","expect","method","fetchMode")

        override fun getBeanClass(element: Element?): Class<*> {
            return StringValidatorFactoryBean::class.java
        }

        override fun doParse(element: Element?, parserContext: ParserContext?, builder: BeanDefinitionBuilder?) {
            setGenericBean(builder,element,propList)
        }
    }







}