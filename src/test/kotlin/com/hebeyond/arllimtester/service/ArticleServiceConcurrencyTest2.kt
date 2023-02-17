package com.hebeyond.arllimtester.service

import com.hebeyond.arllimtester.persistence.Article
import com.hebeyond.arllimtester.repository.ArticleRepository
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ArticleServiceConcurrencyTest2 @Autowired constructor(
    private val articleRepository: ArticleRepository,
    private val articleService: ArticleService,
) {
    private val service = Executors.newFixedThreadPool(100)

    @Test
    fun test7() {
        articleRepository.deleteAll()

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
                myApiV2()
                latch.countDown()
            }
        }
        latch.await()


        sleep(1000)
        val findAllFalse = articleRepository.findAllByChangeHappened(false)
        Assertions.assertThat(findAllFalse.size).isEqualTo(0)
        val findAll = articleRepository.findAll()
        Assertions.assertThat(
            findAll.map { it.readCount }
        ).allMatch { it == 1 }
    }

    fun myApiV2() {
        val newArticles = articleService.findByChangeHappenedFalse()
        println("newArticles.size: ${newArticles.size}")
        chunkAndProcessV2(newArticles)
    }

    fun chunkAndProcessV2(articles: MutableList<Article>) {
        val chunked = articles.chunked(10)

        chunked.forEach { chunk ->
            val updatedArticles = chunk.map { article ->
                article.changeHappened = true
                article.readCount++
                article
            }

            articleService.save(updatedArticles)
        }
    }
}
