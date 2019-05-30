package ru.touchin.extensions

import org.jetbrains.exposed.dao.Entity

fun <ID : Comparable<ID>> Entity<ID>.getId(): ID = id.value