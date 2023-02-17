package com.hebeyond.arllimtester.service

import com.hebeyond.arllimtester.persistence.Article
import com.hebeyond.arllimtester.repository.ArticleRepository
import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import java.lang.Thread.sleep
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArticleService(
        private val articleRepository: ArticleRepository
) {
    fun findAll(): MutableList<Article> = articleRepository.findAll()

    fun save(articles: List<Article>) {
        println("Saving articles at ${java.util.Date()} on thread ${Thread.currentThread().name}")
        articleRepository.saveAll(articles)
    }

    fun changeHappenedProcess(articles: List<Article>) {
        sleep(1000)
        articles.forEach {
            it.changeHappened = true
            it.readCount++
        }
        articleRepository.saveAll(articles)
    }
    @Transactional
    fun getMostRecentArticleAndSaveAsChanged(): Article {
        val article = articleRepository.findAllByChangeHappened(false).first()
        article.changeHappened = true
        article.readCount++
        articleRepository.save(article)
        return article
    }

    fun getRequiredData(): MutableList<Article> {
        return articleRepository.findAllByChangeHappened(false)
    }

    fun findAndChangeProcess() {
        val articles = getRequiredData()
        changeHappenedProcess(articles)
    }

    fun findByChangeHappenedFalse(): MutableList<Article> {
        return articleRepository.findAllByChangeHappened(false)
    }
}
