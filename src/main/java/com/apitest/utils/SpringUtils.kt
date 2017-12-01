package com.apitest.utils

import com.apitest.dataProvider.TestDataConfig
import com.apitest.spring.beanConfig.ApiBaseDataBeanConfigProcessor
import com.apitest.utils.PathUtils.getClassFolder
import com.apitest.utils.PathUtils.getResourceFile
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader
import org.springframework.context.ApplicationContext
import org.springframework.context.support.AbstractApplicationContext
import org.springframework.context.support.StaticApplicationContext
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import java.lang.reflect.Executable
import java.lang.reflect.Parameter
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier
import java.util.regex.Pattern

object SpringUtils{

    private val contextCache:MutableMap<String,AbstractApplicationContext> = ConcurrentHashMap()

    private val logger = LogManager.getLogger(javaClass)

    fun getContext(cls:Class<*>): AbstractApplicationContext? {
        var file =cls.getResourceFile("xml")
        return getContext(file)
    }

    fun getContext(file:String): AbstractApplicationContext? = getContext(FileSystemResource(file))

    fun getContext(resource:Resource):AbstractApplicationContext?{
        if(contextCache.containsKey(resource.file.absolutePath)){
            return contextCache[resource.file.absolutePath]
        }
        return getContext(mapOf("ApiBaseDataBeanConfigProcessor" to ApiBaseDataBeanConfigProcessor::class.java
            //,"ApiBatchDataBeanConfigProcessor" to ApiBatchDataBeanConfigProcessor::class.java
        ),resource)
    }

    @Synchronized
    private fun getContext(defaultBeans:Map<String,Class<*>>?,vararg resources: Resource): AbstractApplicationContext {
        val ctx = StaticApplicationContext()
        val xdr = XmlBeanDefinitionReader(ctx)
        defaultBeans?.forEach { ctx.registerSingleton(it.key,it.value) }
        if(resources.isNotEmpty()){
            resources.forEach { xdr.loadBeanDefinitions(it) }
        }
        ctx.refresh()

        resources.forEach {
            contextCache.put(it.file.absolutePath,ctx)
        }


        return ctx
    }

    fun clearAll(){
        contextCache.forEach{_,v->
            try{
                v.close()
            }catch (e:Exception){
                logger.error(e)
            }
        }
        contextCache.clear()
    }

    private fun getTestData(para:Parameter,ctx:ApplicationContext,methodName:String?=null):List<Any?>{
        val paraType = para.type
        val qualifier = para.getAnnotation(Qualifier::class.java)
        val data = ctx.getBeansOfType(paraType)
        return when{
            data.isEmpty() && paraType.isEnum -> paraType.enumConstants.toList()
            qualifier==null && methodName!=null->data.filter { it.key == methodName }.map { it.value }
            else->{
                when(qualifier){
                    null -> data.map { it.value }
                    else->data.filter { Pattern.matches(qualifier.value,it.key) }.map { it.value }
                }
            }
        }
    }

    private fun getMutipleParameterData(paras:Array<Parameter>, ctx:ApplicationContext):Array<Array<Any?>>{
        val dataList = Array<Supplier<Array<out Any?>?>>(paras.size,{_-> Supplier { null }})
        var i = 0
        paras.forEach {
            dataList[i++] = Supplier { getTestData(it,ctx).toTypedArray() }
        }
        return CommonUtils.getCartesianProductBySupplier(dataList)
    }

    private fun getOnlyOneParameterData(method:Executable,para:Parameter,ctx:ApplicationContext):Array<Array<Any?>>{
        val name = method.name.split(".").last()
        val data = getTestData(para,ctx,name)
        return Array(data.size,{ arrayOf(data[it])})
    }


    private fun getContext(cls:Class<*>,testDataConfig:TestDataConfig):ApplicationContext {
        return if(testDataConfig.file.isBlank()){
            getContext(cls)
        }else{
            getContext("${cls.getClassFolder()}/${testDataConfig.file}")
        } ?: throw NullPointerException("No context found")
    }

    fun getData(para:Parameter,testDataConfig:TestDataConfig):List<Any?>{
        val ctx = getContext(para.declaringExecutable.declaringClass,testDataConfig)
        return getTestData(para,ctx)
    }

    fun getData(method:Executable,testDataConfig: TestDataConfig):Array<Array<Any?>>{
        val params = method.parameters
        val ctx = getContext(method.declaringClass,testDataConfig)
        return when(params.size){
            1-> getOnlyOneParameterData(method,params[0],ctx)
            else->getMutipleParameterData(params,ctx)
        }
    }





//    fun getData(method: Executable,testDataConfig:TestDataConfig):List<Any?>{
//        val paraClasses = method.parameterTypes
//        if(paraClasses.size!=1){
//            throw IllegalArgumentException("Only 1 parameter allowed in method:$method")
//        }
//        val ctx = if(testDataConfig.file.isBlank()){
//            getContext(method.declaringClass)
//        }else{
//            getContext("${method.declaringClass.getClassFolder()}/${testDataConfig.file}")
//        }
//        //由于构造函数的name是全路径
//        val name = method.name.split(".").last()
//        return with(testDataConfig){
//            when(single){
//                true->{
//                    val data = ctx?.getBean(name)
//                    data?.ofType(ITestData::class.java)?.let {it.id = name}
//                    if(data is BatchData){
//                        data.data
//                    }else{
//                        listOf(data)
//                    }
//                }
//                false->{
//                    var mappedDatas = ctx?.getBeansOfType(paraClasses[0])
//                    if(pattern.isNotEmpty()){
//                        mappedDatas = mappedDatas?.filter {
//                            Pattern.matches(pattern,it.key)
//                        }
//                    }
//                    val list = ArrayList<Any?>()
//                    mappedDatas?.forEach { k, v ->
//                        list.add(v)
//                        v.ofType(ITestData::class.java)?.let{it.id = k}
//                    }
//                    list
//                }
//            }
//        }
//    }
}