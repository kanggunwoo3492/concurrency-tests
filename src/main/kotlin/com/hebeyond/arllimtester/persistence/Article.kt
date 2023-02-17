package com.hebeyond.arllimtester.persistence

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id


@Entity(name = "article")
class Article (
    @Id
    @GeneratedValue
    var id: Long? = null,
    var data: String? = null,
    var readCount: Int = 0,
    var changeHappened: Boolean = false,
)
