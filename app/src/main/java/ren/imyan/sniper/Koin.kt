package ren.imyan.sniper

import org.koin.dsl.module

val store = module {
    single {
        Store()
    }
}