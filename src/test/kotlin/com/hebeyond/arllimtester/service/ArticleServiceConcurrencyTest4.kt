package com.hebeyond.arllimtester.service

import com.hebeyond.arllimtester.persistence.Article
import com.hebeyond.arllimtester.repository.ArticleRepository
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class ArticleServiceConcurrencyTest4 @Autowired constructor(
    private val articleRepository: ArticleRepository,
    private val articleService: ArticleService,
) {
    private val service = Executors.newFixedThreadPool(100)

    private var methodCalled = 0

    private val cachedArticleIds = mutableListOf<Long>()

    // cache 방식 ...
    @Test
    fun test1() {
        articleRepository.deleteAll()
        methodCalled = 0

        val articles = mutableListOf<Article>()
        var longId = 1L
        for (i in 1..100) {
            articles.add(Article(id = longId, data = "content$longId"))
            longId++
        }

        Assertions.assertThat(articles.size).isEqualTo(100)

        articleRepository.saveAll(articles)

        val latch = CountDownLatch(100)
        for (i in 1..100) {
            service.execute {
                for (j in 1..100) {
                    val longIdj = j.toLong()
                    val article = articleRepository.findById(longIdj)
                    if (article.isPresent) {
                        if (cachedArticleIds.contains(longIdj)) {
                            continue
                        }
                        cachedArticleIds.add(longIdj)
                        methodCalled++
                        article.get().changeHappened = true
                        article.get().readCount++
                        articleService.save(listOf(article.get()))
                    }
                }
                latch.countDown()
            }
        }

        latch.await()

        val findAllFalse = articleRepository.findAllByChangeHappened(false)
        Assertions.assertThat(findAllFalse.size).isEqualTo(0)
        val findAll = articleRepository.findAll()
        Assertions.assertThat(
            findAll.map { it.readCount }
        ).allMatch { it == 1 }

        println("methodCalled: $methodCalled")
    }


    @Test
    fun test2() {
        articleRepository.deleteAll()
        methodCalled = 0

        val articles = mutableListOf<Article>()
        var longId = 1L
        for (i in 1..100) {
            articles.add(Article(id = longId, data = "content$longId"))
            longId++
        }

        Assertions.assertThat(articles.size).isEqualTo(100)

        articleRepository.saveAll(articles)

        val latch = CountDownLatch(10)
        for (i in 1..10) {
            service.execute {
                myApi()
                latch.countDown()
            }
        }
        latch.await()


        Thread.sleep(1000)
        val findAllFalse = articleRepository.findAllByChangeHappened(false)
        Assertions.assertThat(findAllFalse.size).isEqualTo(0)
        val findAll = articleRepository.findAll()
        Assertions.assertThat(
            findAll.map { it.readCount }
        ).allMatch { it == 1 }

        println("methodCalled: $methodCalled")
    }
    @Transactional
    fun myApi() {
        val newArticles = articleService.findByChangeHappenedFalse()
        println("newArticles.size: ${newArticles.size}")
        for (article in newArticles) {
            if (article.changeHappened) {
                continue
            }
            methodCalled++
            article.changeHappened = true
            article.readCount++
            articleService.save(listOf(article))
        }
    }
}
