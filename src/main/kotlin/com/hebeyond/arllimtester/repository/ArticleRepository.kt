package com.hebeyond.arllimtester.repository

import com.hebeyond.arllimtester.persistence.Article
import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import java.util.Date
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface ArticleRepository: JpaRepository<Article, Long> {

    fun findAllByChangeHappened(changeHappened: Boolean): MutableList<Article>
}
