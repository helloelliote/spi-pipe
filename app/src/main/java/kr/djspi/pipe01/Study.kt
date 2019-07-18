package kr.djspi.pipe01

class User(val name: String) {

    init {
        println(name)
    }

    constructor(name: String, age: Int) : this(name) {
        val ss = name.toUpperCase()
        age.compareTo(1.0)
    }

    val upperName = name.toUpperCase()

    fun sayHello() {
        println("$name")
        val user = User("st", 13)
    }
}