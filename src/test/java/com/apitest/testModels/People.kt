package com.apitest.testModels

import java.io.Serializable


open class People : Serializable {
    var id: String = ""
    var age:Int = 0
    var name:String?=null
    var test:Array<String>? = null
}

class Student :People() {
    var isMan:Boolean = true
    var money:Double = 0.12
    override fun toString(): String {
        return "Student(id='$id', isMan=$isMan, money=$money)"
    }
}



